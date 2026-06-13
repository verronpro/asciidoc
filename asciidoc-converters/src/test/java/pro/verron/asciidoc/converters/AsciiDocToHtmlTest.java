package pro.verron.asciidoc.converters;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.junit.jupiter.api.Test;
import pro.verron.asciidoc.core.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// Test suite for the [AsciiDocToHtml] converter, verifying that
/// AsciiDoc model elements are correctly rendered into HTML output.
/// Tests cover rendering of:
///   - Empty models
///   - Headings
///   - Paragraphs
///   - Ordered and unordered lists
///   - Tables
///   - Image blocks
public class AsciiDocToHtmlTest {

    /// Default constructor.
    public AsciiDocToHtmlTest() {
    }

    @Test
    void apply_shouldRenderEmptyModelCorrectly() {
        var model = AsciiDocModel.of(List.of());
        var converter = new AsciiDocToHtml();

        var html = converter.apply(model);

        var expectedHtml = """
                <!DOCTYPE html>
                <html>
                  <head>
                    <meta charset="UTF-8">
                  </head>
                  <body>
                  </body>
                </html>""";
        assertEquals(expectedHtml, html);
    }

    @Test
    void apply_shouldRenderHeadingCorrectly() {
        var text = new Text("Sample Heading");
        var heading = new Heading(1, List.of(text));
        var model = AsciiDocModel.of(List.of(heading));
        var converter = new AsciiDocToHtml();

        var html = converter.apply(model);

        var expectedHtml = """
                <!DOCTYPE html>
                <html>
                  <head>
                    <meta charset="UTF-8">
                  </head>
                  <body>
                <h1>Sample Heading</h1>
                  </body>
                </html>""";
        assertEquals(expectedHtml, html);
    }

    @Test
    void apply_shouldRenderParagraphCorrectly() {
        var text = new Text("This is a paragraph.");
        var paragraph = new Paragraph(List.of(), List.of(text));
        var model = AsciiDocModel.of(List.of(paragraph));
        var converter = new AsciiDocToHtml();

        var html = converter.apply(model);

        var expectedHtml = """
                <!DOCTYPE html>
                <html>
                  <head>
                    <meta charset="UTF-8">
                  </head>
                  <body>
                <p>This is a paragraph.</p>
                  </body>
                </html>""";
        assertEquals(expectedHtml, html);
    }

    @Test
    void apply_shouldRenderOrderedListCorrectly() {
        var item1 = new Text("Item 1");
        var item2 = new Text("Item 2");
        var orderedList = new OrderedList(List.of(new ListItem(List.of(item1)),
                new ListItem(List.of(item2))));
        var model = AsciiDocModel.of(List.of(orderedList));
        var converter = new AsciiDocToHtml();

        var html = converter.apply(model);

        var expectedHtml = """
                <!DOCTYPE html>
                <html>
                  <head>
                    <meta charset="UTF-8">
                  </head>
                  <body>
                <ol>
                  <li>Item 1</li>
                  <li>Item 2</li>
                </ol>
                  </body>
                </html>""";
        assertEquals(expectedHtml, html);
    }

    @Test
    void apply_shouldRenderUnorderedListCorrectly() {
        var item1 = new Text("Item 1");
        var item2 = new Text("Item 2");
        var unorderedList =
                new UnorderedList(List.of(new ListItem(List.of(item1)),
                new ListItem(List.of(item2))));
        var model = AsciiDocModel.of(List.of(unorderedList));
        var converter = new AsciiDocToHtml();

        var html = converter.apply(model);

        var expectedHtml = """
                <!DOCTYPE html>
                <html>
                  <head>
                    <meta charset="UTF-8">
                  </head>
                  <body>
                <ul>
                  <li>Item 1</li>
                  <li>Item 2</li>
                </ul>
                  </body>
                </html>""";
        assertEquals(expectedHtml, html);
    }

    @Test
    void apply_shouldRenderTableCorrectly() {
        var cell1 = new Text("Cell 1");
        var cell2 = new Text("Cell 2");
        var table =
                new Table(List.of(new Row(List.of(new Cell(List.of(new Paragraph(
                        List.of(),
                        List.of(cell1)))),
                new Cell(List.of(new Paragraph(List.of(), List.of(cell2))))))));
        var model = AsciiDocModel.of(List.of(table));
        var converter = new AsciiDocToHtml();

        var html = converter.apply(model);

        var expectedHtml = """
                <!DOCTYPE html>
                <html>
                  <head>
                    <meta charset="UTF-8">
                  </head>
                  <body>
                <table>
                  <tr>
                    <td><p>Cell 1</p></td>
                    <td><p>Cell 2</p></td>
                  </tr>
                </table>
                  </body>
                </html>""";
        assertEquals(Jsoup.clean(expectedHtml, Safelist.basic()),
                Jsoup.clean(html, Safelist.basic()));
    }

    @Test
    void apply_shouldRenderImageBlockCorrectly() {
        var imageBlock = new ImageBlock("https://example.com/image.png",
                "Example Image");
        var model = AsciiDocModel.of(List.of(imageBlock));
        var converter = new AsciiDocToHtml();

        var html = converter.apply(model);

        var expectedHtml = """
                <!DOCTYPE html>
                <html>
                  <head>
                    <meta charset="UTF-8">
                  </head>
                  <body>
                <img src="https://example.com/image.png" alt="Example Image">
                  </body>
                </html>""";
        assertEquals(expectedHtml, html);
    }
}
