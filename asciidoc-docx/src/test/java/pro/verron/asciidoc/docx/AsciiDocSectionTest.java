package pro.verron.asciidoc.docx;

import org.docx4j.model.structure.SectionWrapper;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.wml.STPageOrientation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pro.verron.asciidoc.core.AsciiDocParser;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests for the standalone `[section]` marker, which ends a document
/// section. A section that declares no `[header]`/`[footer]` inherits the
/// previous section's ones, matching Word's per-reference-kind inheritance
/// ("link to previous") semantics.
@DisplayName("AsciiDoc Section Break Tests")
public class AsciiDocSectionTest {

    private static WordprocessingMLPackage toDocx(String adoc) {
        var model = new AsciiDocParser().apply(adoc);
        return new AsciiDocToDocx().apply(model);
    }

    private static WordprocessingMLPackage pkgWithHeaderThenTwoSections(String secondSectionSource) {
        return toDocx("""
                [header]
                --
                Section one header
                --
                
                First section content
                
                [section]
                %s
                """.formatted(secondSectionSource));
    }

    @Test
    @DisplayName("[section] should end the current section and start a new one")
    void sectionMarkerEndsSection() {
        var pkg = toDocx("""
                First section content
                
                [section]
                
                Second section content
                """);
        var sections = pkg.getDocumentModel()
                          .getSections();
        assertEquals(2, sections.size(), "The [section] marker should create a second section");
        var mainContent = pkg.getMainDocumentPart()
                             .getContent();
        var first = (org.docx4j.wml.P) mainContent.get(0);
        assertNotNull(first.getPPr()
                           .getSectPr(),
                "The last paragraph of the first section should carry its sectPr");
    }

    @Test
    @DisplayName("A section without [header] should inherit the previous section's header")
    void sectionWithoutHeaderInheritsPreviousOne() {
        var pkg = pkgWithHeaderThenTwoSections("""
                
                Second section content
                """);
        var sections = pkg.getDocumentModel()
                          .getSections();
        HeaderPart sectionOneHeader = defaultHeader(sections.get(0));
        HeaderPart sectionTwoHeader = defaultHeader(sections.get(1));
        assertSame(sectionOneHeader, sectionTwoHeader,
                "A section without its own [header] should inherit the previous section's header");
    }

    @Test
    @DisplayName("A section with a [header] but no [footer] should inherit only the footer")
    void headerOnlySectionInheritsFooter() {
        var pkg = toDocx("""
                [header]
                --
                Section one header
                --
                
                [footer]
                --
                Section one footer
                --
                
                First section content
                
                [section]
                
                [header]
                --
                Section two header
                --
                
                Second section content
                """);
        var sections = pkg.getDocumentModel()
                          .getSections();
        var sectionOne = sections.get(0)
                                 .getHeaderFooterPolicy();
        var sectionTwo = sections.get(1)
                                 .getHeaderFooterPolicy();
        assertNotNull(sectionOne.getDefaultFooter());
        assertSame(sectionOne.getDefaultFooter(), sectionTwo.getDefaultFooter(),
                "A section without its own [footer] should inherit the previous section's footer");
        assertNotSame(sectionOne.getDefaultHeader(), sectionTwo.getDefaultHeader(),
                "A section with its own [header] should not inherit the previous section's header");
        assertTrue(sectionTwo.getDefaultHeader()
                             .getXML()
                             .contains("Section two header"));
    }

    @Test
    @DisplayName("An empty [footer] block should break footer inheritance")
    void emptyFooterBlockBreaksInheritance() {
        var pkg = toDocx("""
                [header]
                --
                Section one header
                --
                
                [footer]
                --
                Section one footer
                --
                
                First section content
                
                [section]
                
                [footer]
                --
                --
                
                Second section content
                """);
        var sections = pkg.getDocumentModel()
                          .getSections();
        var sectionOne = sections.get(0)
                                 .getHeaderFooterPolicy();
        var sectionTwo = sections.get(1)
                                 .getHeaderFooterPolicy();
        assertNotNull(sectionOne.getDefaultFooter());
        assertNotNull(sectionTwo.getDefaultFooter());
        assertNotSame(sectionOne.getDefaultFooter(), sectionTwo.getDefaultFooter(),
                "An empty [footer] block should reference a distinct empty footer, not inherit");
        assertSame(sectionOne.getDefaultHeader(), sectionTwo.getDefaultHeader(),
                "A section without its own [header] should inherit the previous section's header");
    }

    @Test
    @DisplayName("The [section] marker should survive an AsciiDoc→DOCX→AsciiDoc round-trip")
    void sectionMarkerRoundTripsThroughDocx() {
        var source = """
                First section content

                [section]

                Second section content
                """;
        var rendered = toText(toDocx(source), false);
        assertEquals(source.stripTrailing(), rendered.stripTrailing(),
                "The [section] marker should be reconstructed by the DOCX→AsciiDoc converter");

        var stabilized = toText(toDocx(rendered), false);
        assertEquals(rendered.stripTrailing(), stabilized.stripTrailing(),
                "The round-trip should be a fixed point: converting the rendered text again must not change it");
    }

    @Test
    @DisplayName("Sections with headers and footers should round-trip through DOCX")
    void sectionsWithHeaderFooterRoundTripThroughDocx() {
        var source = """
                [header]
                --
                Section one header
                --

                [footer]
                --
                Section one footer
                --

                First section content

                [section]

                Second section content
                """;
        var rendered = toText(toDocx(source), false);
        assertEquals(source.stripTrailing(), rendered.stripTrailing(),
                "Header/footer blocks and the [section] marker should survive the DOCX round-trip, "
                        + "with each block bound to the section it came from");
    }

    @Test
    @DisplayName("An inheritance-breaking empty [footer] block should round-trip through DOCX")
    void emptyFooterBlockRoundTripsThroughDocx() {
        var source = """
                [footer]
                --
                Section one footer
                --

                First section content

                [section]

                [footer]
                --
                --

                Second section content
                """;
        var rendered = toText(toDocx(source), false);
        assertEquals(source.stripTrailing(), rendered.stripTrailing(),
                "The empty [footer] block that breaks inheritance should survive the DOCX round-trip");
    }

    @Test
    @DisplayName("[section-properties] should apply its page setup to the section it ends, and round-trip")
    void sectionPropertiesRoundTripThroughDocx() {
        var source = """
                [section-properties]
                --
                pgSz={w=16838, h=11906, orient=landscape}
                --

                First section content

                [section]

                Second section content
                """;
        var pkg = toDocx(source);

        var firstPage = pkg.getDocumentModel()
                           .getSections()
                           .getFirst()
                           .getSectPr()
                           .getPgSz();
        assertEquals(BigInteger.valueOf(16838), firstPage.getW(), "The landscape width should be applied");
        assertEquals(BigInteger.valueOf(11906), firstPage.getH(), "The landscape height should be applied");
        assertEquals(STPageOrientation.LANDSCAPE, firstPage.getOrient(), "The orientation should be applied");

        var rendered = toText(pkg, true);
        assertTrue(rendered.contains("pgSz={code=9, h=11906, orient=landscape, w=16838}"),
                "The emitted properties block should carry the landscape page setup: " + rendered);

        // The converter re-emits the document's full page setup (including
        // defaults the source omitted), so the first render is canonical but
        // not byte-identical; converting it again must be a fixed point.
        var stabilized = toText(toDocx(rendered), true);
        assertEquals(rendered.stripTrailing(), stabilized.stripTrailing(),
                "The properties round-trip should be a fixed point");
    }

    @Test
    @DisplayName("Dotted-path and nested-brace property values should be equivalent")
    void dottedAndNestedSectionPropertiesAreEquivalent() {
        var nested = toDocx("""
                [section-properties]
                --
                pgSz={w=11906, h=16838}
                pgMar={top=720, bottom=720}
                --

                Content
                """);
        var dotted = toDocx("""
                [section-properties]
                --
                pgSz.w=11906
                pgSz.h=16838
                pgMar.top=720
                pgMar.bottom=720
                --

                Content
                """);

        for (var pkg : List.of(nested, dotted)) {
            var sectPr = pkg.getMainDocumentPart()
                            .getJaxbElement()
                            .getBody()
                            .getSectPr();
            assertEquals(BigInteger.valueOf(11906), sectPr.getPgSz()
                                                          .getW());
            assertEquals(BigInteger.valueOf(16838), sectPr.getPgSz()
                                                          .getH());
            assertEquals(BigInteger.valueOf(720), sectPr.getPgMar()
                                                        .getTop());
            assertEquals(BigInteger.valueOf(720), sectPr.getPgMar()
                                                        .getBottom());
        }
    }

    /// Renders a DOCX package back to AsciiDoc text, the way consumers compare
    /// stamped documents against their templates.
    private static String toText(WordprocessingMLPackage pkg, boolean emitSectionProperties) {
        var model = new DocxToAsciiDoc(pkg, emitSectionProperties).apply(pkg);
        return new pro.verron.asciidoc.converters.AsciiDocToText(true).apply(model);
    }

    private static HeaderPart defaultHeader(SectionWrapper section) {
        var policy = section.getHeaderFooterPolicy();
        assertNotNull(policy);
        var header = policy.getDefaultHeader();
        assertNotNull(header);
        return header;
    }
}
