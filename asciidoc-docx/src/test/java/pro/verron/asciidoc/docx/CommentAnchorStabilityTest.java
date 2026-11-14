package pro.verron.asciidoc.docx;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.wml.CommentRangeEnd;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pro.verron.asciidoc.converters.AsciiDocToText;
import pro.verron.asciidoc.core.AsciiDocParser;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// Comment anchor coordinates (`comment::<id>[start="b,l", end="b,l"]`)
/// must identify a position in the **content** sequence of the document:
/// the `[section-properties]` blocks are emission metadata about the page
/// setup, not content, so emitting them must not shift the anchors. The
/// anchors must also survive a docx→asciidoc→docx→asciidoc round-trip
/// unchanged.
@DisplayName("Comment anchor stability tests")
class CommentAnchorStabilityTest {

    private static final String ANCHOR = "comment::0[start=\"0,0\", end=\"1,0\", value=\"commented text\"]";

    private static String toAsciidoc(WordprocessingMLPackage pkg, boolean skipComments,
            boolean emitSectionProperties) {
        var model = new DocxToAsciiDoc(pkg, emitSectionProperties).apply(pkg);
        return new AsciiDocToText(skipComments).apply(model);
    }

    /// A comment range (crs) opens inside the first paragraph, spans a
    /// section break (the first paragraph carries section 1's sectPr, as
    /// asciidoc generates them), and closes inside the second paragraph (cre).
    private static WordprocessingMLPackage docxWithCommentSpanningTwoSections() throws Exception {
        var factory = new ObjectFactory();
        var pkg = WordprocessingMLPackage.createPackage();
        var mdp = pkg.getMainDocumentPart();
        mdp.getContent()
           .clear();
        mdp.addTargetPart(commentsPart(factory, "commented text"));

        // P0: crs is a direct child, before the run → start="0,0";
        // its sectPr emits a [section] marker after it.
        var p0 = factory.createP();
        var crs = factory.createCommentRangeStart();
        crs.setId(BigInteger.ZERO);
        p0.getContent().add(crs);
        p0.getContent().add(run(factory, "first"));
        var ppr = factory.createPPr();
        ppr.setSectPr(factory.createSectPr());
        p0.setPPr(ppr);

        // P1: the run comes first, cre is a direct child → end="1,0"
        var p1 = factory.createP();
        p1.getContent().add(run(factory, "second"));
        var cre = factory.createCommentRangeEnd();
        cre.setId(BigInteger.ZERO);
        p1.getContent().add(cre);

        mdp.addObject(p0);
        mdp.addObject(p1);
        return pkg;
    }

    private static CommentsPart commentsPart(ObjectFactory factory, String text) throws Exception {
        var part = new CommentsPart();
        var comments = factory.createComments();
        var comment = factory.createCommentsComment();
        comment.setId(BigInteger.ZERO);
        var p = factory.createP();
        p.getContent().add(run(factory, text));
        comment.getContent()
               .add(p);
        comments.getComment()
                .add(comment);
        part.setJaxbElement(comments);
        return part;
    }

    private static R run(ObjectFactory factory, String text) {
        var r = factory.createR();
        var t = factory.createText();
        t.setValue(text);
        r.getContent()
         .add(t);
        return r;
    }

    private static List<String> commentMacros(String asciidoc) {
        return asciidoc.lines()
                       .filter(line -> line.startsWith("comment::"))
                       .toList();
    }

    private record Anchor(int block, int line) {}

    private record CommentSpec(BigInteger id, Anchor start, Anchor end, String value) {}

    private static final Pattern MACRO = Pattern.compile(
            "comment::([0-9]+)\\[start=\"(\\d+),(\\d+)\", end=\"(\\d+),(\\d+)\", value=\"([^\"]*)\"\\]");

    private static List<CommentSpec> parseMacros(String asciidoc) {
        var specs = new ArrayList<CommentSpec>();
        var matcher = MACRO.matcher(asciidoc);
        while (matcher.find()) {
            specs.add(new CommentSpec(new BigInteger(matcher.group(1)),
                    new Anchor(Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3))),
                    new Anchor(Integer.parseInt(matcher.group(4)), Integer.parseInt(matcher.group(5))),
                    matcher.group(6)));
        }
        return specs;
    }

    /// Weight-space walk mirroring [DocxToAsciiDoc]: a paragraph weighs 1,
    /// while the [section] marker and [section-properties] blocks a sectPr
    /// paragraph emits weigh nothing.
    private static void applyComments(WordprocessingMLPackage pkg, List<CommentSpec> specs) throws Exception {
        var factory = new ObjectFactory();
        var content = pkg.getMainDocumentPart()
                         .getContent();
        for (var spec : specs) {
            var start = holder(pkg, factory, spec.id(), spec.value());
            int weight = 0;
            for (var o : content) {
                if (!(o instanceof P p)) continue;
                var inRange = weight <= spec.start().block() && spec.start().block() < weight + 1
                              || weight <= spec.end().block() && spec.end().block() < weight + 1;
                if (!inRange) {
                    weight++;
                    continue;
                }
                if (weight == spec.start().block()) p.getContent()
                                                     .add(Math.min(spec.start()
                                                                       .line(), p.getContent()
                                                                                 .size()),
                                                             start.rangeStart());
                if (weight == spec.end().block()) p.getContent()
                                                   .add(Math.min(spec.end()
                                                                     .line(), p.getContent()
                                                                               .size()),
                                                           start.rangeEnd());
                weight++;
            }
        }
    }

    private record CommentHolder(CommentRangeStart rangeStart, CommentRangeEnd rangeEnd) {}

    private static CommentHolder holder(
            WordprocessingMLPackage pkg, ObjectFactory factory, BigInteger id, String value
    ) throws Exception {
        pkg.getMainDocumentPart()
           .addTargetPart(commentsPart(factory, value));
        var crs = factory.createCommentRangeStart();
        crs.setId(id);
        var cre = factory.createCommentRangeEnd();
        cre.setId(id);
        return new CommentHolder(crs, cre);
    }

    @Test
    @DisplayName("Comment anchors must not shift with the [section-properties] emission option")
    void anchorsAreIndependentOfSectionPropertiesEmission() throws Exception {
        var pkg = docxWithCommentSpanningTwoSections();
        var withProperties = toAsciidoc(pkg, false, true);
        var withoutProperties = toAsciidoc(pkg, false, false);

        assertEquals(List.of(ANCHOR), commentMacros(withoutProperties),
                "The anchor itself should be pinned");
        assertEquals(commentMacros(withoutProperties), commentMacros(withProperties),
                "Emitting [section-properties] blocks must not shift comment anchors");
    }

    @Test
    @DisplayName("Comment anchors must survive a docx→asciidoc→docx→asciidoc round-trip")
    void anchorsSurviveDocxToAsciiDocToDocxRoundTrip() throws Exception {
        var original = docxWithCommentSpanningTwoSections();
        var text = toAsciidoc(original, false, false);
        var specs = parseMacros(text);

        // Strip the comment macros, recompile to docx, and re-apply the
        // comments at the coordinates the converter emitted.
        var stripped = text.replaceAll("(?m)^\\s*comment::[0-9]+\\[[^]]*]\\s*\\n?", "");
        var model = new AsciiDocParser().apply(stripped);
        var regenerated = new AsciiDocToDocx().apply(model);
        applyComments(regenerated, specs);

        assertEquals(List.of(ANCHOR), commentMacros(toAsciidoc(regenerated, false, false)),
                "The anchors should survive the round-trip");
        assertEquals(commentMacros(toAsciidoc(regenerated, false, false)),
                commentMacros(toAsciidoc(regenerated, false, true)),
                "And stay stable across emission options after the round-trip");
    }
}
