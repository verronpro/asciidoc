package pro.verron.asciidoc.converters.converters;

import org.junit.jupiter.api.Test;
import pro.verron.asciidoc.core.core.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AsciiDocToTextTest {

    // Test case 1: Render a simple paragraph
    @Test
    void shouldRenderSimpleParagraph() {
        var text = new Text("Simple paragraph content.");
        var paragraph = new Paragraph(List.of(), List.of(text));
        var model = AsciiDocModel.of(List.of(paragraph));

        var result = new AsciiDocToText(false).apply(model);

        var expected = "Simple paragraph content.\n\n";

        assertEquals(expected, result);
    }

    // Test case 2: Render a heading
    @Test
    void shouldRenderHeading() {
        var text = new Text("Heading Text");
        var heading = new Heading(1, List.of(text));
        var model = AsciiDocModel.of(List.of(heading));

        var result = new AsciiDocToText(false).apply(model);

        var expected = "= Heading Text\n\n";

        assertEquals(expected, result);
    }

    // Test case 3: Render a code block
    @Test
    void shouldRenderCodeBlock() {
        var codeBlock = new CodeBlock("java",
                "System.out.println(\"Hello World\");");
        var model = AsciiDocModel.of(List.of(codeBlock));

        var result = new AsciiDocToText(false).apply(model);

        var expected = """
                [source,java]
                ----
                System.out.println("Hello World");
                ----
                
                """;

        assertEquals(expected, result);
    }

    // Test case 4: Render an image block
    @Test
    void shouldRenderImageBlock() {
        var imageBlock = new ImageBlock("path/to/image.png", "Alt Text");
        var model = AsciiDocModel.of(List.of(imageBlock));

        var result = new AsciiDocToText(false).apply(model);

        var expected = "image::path/to/image.png[Alt Text]\n\n";

        assertEquals(expected, result);
    }

    // Test case 5: Render an unordered list
    @Test
    void shouldRenderUnorderedList() {
        var listItem1 = new ListItem(List.of(new Text("Item 1")));
        var listItem2 = new ListItem(List.of(new Text("Item 2")));
        var unorderedList = new UnorderedList(List.of(listItem1, listItem2));
        var model = AsciiDocModel.of(List.of(unorderedList));

        var result = new AsciiDocToText(false).apply(model);

        var expected = """
                * Item 1
                * Item 2
                
                """;

        assertEquals(expected, result);
    }

    // Test case 6: Render a blockquote
    @Test
    void shouldRenderBlockquote() {
        var text = new Text("This is a blockquote.");
        var blockquote = new QuoteBlock(List.of(text));
        var model = AsciiDocModel.of(List.of(blockquote));

        var result = new AsciiDocToText(false).apply(model);

        var expected = """
                ____
                This is a blockquote.
                ____
                
                """;

        assertEquals(expected, result);
    }

    // Test case 7: Render a table
    @Test
    void shouldRenderTable() {
        var cell1 = new Cell(List.of(new Paragraph(List.of(new Text("Cell 1")))));
        var cell2 = new Cell(List.of(new Paragraph(List.of(new Text("Cell 2")))));
        var row = new Row(List.of(cell1, cell2));
        var table = new Table(List.of(row));
        var model = AsciiDocModel.of(List.of(table));

        var result = new AsciiDocToText(false).apply(model);

        var expected = """
                |===
                |Cell 1
                |Cell 2
                |===
                
                """;

        assertEquals(expected, result);
    }

    // Test case 8: Render with comments skipped
    @Test
    void shouldSkipCommentsIfConfigured() {
        var text = new Text("Paragraph with comment.");
        var paragraph = new Paragraph(List.of(), List.of(text));
        var commentBlock = new CommentBlock("This is a comment.");
        var model = AsciiDocModel.of(List.of(paragraph, commentBlock));

        var result = new AsciiDocToText(true).apply(model);

        var expected = "Paragraph with comment.\n\n";

        assertEquals(expected, result);
    }

    // Test case 9: Render macro blocks
    @Test
    void shouldRenderMacroBlocks() {
        var macroBlock = new MacroBlock(List.of("arg1", "arg2"), "macro", "id");
        var model = AsciiDocModel.of(List.of(macroBlock));

        var result = new AsciiDocToText(false).apply(model);

        var expected = "macro::id[arg1, arg2]\n\n";

        assertEquals(expected, result);
    }

    // Test case 10: Render inline elements
    @Test
    void shouldRenderInlineElements() {
        var boldText = new Bold(List.of(new Text("bold")));
        var italicText = new Italic(List.of(new Text("italic")));
        var supText = new Sup(List.of(new Text("superscript")));
        var subText = new Sub(List.of(new Text("subscript")));
        var inlinesParagraph = new Paragraph(List.of(),
                List.of(boldText, italicText, supText, subText));
        var model = AsciiDocModel.of(List.of(inlinesParagraph));

        var result = new AsciiDocToText(false).apply(model);

        var expected = "*bold*_italic_^superscript^~subscript~\n\n";

        assertEquals(expected, result);
    }
}
