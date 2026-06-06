package pro.verron.asciidoc.converters.converters;

import org.junit.jupiter.api.Test;
import pro.verron.asciidoc.core.core.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AsciiDocToSvgTest {

    @Test
    void apply_shouldRenderEmptyModelCorrectly() {
        var model = AsciiDocModel.of(Map.of("theme", "none"));
        var converter = new AsciiDocToSvg();

        var html = converter.apply(model);

        var expectedHtml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <svg xmlns="http://www.w3.org/2000/svg" width="1200" height="990" viewBox="0 0 1200 990">
                <rect x="0" y="0" width="100%%" height="100%%" />
                <rect x="54" y="144" width="800" height="800" fill="#000" fill-opacity="0.1" rx="2"/>
                <rect x="50" y="140" width="800" height="800" fill="white" stroke="#ccc" stroke-width="1"/>
                </svg>""";
        assertEquals(expectedHtml, html);
    }

    @Test
    void apply_shouldRenderHeadingCorrectly() {
        var text = new Text("Sample Heading");
        var heading = new Heading(1, List.of(text));
        var model = AsciiDocModel.of(Map.of("theme", "none"), heading);
        var converter = new AsciiDocToSvg();

        var html = converter.apply(model);

        var expectedHtml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <svg xmlns="http://www.w3.org/2000/svg" width="1200" height="990" viewBox="0 0 1200 990">
                <rect x="0" y="0" width="100%%" height="100%%" />
                <rect x="54" y="144" width="800" height="800" fill="#000" fill-opacity="0.1" rx="2"/>
                <rect x="50" y="140" width="800" height="800" fill="white" stroke="#ccc" stroke-width="1"/>
                <text x="122" y="243" font-size="31" font-weight="700" fill="#111">Sample Heading</text>
                </svg>""";
        assertEquals(expectedHtml, html);
    }

    @Test
    void apply_shouldRenderParagraphCorrectly() {
        var text = new Text("This is a paragraph.");
        var paragraph = new Paragraph(List.of(), List.of(text));
        var model = AsciiDocModel.of(Map.of("theme", "none"), paragraph);
        var converter = new AsciiDocToSvg();

        var html = converter.apply(model);

        var expectedHtml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <svg xmlns="http://www.w3.org/2000/svg" width="1200" height="990" viewBox="0 0 1200 990">
                <rect x="0" y="0" width="100%%" height="100%%" />
                <rect x="54" y="144" width="800" height="800" fill="#000" fill-opacity="0.1" rx="2"/>
                <rect x="50" y="140" width="800" height="800" fill="white" stroke="#ccc" stroke-width="1"/>
                <text x="122" y="226" font-size="14" font-weight="400" fill="#111">This is a paragraph.</text>
                </svg>""";
        assertEquals(expectedHtml, html);
    }

    @Test
    void apply_shouldRenderOrderedListCorrectly() {
        var item1 = new Text("Item 1");
        var item2 = new Text("Item 2");
        var orderedList = new OrderedList(List.of(new ListItem(List.of(item1)),
                new ListItem(List.of(item2))));
        var model = AsciiDocModel.of(Map.of("theme", "none"), orderedList);
        var converter = new AsciiDocToSvg();

        var html = converter.apply(model);

        var expectedHtml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <svg xmlns="http://www.w3.org/2000/svg" width="1200" height="990" viewBox="0 0 1200 990">
                <rect x="0" y="0" width="100%%" height="100%%" />
                <rect x="54" y="144" width="800" height="800" fill="#000" fill-opacity="0.1" rx="2"/>
                <rect x="50" y="140" width="800" height="800" fill="white" stroke="#ccc" stroke-width="1"/>
                <text x="122" y="226" font-size="14" font-weight="400" fill="#111">1. Item 1</text>
                <text x="122" y="260" font-size="14" font-weight="400" fill="#111">2. Item 2</text>
                </svg>""";
        assertEquals(expectedHtml, html);
    }

    @Test
    void apply_shouldRenderUnorderedListCorrectly() {
        var item1 = new Text("Item 1");
        var item2 = new Text("Item 2");
        var unorderedList =
                new UnorderedList(List.of(new ListItem(List.of(item1)),
                        new ListItem(List.of(item2))));
        var model = AsciiDocModel.of(Map.of("theme", "none"), unorderedList);
        var converter = new AsciiDocToSvg();

        var html = converter.apply(model);

        var expectedHtml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <svg xmlns="http://www.w3.org/2000/svg" width="1200" height="990" viewBox="0 0 1200 990">
                <rect x="0" y="0" width="100%%" height="100%%" />
                <rect x="54" y="144" width="800" height="800" fill="#000" fill-opacity="0.1" rx="2"/>
                <rect x="50" y="140" width="800" height="800" fill="white" stroke="#ccc" stroke-width="1"/>
                <text x="122" y="226" font-size="14" font-weight="400" fill="#111">• Item 1</text>
                <text x="122" y="260" font-size="14" font-weight="400" fill="#111">• Item 2</text>
                </svg>""";
        assertEquals(expectedHtml, html);
    }

    @Test
    void apply_shouldRenderTableCorrectly() {
        var text1 = new Text("Cell 1");
        var text2 = new Text("Cell 2");
        var cell1 = new Cell(List.of(new Paragraph(List.of(), List.of(text1))));
        var cell2 = new Cell(List.of(new Paragraph(List.of(), List.of(text2))));
        var row1 = new Row(List.of(cell1, cell2));
        var text3 = new Text("Cell 3");
        var text4 = new Text("Cell 4");
        var cell3 = new Cell(List.of(new Paragraph(List.of(), List.of(text3))));
        var cell4 = new Cell(List.of(new Paragraph(List.of(), List.of(text4))));
        var row2 = new Row(List.of(cell3, cell4));
        var table = new Table(List.of(row1, row2));
        var model = AsciiDocModel.of(Map.of("theme", "none"), table);
        var converter = new AsciiDocToSvg();

        var html = converter.apply(model);

        var expectedHtml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <svg xmlns="http://www.w3.org/2000/svg" width="1200" height="990" viewBox="0 0 1200 990">
                <rect x="0" y="0" width="100%%" height="100%%" />
                <rect x="54" y="144" width="800" height="800" fill="#000" fill-opacity="0.1" rx="2"/>
                <rect x="50" y="140" width="800" height="800" fill="white" stroke="#ccc" stroke-width="1"/>
                <rect x="122" y="220" width="380" height="56" stroke="black" fill="white"/>
                <rect x="122" y="220" width="380" height="28" stroke="black" fill="white"/>
                <rect x="122" y="220" width="190" height="28" stroke="black" fill="white"/>
                <rect x="312" y="220" width="190" height="28" stroke="black" fill="white"/>
                <rect x="122" y="248" width="380" height="28" stroke="black" fill="white"/>
                <rect x="122" y="248" width="190" height="28" stroke="black" fill="white"/>
                <rect x="312" y="248" width="190" height="28" stroke="black" fill="white"/>
                </svg>""";
        assertEquals(expectedHtml, html);
    }

    @Test
    void apply_shouldRenderImageBlockCorrectly() {
        var imageBlock = new ImageBlock("https://example.com/image.png",
                "Example Image");
        var model = AsciiDocModel.of(Map.of("theme", "none"), imageBlock);
        var converter = new AsciiDocToSvg();

        var html = converter.apply(model);

        var expectedHtml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <svg xmlns="http://www.w3.org/2000/svg" width="1200" height="990" viewBox="0 0 1200 990">
                <rect x="0" y="0" width="100%%" height="100%%" />
                <rect x="54" y="144" width="800" height="800" fill="#000" fill-opacity="0.1" rx="2"/>
                <rect x="50" y="140" width="800" height="800" fill="white" stroke="#ccc" stroke-width="1"/>
                <image x="122" y="220" width="320" height="120" rx="4" href="https://example.com/image.png"/>
                </svg>""";
        assertEquals(expectedHtml, html);
    }
}
