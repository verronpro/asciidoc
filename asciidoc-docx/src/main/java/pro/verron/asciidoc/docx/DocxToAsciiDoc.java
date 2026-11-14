package pro.verron.asciidoc.docx;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.TextUtils;
import org.docx4j.com.microsoft.schemas.office.drawing.x2016.SVG.main.CTSVGBlip;
import org.docx4j.dml.CTOfficeArtExtensionList;
import org.docx4j.dml.Graphic;
import org.docx4j.dml.picture.Pic;
import org.docx4j.dml.wordprocessingDrawing.Anchor;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.mce.AlternateContent;
import org.docx4j.model.structure.HeaderFooterPolicy;
import org.docx4j.model.structure.SectionWrapper;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.wml.*;
import org.docx4j.wml.PPrBase.PStyle;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.verron.asciidoc.core.*;
import pro.verron.asciidoc.core.Text;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static pro.verron.asciidoc.core.AsciiDocModel.of;

/// Extracts an [AsciiDocModel] from a [WordprocessingMLPackage], converting
/// DOCX content into AsciiDoc blocks and inlines.
///
/// Implements [Function]&lt;[WordprocessingMLPackage], [AsciiDocModel]&gt; and
/// handles paragraphs, headings, tables (including nested), inline formatting
/// (bold, italic, styled, superscript, subscript), hyperlinks, images,
/// structured document tags, comments, headers/footers, and
/// footnotes/endnotes.
///
/// @see AsciiDocToDocx
public final class DocxToAsciiDoc implements Function<WordprocessingMLPackage, AsciiDocModel> {
    private static final String SVG_EXTENSION = "{96DAC541-7B7A-43D3-8B79" + "-37D633B846F1}";
    private static final Logger log = LoggerFactory.getLogger(DocxToAsciiDoc.class);

    private final StyleDefinitionsPart styleDefinitionsPart;
    private final CommentRecorder commentRecorder;
    private final PictRecorder pictRecorder;
    private final BlockRecorder blocks;
    private final WordprocessingMLPackage wordprocessingMLPackage;
    private final AltContentRecorder altContentRecorder;
    private List<SectionWrapper> sectionWrappers;
    private int nextWrapperIndex;
    private final boolean emitSectionProperties;

    /// Constructs a new [DocxToAsciiDoc] for the given DOCX package, emitting
    /// `[section-properties]` blocks for every section's page setup.
    ///
    /// @param pkg the WordprocessingMLPackage to convert
    public DocxToAsciiDoc(WordprocessingMLPackage pkg) {
        this(pkg, true);
    }

    /// Constructs a new [DocxToAsciiDoc] for the given DOCX package.
    ///
    /// @param pkg                    the WordprocessingMLPackage to convert
    /// @param emitSectionProperties  whether to emit a `[section-properties]`
    ///                               block for each section's layout (page
    ///                               size, margins, …). Tests that do not
    ///                               verify those values can pass `false` to
    ///                               keep the output minimal.
    public DocxToAsciiDoc(WordprocessingMLPackage pkg, boolean emitSectionProperties) {
        this.emitSectionProperties = emitSectionProperties;
        this.wordprocessingMLPackage = pkg;
        var mdp = wordprocessingMLPackage.getMainDocumentPart();
        this.styleDefinitionsPart = mdp.getStyleDefinitionsPart(true);
        CommentsPart commentsPart = mdp.getCommentsPart();
        this.commentRecorder = new CommentRecorder(commentsPart);
        this.pictRecorder = new PictRecorder();
        this.altContentRecorder = new AltContentRecorder();
        this.blocks = new BlockRecorder(commentRecorder::attach);
    }

    /// Returns the `[header]`/`[footer]` blocks of the section at `index` that
    /// are not inherited from an earlier section, in Word's model where a
    /// section shares the previous section's parts by referencing the same
    /// part object. Ownership is decided from the section's own
    /// `headerReference`/`footerReference` elements, so parts docx4j
    /// auto-creates for `titlePg` sections (which carry no reference) are
    /// never emitted. Inherited parts are skipped so a section declaring none
    /// emits nothing, and the blocks land in the section's own span. The
    /// reference kind (`first`, `even`) is reflected in the block role so the
    /// kind survives the round-trip.
    private List<Block> ownHeaderFooterBlocks(List<SectionWrapper> wrappers, int index) {
        var result = new ArrayList<Block>();
        var own = referencedParts(wrappers.get(index)
                                          .getSectPr());
        if (own.isEmpty()) return result;

        var inherited = new HashSet<>();
        for (int i = 0; i < index; i++) inherited.addAll(referencedParts(wrappers.get(i)
                                                                                 .getSectPr()));
        own.removeAll(inherited);

        for (var entry : own) {
            var kind = switch (entry.kind()) {
                case "first" -> "-first";
                case "even" -> "-even";
                default -> "";
            };
            if (entry.part() instanceof HeaderPart hp) toHeaderBlock(hp, "header" + kind)
                    .ifPresent(result::add);
            else if (entry.part() instanceof FooterPart fp) toFooterBlock(fp, "footer" + kind)
                    .ifPresent(result::add);
        }
        return result;
    }

    private record ReferencedPart(String kind, Object part) {
    }

    /// Resolves a section's header/footer references to their parts, in
    /// document order. Unresolvable relationships are skipped.
    private List<ReferencedPart> referencedParts(SectPr sectPr) {
        var mdp = wordprocessingMLPackage.getMainDocumentPart();
        var result = new ArrayList<ReferencedPart>();
        for (var ref : sectPr.getEGHdrFtrReferences()) {
            var relId = switch (ref) {
                case HeaderReference h -> h.getId();
                case FooterReference f -> f.getId();
                default -> null;
            };
            if (relId == null) continue;
            var relationship = mdp.getRelationshipsPart()
                                  .getRelationshipByID(relId);
            if (relationship == null) continue;
            var kind = switch (ref) {
                case HeaderReference h -> h.getType()
                                           .value();
                case FooterReference f -> f.getType()
                                           .value();
                default -> "default";
            };
            var target = mdp.getRelationshipsPart()
                            .getPart(relId);
            if (target instanceof HeaderPart hp) result.add(new ReferencedPart(kind, hp));
            else if (target instanceof FooterPart fp) result.add(new ReferencedPart(kind, fp));
        }
        return result;
    }

    private static Optional<ImageInline> extractGraphic(Graphic graphic) {
        if (graphic.getGraphicData() == null) return Optional.empty();
        for (Object o : graphic.getGraphicData().getAny()) {
            Object val = unwrap(o);
            // Handling WML textboxes (Wordprocessing Shape)
            if (val instanceof Pic pic) {
                var inlineImage = getInlineImage(pic);
                return Optional.of(inlineImage);
            }
        }
        return Optional.empty();
    }

    private static ImageInline getInlineImage(Pic pic) {
        var blipFill = pic.getBlipFill();
        var blip = blipFill.getBlip();
        var ctShapeProperties = pic.getSpPr();
        var ctShapePropertiesXfrm = ctShapeProperties.getXfrm();
        var ctShapePropertiesXfrmExt = ctShapePropertiesXfrm.getExt();
        var cx = ctShapePropertiesXfrmExt.getCx();
        var cy = ctShapePropertiesXfrmExt.getCy();
        var embed = ofNullable(blip.getExtLst()).stream()
                .map(CTOfficeArtExtensionList::getExt)
                .flatMap(List::stream)
                .filter(e -> Objects.equals(e.getUri(), SVG_EXTENSION))
                .map(e -> (JAXBElement<?>) e.getAny())
                .map(jaxbElement -> (CTSVGBlip) jaxbElement.getValue())
                .map(CTSVGBlip::getEmbed)
                .findFirst()
                .orElse(blip.getEmbed());

        var map = Map.of("cx", String.valueOf(cx), "cy", String.valueOf(cy));
        return new ImageInline(embed, map);
    }

    private static Object unwrap(Object o) {
        return (o instanceof JAXBElement<?> j) ? j.getValue() : o;
    }

    private static Function<List<pro.verron.asciidoc.core.Inline>, List<pro.verron.asciidoc.core.Inline>> getWrapper(STVerticalAlignRun valign) {
        return switch (valign) {
            case BASELINE -> i -> i;
            case SUPERSCRIPT -> i -> List.of(new Sup(i));
            case SUBSCRIPT -> i -> List.of(new Sub(i));
        };
    }

    private static Function<List<pro.verron.asciidoc.core.Inline>, List<pro.verron.asciidoc.core.Inline>> boldwrapper(BooleanDefaultTrue w) {
        return is -> List.of(new Bold(is));
    }

    private static Function<List<pro.verron.asciidoc.core.Inline>, List<pro.verron.asciidoc.core.Inline>> italicwrapper(BooleanDefaultTrue booleanDefaultTrue) {
        return is -> List.of(new Italic(is));
    }

    private static Function<List<pro.verron.asciidoc.core.Inline>, List<pro.verron.asciidoc.core.Inline>> styledwrapper(String s) {
        return is -> List.of(new Styled(s, is));
    }

    private List<pro.verron.asciidoc.core.Inline> toInlines(ContentAccessor accessor, BreakRecorder breakRecorder) {
        var inlines = new ArrayList<pro.verron.asciidoc.core.Inline>();
        for (Object o : accessor.getContent()) {
            Object val = unwrap(o);
            switch (val) {
                case R r -> inlines.addAll(getInlines(r, breakRecorder));
                case ProofErr _ -> {/* NOOP */}
                case CommentRangeStart crs -> commentRecorder.open(crs.getId(), 0);
                case CommentRangeEnd cre -> commentRecorder.close(cre.getId(), 0);
                case SdtRun sdtRun -> {
                    List<String> list = new ArrayList<>();
                    var id = ofNullable(sdtRun.getSdtPr()).map(SdtPr::getId)
                            .map(Id::getVal)
                            .map(BigInteger::intValueExact)
                            .map(Integer::toHexString)
                            .orElse("");

                    ofNullable(sdtRun.getSdtPr()).map(SdtPr::getTag)
                            .map(Tag::getVal)
                            .map(s -> "tag=" + s)
                            .ifPresent(list::add);
                    list.add(toInlines(sdtRun.getSdtContent()).stream()
                            .map(pro.verron.asciidoc.core.Inline::text)
                            .collect(Collectors.joining()));
                    inlines.add(new MacroInline("form", id, list));
                }
                case CTSmartTagRun tag -> {
                    var list = new ArrayList<String>();
                    list.add("start");
                    ofNullable(tag.getElement()).ifPresent(e -> list.add("element=" + e));
                    ofNullable(tag.getSmartTagPr()).stream()
                            .map(CTSmartTagPr::getAttr)
                            .flatMap(Collection::stream)
                            .forEach(a -> list.add("%s=%s".formatted(a.getName(), a.getVal())));
                    inlines.add(new MacroInline("tag", "", list));
                    inlines.addAll(toInlines(tag));
                    inlines.add(new MacroInline("tag", "", List.of("end")));
                }
                case P.Hyperlink hyperlink -> inlines.addAll(toInlines(hyperlink));
                default -> log.debug("Unexpected inline: {}", val);
            }
        }
        return List.copyOf(inlines);
    }

    private List<pro.verron.asciidoc.core.Inline> getInlines(R r, BreakRecorder breakRecorder) {
        var runInlines = extractInlines(r, breakRecorder);
        List<pro.verron.asciidoc.core.Inline> styled = runInlines;
        if (!runInlines.isEmpty()) styled = significantPr(r.getRPr()).apply(runInlines);
        return styled;
    }

    private List<pro.verron.asciidoc.core.Inline> toInlines(ContentAccessor accessor) {
        return toInlines(accessor, new BreakRecorder());
    }

    private List<pro.verron.asciidoc.core.Inline> extractInlines(ContentAccessor r, BreakRecorder brecorder) {
        var inlines = new ArrayList<pro.verron.asciidoc.core.Inline>();
        var content = r.getContent();
        var iterator = content.iterator();
        var sb = new StringBuilder();
        while (iterator.hasNext()) {
            var rc = unwrap(iterator.next());
            switch (rc) {
                case org.docx4j.wml.Text t -> sb.append(t.getValue());
                case Br br when br.getType() == null -> sb.append(" +\n");
                case Br br when br.getType() == STBrType.TEXT_WRAPPING -> sb.append(" +\n");
                case Br br when br.getType() == STBrType.COLUMN -> brecorder.set();
                case Br br when br.getType() == STBrType.PAGE -> brecorder.set();
                case R.Tab _ -> sb.append("\t");
                case CTFtnEdnRef n -> sb.append("footnote:%s[]".formatted(n.getId()));
                case CommentRangeStart crs -> commentRecorder.open(crs.getId(), inlines.size());
                case CommentRangeEnd cre -> commentRecorder.close(cre.getId(), inlines.size());
                case Pict pict -> {
                    sb.append("pict:%s[]".formatted(pict.getAnchorId()));
                    pictRecorder.add(pict);
                }
                case Drawing drawing -> {
                    if (!sb.isEmpty()) {
                        inlines.add(new Text(sb.toString()));
                        sb = new StringBuilder();
                    }
                    for (Object dO : drawing.getAnchorOrInline()) {
                        Object dV = unwrap(dO);
                        switch (dV) {
                            case Inline inline -> extractGraphic(inline.getGraphic()).ifPresent(inlines::add);
                            case Anchor anchor -> extractGraphic(anchor.getGraphic()).ifPresent(inlines::add);
                            default -> { /* DO NOTHING */ }
                        }
                    }
                }
                case AlternateContent ac -> {
                    var recordId = altContentRecorder.add(ac);
                    sb.append("alternateContent:%d[]".formatted(recordId));
                }
                default -> { /* DO NOTHING */ }
            }
        }
        if (!sb.isEmpty()) {
            inlines.add(new Text(sb.toString()));
        }
        return inlines;
    }

    AsciiDocModel apply(ContentAccessor accessor) {
        readBlocks(accessor);
        return of(blocks.blocks);
    }

    private Function<List<pro.verron.asciidoc.core.Inline>, List<pro.verron.asciidoc.core.Inline>> significantPr(@Nullable RPr rPr) {
        if (rPr == null) return Function.identity();

        List<Function<List<pro.verron.asciidoc.core.Inline>, List<pro.verron.asciidoc.core.Inline>>> wrappers = new ArrayList<>();
        ofNullable(rPr.getVertAlign()).map(CTVerticalAlignRun::getVal)
                .map(DocxToAsciiDoc::getWrapper)
                .ifPresent(wrappers::add);

        ofNullable(rPr.getB()).filter(BooleanDefaultTrue::isVal)
                .map(DocxToAsciiDoc::boldwrapper)
                .ifPresent(wrappers::add);

        ofNullable(rPr.getI()).filter(BooleanDefaultTrue::isVal)
                .map(DocxToAsciiDoc::italicwrapper)
                .ifPresent(wrappers::add);

        ofNullable(rPr.getU()).map(U::getVal)
                .map(u -> DocxToAsciiDoc.styledwrapper("u_" + u.value()))
                .ifPresent(wrappers::add);

        ofNullable(rPr.getStrike()).map(_ -> DocxToAsciiDoc.styledwrapper("strike")).ifPresent(wrappers::add);

        ofNullable(rPr.getHighlight()).map(h -> DocxToAsciiDoc.styledwrapper("highlight_" + h.getVal()))
                .ifPresent(wrappers::add);

        ofNullable(rPr.getColor()).map(c -> DocxToAsciiDoc.styledwrapper("color_" + c.getVal()))
                .ifPresent(wrappers::add);

        ofNullable(rPr.getRStyle()).map(s -> DocxToAsciiDoc.styledwrapper("rStyle_" + s.getVal()))
                .ifPresent(wrappers::add);

        return wrappers.stream().reduce(Function::andThen).orElse(Function.identity());
    }

    private void readBlocks(ContentAccessor accessor) {
        for (Object o : accessor.getContent()) {
            Object val = unwrap(o);
            switch (val) {
                case CommentRangeStart crs -> commentRecorder.open(crs.getId(), 0);
                case CommentRangeEnd cre -> commentRecorder.close(cre.getId(), 0);
                case P p -> {
                    var headerLevel = getHeaderLevel(p);
                    var breakRecorder = new BreakRecorder();

                    ofNullable(p.getPPr()).map(PPr::getRPr)
                            .flatMap(this::stringified)
                            .ifPresent(pr -> blocks.add(new CommentBlock(pr)));

                    var style = style(p).stream().toList();

                    if (headerLevel.isPresent())
                        blocks.add(new Heading(style, headerLevel.get(), toInlines(p, breakRecorder)));
                    else blocks.add(new Paragraph(style, toInlines(p, breakRecorder)));

                    // A paragraph-level sectPr marks a section break (it holds
                    // the section's own sectPr); emit its [section-properties]
                    // block (binds to the section it ends), then the [section]
                    // marker, followed by the next section's own header/footer
                    // blocks. The [section-properties] block is emission
                    // metadata: it is added to the model without consuming any
                    // position in the comment coordinate space, so comment
                    // anchors do not shift with the emitSectionProperties
                    // option.
                    ofNullable(p.getPPr()).map(PPr::getSectPr)
                            .ifPresent(sectPr -> {
                                if (emitSectionProperties) blocks.addMetadata(
                                        sectionPropertiesBlocks(sectPrMap(sectPr)));
                                blocks.add(new SectionBreak());
                                if (sectionWrappers != null && nextWrapperIndex < sectionWrappers.size())
                                    blocks.addAll(ownHeaderFooterBlocks(sectionWrappers,
                                            nextWrapperIndex++));
                            });

                    if (breakRecorder.isSet()) blocks.add(new Break());
                }
                case Tbl tbl -> blocks.add(toTableBlock(tbl));
                case SdtBlock sdtBlock -> blocks.add(toSdtBlock(sdtBlock));
                default -> log.debug("Unexpected block: {}", val);
            }
        }
    }

    private Block toSdtBlock(SdtBlock sdtBlock) {
        var form = "form";
        var id = ofNullable(sdtBlock.getSdtPr()).map(SdtPr::getId)
                .map(Id::getVal)
                .map(BigInteger::intValueExact)
                .map(Integer::toHexString);
        var tag = ofNullable(sdtBlock.getSdtPr()).map(SdtPr::getTag).map(Tag::getVal);
        var header = new ArrayList<String>();
        header.add(form);
        id.ifPresent(e -> header.add("id=" + e));
        tag.ifPresent(e -> header.add("tag=" + e));
        var toAsciiDoc = new DocxToAsciiDoc(wordprocessingMLPackage);
        var docModel = toAsciiDoc.apply(() -> sdtBlock.getSdtContent().getContent());
        return new OpenBlock(header, docModel.getBlocks());
    }

    private Optional<String> stringified(ParaRPr paraRPr) {
        var map = new TreeMap<String, Object>();
        ofNullable(paraRPr.getHighlight()).ifPresent(h -> map.put("highlight", h.getVal()));
        ofNullable(paraRPr.getColor()).ifPresent(c -> map.put("color", c.getVal()));
        ofNullable(paraRPr.getRFonts()).ifPresent(r -> {
            var rFontMap = new TreeMap<String, Object>();
            ofNullable(r.getAscii()).ifPresent(a -> rFontMap.put("ascii", a));
            ofNullable(r.getHAnsi()).ifPresent(h -> rFontMap.put("hAnsi", h));
            ofNullable(r.getEastAsia()).ifPresent(e -> rFontMap.put("eastAsia", e));
            ofNullable(r.getCs()).ifPresent(c -> rFontMap.put("cs", c));
            ofNullable(r.getAsciiTheme()).ifPresent(a -> rFontMap.put("asciiTheme", a.value()));
            ofNullable(r.getHAnsiTheme()).ifPresent(h -> rFontMap.put("hAnsi", h.value()));
            ofNullable(r.getEastAsiaTheme()).ifPresent(e -> rFontMap.put("eastAsia", e.value()));
            ofNullable(r.getCstheme()).ifPresent(c -> rFontMap.put("cs", c.value()));
            if (!rFontMap.isEmpty()) map.put("rFonts", rFontMap);
        });
        ofNullable(paraRPr.getSz()).ifPresent(s -> map.put("sz", s.getVal()));
        ofNullable(paraRPr.getSzCs()).ifPresent(s -> map.put("szCs", s.getVal()));
        ofNullable(paraRPr.getU()).ifPresent(u -> map.put("u", u.getVal()));
        ofNullable(paraRPr.getHighlight()).ifPresent(h -> map.put("highlight", h.getVal()));
        ofNullable(paraRPr.getI()).ifPresent(i -> map.put("i", i.isVal()));
        return map.isEmpty() ? Optional.empty() : Optional.of("runPr %s".formatted(map));
    }

    /// Extracts a section's layout into the canonical nested map consumed by
    /// [SectionProperties]. Only the round-trippable scalar fields are
    /// included; complex children (page borders, column definitions) are
    /// deliberately omitted.
    private Map<String, Object> sectPrMap(SectPr sectPr) {
        var map = new TreeMap<String, Object>();
        ofNullable(sectPr.getDocGrid()).ifPresent(d -> {
            var dgmap = new TreeMap<String, Object>();
            ofNullable(d.getLinePitch()).ifPresent(l -> dgmap.put("linePitch", l));
            ofNullable(d.getCharSpace()).ifPresent(c -> dgmap.put("charSpace", c));
            ofNullable(d.getType()).ifPresent(t -> dgmap.put("type", t.value()));
            map.put("docGrid", dgmap);
        });
        ofNullable(sectPr.getPgMar()).ifPresent(p -> {
            var pmmap = new TreeMap<String, Object>();
            ofNullable(p.getTop()).filter(t -> !BigInteger.ZERO.equals(t)).ifPresent(t -> pmmap.put("top", t));
            ofNullable(p.getBottom()).filter(t -> !BigInteger.ZERO.equals(t)).ifPresent(b -> pmmap.put("bottom", b));
            ofNullable(p.getLeft()).filter(t -> !BigInteger.ZERO.equals(t)).ifPresent(l -> pmmap.put("left", l));
            ofNullable(p.getRight()).filter(t -> !BigInteger.ZERO.equals(t)).ifPresent(r -> pmmap.put("right", r));
            ofNullable(p.getHeader()).filter(t -> !BigInteger.ZERO.equals(t)).ifPresent(h -> pmmap.put("header", h));
            ofNullable(p.getFooter()).filter(t -> !BigInteger.ZERO.equals(t)).ifPresent(f -> pmmap.put("footer", f));
            ofNullable(p.getGutter()).filter(t -> !BigInteger.ZERO.equals(t)).ifPresent(g -> pmmap.put("gutter", g));
            map.put("pgMar", pmmap);
        });
        ofNullable(sectPr.getPgSz()).ifPresent(p -> {
            var psmap = new TreeMap<String, Object>();
            ofNullable(p.getW()).filter(t -> !BigInteger.ZERO.equals(t)).ifPresent(w -> psmap.put("w", w));
            ofNullable(p.getH()).filter(t -> !BigInteger.ZERO.equals(t)).ifPresent(h -> psmap.put("h", h));
            ofNullable(p.getOrient()).ifPresent(o -> psmap.put("orient", o.value()));
            ofNullable(p.getCode()).filter(t -> !BigInteger.ZERO.equals(t)).ifPresent(c -> psmap.put("code", c));
            map.put("pgSz", psmap);
        });
        ofNullable(sectPr.getCols()).ifPresent(c -> {
            var colMap = new TreeMap<String, Object>();
            ofNullable(c.getNum()).filter(t -> !BigInteger.ZERO.equals(t)).ifPresent(n -> colMap.put("num", n));
            ofNullable(c.getSpace()).filter(t -> !BigInteger.ZERO.equals(t)).ifPresent(s -> colMap.put("space", s));
            ofNullable(c.getCol()).filter(l -> !l.isEmpty()).ifPresent(list -> {
                // Encode the ordered column list as a map keyed by index; the
                // applier restores the order numerically.
                var col = new TreeMap<String, Object>();
                for (int i = 0; i < list.size(); i++) {
                    var ct = list.get(i);
                    var m = new TreeMap<String, Object>();
                    ofNullable(ct.getSpace()).ifPresent(s -> m.put("space", s));
                    ofNullable(ct.getW()).ifPresent(w -> m.put("w", w));
                    col.put(String.valueOf(i), m);
                }
                colMap.put("col", col);
            });
            if (!colMap.isEmpty()) map.put("cols", colMap);
        });
        ofNullable(sectPr.getPgBorders()).ifPresent(p -> {
            var pbmap = new TreeMap<String, Object>();
            ofNullable(p.getOffsetFrom()).ifPresent(o -> pbmap.put("offsetFrom", o.value()));
            ofNullable(p.getDisplay()).ifPresent(d -> pbmap.put("display", d.value()));
            ofNullable(p.getZOrder()).ifPresent(z -> pbmap.put("zOrder", z.value()));
            borderMap("bottom", p.getBottom(), pbmap);
            borderMap("left", p.getLeft(), pbmap);
            borderMap("right", p.getRight(), pbmap);
            borderMap("top", p.getTop(), pbmap);
            if (!pbmap.isEmpty()) map.put("pgBorders", pbmap);
        });
        ofNullable(sectPr.getBidi()).filter(BooleanDefaultTrue::isVal)
                .ifPresent(_ -> map.put("bidi", true));
        ofNullable(sectPr.getType()).map(SectPr.Type::getVal).ifPresent(t -> map.put("type", t));
        ofNullable(sectPr.getTitlePg()).filter(BooleanDefaultTrue::isVal)
                .ifPresent(_ -> map.put("titlePg", true));
        return map;
    }

    /// Encodes one page-border side (`val`, `sz`, `space`, `color`) into the
    /// `pgBorders` map. Theme-based colors and the `shadow`/`frame` flags are
    /// not round-trippable (docx4j's `CTBorder` cannot distinguish an unset
    /// flag from a set one) and are skipped.
    private static void borderMap(String side, CTBorder border, TreeMap<String, Object> pbmap) {
        if (border == null) return;
        var bmap = new TreeMap<String, Object>();
        ofNullable(border.getVal()).ifPresent(v -> bmap.put("val", v.value()));
        ofNullable(border.getSz()).filter(t -> !BigInteger.ZERO.equals(t)).ifPresent(s -> bmap.put("sz", s));
        ofNullable(border.getSpace()).filter(t -> !BigInteger.ZERO.equals(t)).ifPresent(s -> bmap.put("space", s));
        ofNullable(border.getColor()).ifPresent(c -> bmap.put("color", c));
        if (!bmap.isEmpty()) pbmap.put(side, bmap);
    }

    /// Wraps a section layout map into a `[section-properties]` block holding
    /// one line per top-level property.
    private static List<Block> sectionPropertiesBlocks(Map<String, Object> map) {
        if (map.isEmpty()) return List.of();
        var lines = SectionProperties.render(map);
        var paragraph = new Paragraph(List.of(new Text(String.join("\n", lines))));
        return List.of(new OpenBlock(List.of(SectionProperties.role()), List.of(paragraph)));
    }

    private Optional<String> style(P p) {
        return ofNullable(p.getPPr()).map(PPr::getPStyle)
                .map(PStyle::getVal)
                .map(styleDefinitionsPart::getNameForStyleID);
    }

    private Optional<Integer> getHeaderLevel(P p) {
        if (p.getPPr() == null || p.getPPr().getPStyle() == null) return Optional.empty();
        var styleId = p.getPPr().getPStyle().getVal();
        var styleName = styleDefinitionsPart.getNameForStyleID(styleId);
        if (styleName == null) styleName = styleId;

        if (styleName.equalsIgnoreCase("Title") || styleName.equalsIgnoreCase("Titre")) {
            return Optional.of(1);
        }
        if (styleName.toLowerCase().startsWith("heading") || styleName.toLowerCase().startsWith("titre")) {
            String levelStr = styleName.replaceAll("\\D", "");
            if (!levelStr.isEmpty()) {
                return Optional.of(Integer.parseInt(levelStr) + 1);
            }
        }
        return Optional.empty();
    }

    private Table toTableBlock(Tbl tbl) {
        List<Row> rows = new ArrayList<>();
        for (Object trO : tbl.getContent()) {
            Object trV = unwrap(trO);
            if (!(trV instanceof Tr tr)) continue;
            List<Cell> cells = new ArrayList<>();
            for (Object tcO : tr.getContent()) {
                Object tcV = unwrap(tcO);
                switch (tcV) {
                    case Tc tc -> cells.add(extractCellModel(tc));
                    case CTSdtCell ctc -> cells.add(extractCellModel(ctc.getSdtContent()
                            .getContent()
                            .stream()
                            .map(DocxToAsciiDoc::unwrap)
                            .filter(Tc.class::isInstance)
                            .map(Tc.class::cast)
                            .findFirst()
                            .orElseThrow()));
                    default -> log.warn("Unknown table cell type: {}", tcV);
                }
            }
            List<String> header = new ArrayList<>();
            if (tr.getTrPr() instanceof TrPr trpr && trpr.getCnfStyleOrDivIdOrGridBefore() instanceof List<JAXBElement<?>> elements) {
                elements.stream()
                        .map(DocxToAsciiDoc::unwrap)
                        .filter(CTCnf.class::isInstance)
                        .map(CTCnf.class::cast)
                        .findFirst()
                        .map(s -> "rowStyle=" + Long.parseLong(s.getVal(), 2))
                        .ifPresent(header::add);

            }
            rows.add(new Row(header, cells));
        }
        return new Table(rows);
    }

    private Cell extractCellModel(Tc tc) {
        var toAsciiDoc = new DocxToAsciiDoc(wordprocessingMLPackage);
        var cellModel = toAsciiDoc.apply(tc);
        var cellBlocks = cellModel.getBlocks();
        String ctCnfVal;
        if (!(tc.getTcPr() instanceof TcPr tcPr)) ctCnfVal = null;
        else if (!(tcPr.getCnfStyle() instanceof CTCnf ctCnf)) ctCnfVal = null;
        else ctCnfVal = "style=" + Long.parseLong(ctCnf.getVal(), 2);
        return new Cell(cellBlocks, ctCnfVal);
    }

    @Override
    public AsciiDocModel apply(WordprocessingMLPackage pkg) {
        var wrappers = pkg.getDocumentModel()
                          .getSections();
        // The first section's own header/footer blocks open the document; the
        // following sections' ones are emitted right after their [section]
        // marker, so each block binds back to the section it came from.
        this.sectionWrappers = wrappers;
        this.nextWrapperIndex = 1;
        blocks.addAll(ownHeaderFooterBlocks(wrappers, 0));

        var mdp = pkg.getMainDocumentPart();
        {
            Document contents;
            try {
                contents = mdp.getContents();
            } catch (Docx4JException e) {
                throw new RuntimeException(e);
            }
            var body = contents.getBody();
            readBlocks(body);
            // The body-level sectPr holds the LAST section's layout; emit it
            // as a [section-properties] block so the final section's page
            // setup round-trips too. As above, it consumes no position in the
            // comment coordinate space.
            if (emitSectionProperties && body.getSectPr() instanceof SectPr sectPr)
                blocks.addMetadata(sectionPropertiesBlocks(sectPrMap(sectPr)));
        }

        try {
            var footnotesPart = mdp.getFootnotesPart();
            if (footnotesPart != null) {
                var contents = footnotesPart.getContents();
                var footnote = contents.getFootnote();
                toNoteBlock("footnotes", footnote).ifPresent(blocks::add);
            }
            var endNotesPart = mdp.getEndNotesPart();
            if (endNotesPart != null) {
                var contents = endNotesPart.getContents();
                var endnote = contents.getEndnote();
                toNoteBlock("endnotes", endnote).ifPresent(blocks::add);
            }
        } catch (Docx4JException e) {
            throw new RuntimeException(e);
        }
        var list = new ArrayList<Block>();
        list.addAll(commentRecorder.all());
        list.addAll(blocks.all());
        list.addAll(pictRecorder.all());
        list.addAll(altContentRecorder.all());
        return of(list);
    }

    private Optional<Block> toFooterBlock(FooterPart footerPart, String role) {
        var toAsciiDoc = new DocxToAsciiDoc(wordprocessingMLPackage);
        var extractedBlocks = toAsciiDoc.apply(footerPart).getBlocks();
        // An empty part is meaningful: it is the AsciiDoc encoding of an
        // inheritance-breaking empty [footer] block, so keep it.
        return Optional.of(new OpenBlock(List.of(role), extractedBlocks));
    }

    private Optional<Block> toHeaderBlock(HeaderPart headerPart, String role) {
        var toAsciiDoc = new DocxToAsciiDoc(wordprocessingMLPackage);
        var extractedBlocks = toAsciiDoc.apply(headerPart).getBlocks();
        // An empty part is meaningful: it is the AsciiDoc encoding of an
        // inheritance-breaking empty [header] block, so keep it.
        return Optional.of(new OpenBlock(List.of(role), extractedBlocks));
    }

    private Optional<Block> toNoteBlock(String role, List<CTFtnEdn> notes) {
        var content = new ArrayList<Block>();
        for (CTFtnEdn note : notes) {
            var noteType = note.getType();
            if (noteType != null && List.of(STFtnEdn.SEPARATOR, STFtnEdn.CONTINUATION_SEPARATOR).contains(noteType))
                continue;
            var toAsciiDoc = new DocxToAsciiDoc(wordprocessingMLPackage);
            var extractedBlocks = toAsciiDoc.apply(note::getContent).getBlocks();
            content.add(new Paragraph(List.of(new Text("%s::".formatted(note.getId())))));
            content.addAll(extractedBlocks);
        }
        return content.isEmpty() ? Optional.empty() : Optional.of(new OpenBlock(List.of(role), content));
    }

    private static class BreakRecorder {
        private boolean set;

        public void set() {
            set = true;
        }

        public boolean isSet() {
            return set;
        }
    }

    /// Tracks comment ranges in the document and produces [MacroBlock]
    /// representations with start/end positions.
    /// <p>
    /// Comment anchors are resolved against the <em>content</em> block
    /// sequence only: a range endpoint is recorded while walking a block's
    /// content and is resolved when that block itself is registered via
    /// [CommentRecorder.attach]. Blocks that are emission metadata rather
    /// than content — the `[section-properties]` blocks — are never
    /// registered, so anchor coordinates are independent of emission options
    /// such as `emitSectionProperties`.
    public static class CommentRecorder {
        private final Deque<CommentBuilder> comments;
        private final List<BigInteger> ids;
        private final Map<BigInteger, CommentBuilder> map;
        private final CommentsPart commentsPart;
        private int contentWeight = 0;
        private final List<PendingAnchor> pendingAnchors = new ArrayList<>();

        /// A comment anchor recorded while walking a block's content, waiting
        /// for the block it belongs to.
        private record PendingAnchor(CommentBuilder builder, boolean start) {}

        CommentRecorder(CommentsPart commentsPart) {
            this.commentsPart = commentsPart;
            comments = new ArrayDeque<>();
            ids = new ArrayList<>();
            map = new HashMap<>();
        }

        /// Registers a content block at its position in the comment
        /// coordinate space (the sum of the sizes of the content blocks
        /// before it), and resolves the anchors that were recorded while
        /// walking its content.
        ///
        /// @param block the content block being added to the model
        public void attach(Block block) {
            var weight = contentWeight;
            contentWeight += block.size();
            for (var anchor : pendingAnchors) {
                if (anchor.start()) anchor.builder()
                                          .setBlockStart(weight);
                else anchor.builder()
                           .setBlockEnd(weight);
            }
            pendingAnchors.clear();
        }

        /// Opens a comment range, anchored to the block currently being
        /// walked. The block coordinate is resolved when that block is
        /// attached.
        ///
        /// @param id        the unique identifier for the comment
        /// @param lineStart the starting inline position
        public void open(BigInteger id, int lineStart) {
            var builder = new CommentBuilder(id);
            builder.setLineStart(lineStart);
            comments.addLast(builder);
            ids.add(id);
            pendingAnchors.add(new PendingAnchor(builder, true));
        }

        /// Closes the comment range matching the given ID, anchored to the
        /// block currently being walked. The block coordinate is resolved
        /// when that block is attached.
        ///
        /// @param id      the unique identifier of the comment
        /// @param lineEnd the ending inline position
        /// @throws IllegalStateException if the ID does not match the last
        ///         opened comment
        public void close(BigInteger id, int lineEnd) {
            var lastComment = comments.removeLast();
            var lastCommentId = lastComment.getId();
            var msg = "Closing comment %s but last comment open is %s".formatted(id, lastCommentId);
            assertThat(lastCommentId.equals(id), msg);
            lastComment.setLineEnd(lineEnd);
            pendingAnchors.add(new PendingAnchor(lastComment, false));
            map.put(id, lastComment);
        }

        private void assertThat(boolean bool, String msg) {
            if (!bool) throw new IllegalStateException(msg);
        }

        /// Returns all recorded comments as [MacroBlock] instances, in
        /// document order.
        ///
        /// @return the comment macro blocks
        public Collection<MacroBlock> all() {
            return ids.stream()
                      .map(map::get)
                      .map(CommentBuilder::createComment)
                      .map(this::asBlock)
                      .toList();
        }

        private MacroBlock asBlock(Comment comment) {
            var id = comment.id();
            var idStr = String.valueOf(id);
            var blockStart = comment.blockStart();
            var lineStart = comment.lineStart();
            var blockEnd = comment.blockEnd();
            var lineEnd = comment.lineEnd();
            var commentMessage = extractComment(id);
            var startProp = "start=\"%d,%d\"".formatted(blockStart, lineStart);
            var endProp = "end=\"%d,%d\"".formatted(blockEnd, lineEnd);
            var valueProp = "value=\"%s\"".formatted(commentMessage);
            var header = List.of(startProp, endProp, valueProp);
            return new MacroBlock(header, "comment", idStr);
        }

        private String extractComment(BigInteger id) {
            try {
                return this.commentsPart.getContents()
                        .getComment()
                        .stream()
                        .filter(c -> Objects.equals(c.getId(), id))
                        .findFirst()
                        .map(this::str)
                        .orElseThrow();
            } catch (Docx4JException e) {
                throw new RuntimeException(e);
            }
        }

        private String str(Comments.Comment comment) {

            var first = (P) comment.getContent().getFirst();
            return TextUtils.getText(first);
        }

    }

    /// Collects the blocks of the document in emission order. Content blocks
    /// are forwarded to the comment recorder so comment anchors resolve
    /// against the content sequence; emission-metadata blocks ([section-properties])
    /// are kept in the model but consume no position in that coordinate space.
    private static class BlockRecorder {
        private final List<Block> blocks = new ArrayList<>();
        private final Consumer<Block> contentBlockConsumer;

        BlockRecorder(Consumer<Block> contentBlockConsumer) {
            this.contentBlockConsumer = contentBlockConsumer;
        }

        public void add(Block block) {
            blocks.add(block);
            contentBlockConsumer.accept(block);
        }

        /// Adds emission-metadata blocks to the model: they appear in
        /// emission order but do not shift the comment coordinate space.
        public void addMetadata(Collection<Block> metadata) {
            blocks.addAll(metadata);
        }

        public List<Block> all() {
            return blocks;
        }

        public void addAll(Collection<Block> blocks) {
            blocks.forEach(this::add);
        }
    }

}
