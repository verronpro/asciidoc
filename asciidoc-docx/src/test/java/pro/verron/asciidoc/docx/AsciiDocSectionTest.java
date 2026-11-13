package pro.verron.asciidoc.docx;

import org.docx4j.model.structure.SectionWrapper;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pro.verron.asciidoc.core.AsciiDocParser;

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

    private static HeaderPart defaultHeader(SectionWrapper section) {
        var policy = section.getHeaderFooterPolicy();
        assertNotNull(policy);
        var header = policy.getDefaultHeader();
        assertNotNull(header);
        return header;
    }
}
