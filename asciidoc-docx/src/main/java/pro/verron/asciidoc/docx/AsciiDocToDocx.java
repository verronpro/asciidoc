package pro.verron.asciidoc.docx;

import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.*;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.verron.asciidoc.core.*;
import pro.verron.asciidoc.core.Text;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

/// Renders an [AsciiDocModel] into a [WordprocessingMLPackage] using Docx4J.
///
/// Implements [Function]&lt;[AsciiDocModel], [WordprocessingMLPackage]&gt; and
/// converts headings, paragraphs, lists, tables, blockquotes, code blocks,
/// images, and inline elements (bold, italic, links) into their DOCX
/// counterparts.
///
/// A `[section-properties]` block declares the layout of the section whose
/// span it appears in — like `[header]`/`[footer]` blocks — and is merged
/// into that section's `sectPr`.
///
/// @see DocxToAsciiDoc
public final class AsciiDocToDocx implements Function<AsciiDocModel, WordprocessingMLPackage> {
    private static final Logger log = LoggerFactory.getLogger(AsciiDocToDocx.class);
    private static final List<String> KNOWN_SECTIONS = List.of("pgSz", "pgMar", "cols", "docGrid", "type",
            "titlePg");
    private int headerCount = 1;
    private int footerCount = 1;
    private final Map<String, Object> pendingSectionProperties = new TreeMap<>();

    /// Constructs a new [AsciiDocToDocx] converter.
    public AsciiDocToDocx() {
    }

    private static P createHeading(ObjectFactory factory, Heading heading) {
        P p = factory.createP();
        PPr ppr = factory.createPPr();
        PPrBase.PStyle pStyle = factory.createPPrBasePStyle();
        pStyle.setVal("Heading" + heading.level());
        ppr.setPStyle(pStyle);
        p.setPPr(ppr);

        RPr headingRunPr = factory.createRPr();
        // Increase size a bit relative to level if heading styles are missing
        HpsMeasure sz = factory.createHpsMeasure();
        int base = switch (heading.level()) {
            case 1 -> 32; // 16pt
            case 2 -> 28;
            case 3 -> 26;
            case 4 -> 24;
            case 5 -> 22;
            default -> 20;
        };
        sz.setVal(BigInteger.valueOf(base));
        headingRunPr.setSz(sz);
        headingRunPr.setSzCs(sz);

        addInlines(factory, p, heading.inlines(), headingRunPr);
        return p;
    }

    private static P createParagraph(ObjectFactory factory, Paragraph paragraph) {
        P p = factory.createP();
        addInlines(factory, p, paragraph.inlines(), null);
        return p;
    }

    private static void addInlines(ObjectFactory factory, P p, List<Inline> inlines, @Nullable RPr base) {
        for (Inline inline : inlines) {
            emitInline(factory, p, inline, base);
        }
    }

    private static Tbl createTable(ObjectFactory factory, Table table) {
        Tbl tbl = factory.createTbl();
        // Minimal table without explicit grid; Word will auto-fit columns
        for (Row row : table.rows()) {
            Tr tr = factory.createTr();
            for (Cell cell : row.cells()) {
                Tc tc = factory.createTc();
                for (Block block : cell.blocks()) {
                    addBlock(factory, tc.getContent(), block);
                }
                tr.getContent().add(tc);
            }
            tbl.getContent().add(tr);
        }
        return tbl;
    }

    private static void addBlock(ObjectFactory factory, List<Object> content, Block block) throws UnsupportedOperationException {
        switch (block) {
            case Heading h -> content.add(createHeading(factory, h));
            case Paragraph p -> content.add(createParagraph(factory, p));
            case Table t -> content.add(createTable(factory, t));
            case UnorderedList(List<ListItem> items1) -> {
                for (ListItem item : items1) {
                    content.add(createListItem(factory, item, "* "));
                }
            }
            case OrderedList(List<ListItem> items) -> {
                int i = 1;
                for (ListItem item : items) {
                    content.add(createListItem(factory, item, (i++) + ". "));
                }
            }
            case QuoteBlock b -> content.add(createBlockquote(factory, b));
            case CodeBlock cb -> content.add(createCodeBlock(factory, cb));
            case ImageBlock ib -> content.add(createImageBlock(factory, ib));
            case Break _ -> throw new java.lang.UnsupportedOperationException("Breaks are not supported");
            case CommentBlock _ -> throw new UnsupportedOperationException("Comments are not supported");
            case OpenBlock ob -> {
                for (Block subBlock : ob.content()) {
                    addBlock(factory, content, subBlock);
                }
            }
            case MacroBlock _ -> throw new UnsupportedOperationException("Macro blocks are not supported");
            case SectionBreak _ -> throw new UnsupportedOperationException(
                    "Section breaks are only supported at the document top level");
        }
    }

    private static P createListItem(ObjectFactory factory, ListItem item, String prefix) {
        P p = factory.createP();
        R r = factory.createR();
        org.docx4j.wml.Text t = factory.createText();
        t.setValue(prefix);
        r.getContent().add(t);
        p.getContent().add(r);
        addInlines(factory, p, item.inlines(), null);
        return p;
    }

    private static P createBlockquote(ObjectFactory factory, QuoteBlock quoteBlock) {
        P p = factory.createP();
        PPr ppr = factory.createPPr();
        PPrBase.Ind ind = factory.createPPrBaseInd();
        ind.setLeft(BigInteger.valueOf(720)); // 0.5 inch
        ppr.setInd(ind);
        p.setPPr(ppr);
        addInlines(factory, p, quoteBlock.inlines(), null);
        return p;
    }

    private static P createCodeBlock(ObjectFactory factory, CodeBlock codeBlock) {
        P p = factory.createP();
        RPr rpr = factory.createRPr();
        RFonts fonts = factory.createRFonts();
        fonts.setAscii("Courier New");
        fonts.setHAnsi("Courier New");
        rpr.setRFonts(fonts);

        String[] lines = codeBlock.content().split("\n");
        for (int i = 0; i < lines.length; i++) {
            R r = factory.createR();
            r.setRPr(rpr);
            org.docx4j.wml.Text t = factory.createText();
            t.setValue(lines[i]);
            t.setSpace("preserve");
            r.getContent().add(t);
            if (i < lines.length - 1) {
                r.getContent().add(factory.createBr());
            }
            p.getContent().add(r);
        }
        return p;
    }

    private static P createImageBlock(ObjectFactory factory, ImageBlock imageBlock) {
        P p = factory.createP();
        R r = factory.createR();
        org.docx4j.wml.Text t = factory.createText();
        t.setValue("[Image: " + imageBlock.url() + " - " + imageBlock.altText() + "]");
        r.getContent().add(t);
        p.getContent().add(r);
        return p;
    }

    private static void emitInline(ObjectFactory factory, P p, Inline inline, @Nullable RPr base) {
        switch (inline) {
            case Text(String text) -> {
                RPr rpr = base != null ? deepCopy(factory, base) : factory.createRPr();
                String[] lines = text.split("\n", -1);
                for (int i = 0; i < lines.length; i++) {
                    if (!lines[i].isEmpty()) {
                        R r = factory.createR();
                        r.setRPr(rpr);
                        org.docx4j.wml.Text tx = factory.createText();
                        tx.setValue(lines[i]);
                        tx.setSpace("preserve");
                        r.getContent().add(tx);
                        p.getContent().add(r);
                    }
                    if (i < lines.length - 1) {
                        R r = factory.createR();
                        r.setRPr(rpr);
                        r.getContent().add(factory.createBr());
                        p.getContent().add(r);
                    }
                }
                return;
            }
            case Bold(List<Inline> children) -> {
                RPr next = base != null ? deepCopy(factory, base) : factory.createRPr();
                next.setB(new BooleanDefaultTrue());
                for (Inline child : children) {
                    emitInline(factory, p, child, next);
                }
                return;
            }
            case Italic(List<Inline> children) -> {
                RPr next = base != null ? deepCopy(factory, base) : factory.createRPr();
                next.setI(new BooleanDefaultTrue());
                for (Inline child : children) {
                    emitInline(factory, p, child, next);
                }
                return;
            }
            case Tab _ -> {
                R r = factory.createR();
                R.Tab tab = factory.createRTab();
                r.getContent().add(tab);
                p.getContent().add(r);
            }
            default -> { /* DO NOTHING */ }
        }

        if (inline instanceof Link link) {
            R r = factory.createR();
            RPr rpr = base != null ? deepCopy(factory, base) : factory.createRPr();
            Color color = factory.createColor();
            color.setVal("0000FF");
            rpr.setColor(color);
            U u = factory.createU();
            u.setVal(UnderlineEnumeration.SINGLE);
            rpr.setU(u);
            r.setRPr(rpr);
            org.docx4j.wml.Text t = factory.createText();
            t.setValue(link.text());
            r.getContent().add(t);
            p.getContent().add(r);
        }

        if (inline instanceof ImageInline ii) {
            R r = factory.createR();
            org.docx4j.wml.Text t = factory.createText();
            t.setValue("[Image: " + ii.path() + "]");
            r.getContent().add(t);
            p.getContent().add(r);
        }
    }

    private static RPr deepCopy(ObjectFactory factory, RPr src) {
        // Minimal copy of relevant props; docx4j doesn't offer a trivial
        // clone here.
        RPr c = factory.createRPr();
        if (src.getB() != null) {
            BooleanDefaultTrue b = new BooleanDefaultTrue();
            c.setB(b);
        }
        if (src.getI() != null) {
            BooleanDefaultTrue i = new BooleanDefaultTrue();
            c.setI(i);
        }
        if (src.getSz() != null) {
            HpsMeasure sz = factory.createHpsMeasure();
            sz.setVal(src.getSz().getVal());
            c.setSz(sz);
            HpsMeasure szCs = factory.createHpsMeasure();
            szCs.setVal(src.getSz().getVal());
            c.setSzCs(szCs);
        }
        return c;
    }

    private static boolean isHeaderOrFooter(OpenBlock ob) {
        return ob.header().stream().anyMatch(h -> h.startsWith("header") || h.startsWith("footer"));
    }

    private static boolean isSectionProperties(OpenBlock ob) {
        return ob.header().stream().anyMatch(SectionProperties.role()::equals);
    }

    /// Collects the property lines of a `[section-properties]` block into the
    /// pending map; they will be merged into the sectPr of the section whose
    /// span the block appeared in.
    private void mergeSectionProperties(OpenBlock ob) {
        for (Block block : ob.content()) {
            if (!(block instanceof Paragraph(var header, var inlines)) || !header.isEmpty()) continue;
            var text = inlines.stream()
                              .filter(Text.class::isInstance)
                              .map(inline -> ((Text) inline).text())
                              .reduce("", (a, b) -> a + b);
            for (var line : text.split("\n")) {
                var trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
                var parsed = SectionProperties.parse(List.of(trimmed));
                deepMerge(parsed);
            }
        }
    }

    private void deepMerge(Map<String, Object> parsed) {
        parsed.forEach((key, value) -> {
            if (value instanceof Map<?, ?> newNested && pendingSectionProperties.get(key) instanceof Map<?, ?>) {
                @SuppressWarnings("unchecked") var existing = (Map<String, Object>) pendingSectionProperties.get(key);
                @SuppressWarnings("unchecked") var toMerge = (Map<String, Object>) newNested;
                toMerge.forEach(existing::put);
            } else pendingSectionProperties.put(key, value);
        });
    }

    /// Merges the pending `[section-properties]` values into the current
    /// body-level sectPr (the one that is demoted at the next `[section]`
    /// marker, or the final document section).
    private void applyPendingSectionProperties(WordprocessingMLPackage pkg, ObjectFactory factory) {
        if (pendingSectionProperties.isEmpty()) return;
        var body = pkg.getMainDocumentPart()
                      .getJaxbElement()
                      .getBody();
        var sectPr = body.getSectPr();
        if (sectPr == null) {
            sectPr = factory.createSectPr();
            body.setSectPr(sectPr);
        }
        applySectionProperties(factory, sectPr, pendingSectionProperties);
        pendingSectionProperties.clear();
    }

    private static java.util.Optional<BigInteger> bigInt(Map<?, ?> map, String key) {
        return java.util.Optional.ofNullable((BigInteger) map.get(key));
    }

    private static void applySectionProperties(ObjectFactory factory, SectPr sectPr,
            Map<String, Object> properties) {
        for (var entry : properties.entrySet()) {
            var key = entry.getKey();
            if (!KNOWN_SECTIONS.contains(key)) {
                log.warn("Ignoring unsupported [section-properties] entry: {}", key);
                continue;
            }
            var value = entry.getValue();
            switch (key) {
                case "pgSz" -> {
                    if (!(value instanceof Map<?, ?> pgSz)) throw new IllegalArgumentException("pgSz must be a map");
                    var ct = pgSz(factory, sectPr);
                    bigInt(pgSz, "w").ifPresent(ct::setW);
                    bigInt(pgSz, "h").ifPresent(ct::setH);
                    bigInt(pgSz, "code").ifPresent(ct::setCode);
                    ofNullableMap(pgSz, "orient").map(String::valueOf)
                            .ifPresent(o -> ct.setOrient(STPageOrientation.fromValue(o)));
                }
                case "pgMar" -> {
                    if (!(value instanceof Map<?, ?> pgMar)) throw new IllegalArgumentException("pgMar must be a map");
                    var ct = pgMar(factory, sectPr);
                    bigInt(pgMar, "top").ifPresent(ct::setTop);
                    bigInt(pgMar, "bottom").ifPresent(ct::setBottom);
                    bigInt(pgMar, "left").ifPresent(ct::setLeft);
                    bigInt(pgMar, "right").ifPresent(ct::setRight);
                    bigInt(pgMar, "header").ifPresent(ct::setHeader);
                    bigInt(pgMar, "footer").ifPresent(ct::setFooter);
                    bigInt(pgMar, "gutter").ifPresent(ct::setGutter);
                }
                case "cols" -> {
                    if (!(value instanceof Map<?, ?> cols)) throw new IllegalArgumentException("cols must be a map");
                    var ct = cols(factory, sectPr);
                    bigInt(cols, "num").ifPresent(ct::setNum);
                    bigInt(cols, "space").ifPresent(ct::setSpace);
                }
                case "docGrid" -> {
                    if (!(value instanceof Map<?, ?> docGrid)) throw new IllegalArgumentException(
                            "docGrid must be a map");
                    var ct = docGrid(factory, sectPr);
                    bigInt(docGrid, "linePitch").ifPresent(ct::setLinePitch);
                    bigInt(docGrid, "charSpace").ifPresent(ct::setCharSpace);
                    ofNullableMap(docGrid, "type").map(String::valueOf)
                            .ifPresent(t -> ct.setType(STDocGrid.fromValue(t)));
                }
                case "type" -> {
                    var ct = type(factory, sectPr);
                    ct.setVal(String.valueOf(value));
                }
                case "titlePg" -> {
                    if (Boolean.TRUE.equals(value)) {
                        var ct = new BooleanDefaultTrue();
                        ct.setVal(true);
                        sectPr.setTitlePg(ct);
                    }
                }
                default -> log.warn("Ignoring unsupported [section-properties] entry: {}", key);
            }
        }
    }

    private static java.util.Optional<Object> ofNullableMap(Map<?, ?> map, String key) {
        return java.util.Optional.ofNullable(map.get(key));
    }

    private static SectPr.PgSz pgSz(ObjectFactory factory, SectPr sectPr) {
        var ct = sectPr.getPgSz();
        if (ct == null) {
            ct = factory.createSectPrPgSz();
            sectPr.setPgSz(ct);
        }
        return ct;
    }

    private static SectPr.PgMar pgMar(ObjectFactory factory, SectPr sectPr) {
        var ct = sectPr.getPgMar();
        if (ct == null) {
            ct = factory.createSectPrPgMar();
            sectPr.setPgMar(ct);
        }
        return ct;
    }

    private static CTColumns cols(ObjectFactory factory, SectPr sectPr) {
        var ct = sectPr.getCols();
        if (ct == null) {
            ct = factory.createCTColumns();
            sectPr.setCols(ct);
        }
        return ct;
    }

    private static CTDocGrid docGrid(ObjectFactory factory, SectPr sectPr) {
        var ct = sectPr.getDocGrid();
        if (ct == null) {
            ct = factory.createCTDocGrid();
            sectPr.setDocGrid(ct);
        }
        return ct;
    }

    private static SectPr.Type type(ObjectFactory factory, SectPr sectPr) {
        var ct = sectPr.getType();
        if (ct == null) {
            ct = factory.createSectPrType();
            sectPr.setType(ct);
        }
        return ct;
    }

    private static void addReference(WordprocessingMLPackage pkg, ObjectFactory factory, String relId, String role, boolean isHeader) {
        SectPr sectPr = pkg.getMainDocumentPart().getJaxbElement().getBody().getSectPr();
        if (sectPr == null) {
            sectPr = factory.createSectPr();
            pkg.getMainDocumentPart().getJaxbElement().getBody().setSectPr(sectPr);
        }

        HdrFtrRef type = switch (role) {
            case "header-even", "footer-even" -> {
                enableEvenOddHeaders(pkg, factory);
                yield HdrFtrRef.EVEN;
            }
            case "header-first", "footer-first" -> {
                sectPr.setTitlePg(new BooleanDefaultTrue());
                yield HdrFtrRef.FIRST;
            }
            default -> HdrFtrRef.DEFAULT;
        };

        if (isHeader) {
            HeaderReference ref = factory.createHeaderReference();
            ref.setId(relId);
            ref.setType(type);
            sectPr.getEGHdrFtrReferences().add(ref);
        } else {
            FooterReference ref = factory.createFooterReference();
            ref.setId(relId);
            ref.setType(type);
            sectPr.getEGHdrFtrReferences().add(ref);
        }
    }

    private static void enableEvenOddHeaders(WordprocessingMLPackage pkg, ObjectFactory factory) {
        try {
            DocumentSettingsPart dsp = pkg.getMainDocumentPart().getDocumentSettingsPart();
            if (dsp == null) {
                dsp = new DocumentSettingsPart();
                pkg.getMainDocumentPart().addTargetPart(dsp);
                dsp.setJaxbElement(factory.createCTSettings());
            }
            dsp.getContents().setEvenAndOddHeaders(new BooleanDefaultTrue());
        } catch (Docx4JException e) {
            throw new RuntimeException(e);
        }
    }

    /// Creates a new WordprocessingMLPackage and fills it with content from the
    /// model.
    ///
    /// @param model parsed AsciiDoc model
    /// @return package containing the rendered document
    @Override
    public WordprocessingMLPackage apply(AsciiDocModel model) {
        headerCount = 1;
        footerCount = 1;
        pendingSectionProperties.clear();
        try {
            var pkg = WordprocessingMLPackage.createPackage();
            var factory = Context.getWmlObjectFactory();
            var mainContent = pkg.getMainDocumentPart().getContent();
            mainContent.clear();

            for (Block block : model.getBlocks()) {
                if (block instanceof OpenBlock ob && isHeaderOrFooter(ob)) {
                    processHeaderOrFooter(pkg, factory, ob);
                } else if (block instanceof OpenBlock ob && isSectionProperties(ob)) {
                    mergeSectionProperties(ob);
                } else if (block instanceof SectionBreak) {
                    // Properties declared in this section's span apply to the
                    // sectPr that is about to be demoted onto its last paragraph.
                    applyPendingSectionProperties(pkg, factory);
                    endSection(pkg, factory, mainContent);
                } else {
                    addBlock(factory, mainContent, block);
                }
            }
            // Properties declared in the last section's span (no [section]
            // marker follows) apply to the document's final, body-level sectPr.
            applyPendingSectionProperties(pkg, factory);
            return pkg;
        } catch (Docx4JException e) {
            throw new IllegalStateException("Unable to create WordprocessingMLPackage", e);
        }
    }

    /// Closes the section that just ended by moving the body-level sectPr
    /// onto the last paragraph of that section — how Word marks a section
    /// break — and installing a fresh body-level sectPr for the next one.
    ///
    /// Headers and footers declared since the previous break are attached to
    /// the moved sectPr, while the next section starts with none, so Word
    /// inherits them from the previous section.
    private static void endSection(WordprocessingMLPackage pkg, ObjectFactory factory, List<Object> content) {
        var body = pkg.getMainDocumentPart().getJaxbElement().getBody();
        var sectionSectPr = body.getSectPr();
        if (sectionSectPr == null) sectionSectPr = factory.createSectPr();
        body.setSectPr(factory.createSectPr());

        for (int i = content.size() - 1; i >= 0; i--) {
            if (content.get(i) instanceof P p) {
                var pPr = p.getPPr();
                if (pPr == null) {
                    pPr = factory.createPPr();
                    p.setPPr(pPr);
                }
                pPr.setSectPr(sectionSectPr);
                return;
            }
        }
        // The ended section carries no paragraph: emit an empty one to hold its sectPr
        P p = factory.createP();
        PPr pPr = factory.createPPr();
        pPr.setSectPr(sectionSectPr);
        p.setPPr(pPr);
        content.add(p);
    }

    private void processHeaderOrFooter(WordprocessingMLPackage pkg, ObjectFactory factory, OpenBlock ob) {
        String role = ob.header().getFirst();
        try {
            if (role.startsWith("header")) {
                HeaderPart hp = new HeaderPart(new PartName("/word/header" + (headerCount++) + ".xml"));
                hp.setJaxbElement(factory.createHdr());
                for (Block subBlock : ob.content()) {
                    addBlock(factory, hp.getContent(), subBlock);
                }
                Relationship rel = pkg.getMainDocumentPart().addTargetPart(hp);
                addReference(pkg, factory, rel.getId(), role, true);
            } else if (role.startsWith("footer")) {
                FooterPart fp = new FooterPart(new PartName("/word/footer" + (footerCount++) + ".xml"));
                fp.setJaxbElement(factory.createFtr());
                for (Block subBlock : ob.content()) {
                    addBlock(factory, fp.getContent(), subBlock);
                }
                Relationship rel = pkg.getMainDocumentPart().addTargetPart(fp);
                addReference(pkg, factory, rel.getId(), role, false);
            }
        } catch (Docx4JException e) {
            throw new RuntimeException(e);
        }
    }
}
