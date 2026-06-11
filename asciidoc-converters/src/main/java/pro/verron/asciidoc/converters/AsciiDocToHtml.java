package pro.verron.asciidoc.converters;

import pro.verron.asciidoc.core.core.*;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.joining;

/// Converts an [AsciiDocModel] to an HTML document string.
///
/// Implements [Function]&lt;[AsciiDocModel], String&gt; and renders headings,
/// paragraphs, lists, tables, blockquotes, code blocks, images, and inline
/// elements (bold, italic, links) into their HTML counterparts.
///
/// @see AsciiDocToText
/// @see AsciiDocToSvg
public final class AsciiDocToHtml
        implements Function<AsciiDocModel, String> {

    /// Constructs a new [AsciiDocToHtml] converter.
    public AsciiDocToHtml() {

    }

    private static String renderBlock(Block block) {
        switch (block) {
            case Heading(_, int level, List<Inline> inlines) -> {
                return String.format("<h%d>%s</h%d>\n",
                        level,
                        renderInlines(inlines),
                        level);
            }
            case Paragraph(List<String> header, List<Inline> inlines) -> {
                if (header.isEmpty())
                    return String.format("<p>%s</p>\n", renderInlines(inlines));
                else return String.format("<p class=\"%s\">%s</p>\n",
                        header.getFirst(),
                        renderInlines(inlines));
            }
            case UnorderedList(List<ListItem> items) -> {
                return "<ul>\n" + items.stream()
                                       .map(item -> "  <li>" + renderInlines(
                                               item.inlines()) + "</li>\n")
                                       .collect(joining()) + "</ul>\n";
            }
            case OrderedList(List<ListItem> items) -> {
                return "<ol>\n" + items.stream()
                                       .map(item -> "  <li>" + renderInlines(
                                               item.inlines()) + "</li>\n")
                                       .collect(joining()) + "</ol>\n";
            }
            case Table(List<Row> rows) -> {
                var sb = new StringBuilder("<table>\n");
                for (var row : rows) {
                    sb.append("  <tr>\n");
                    for (var cell : row.cells()) {
                        sb.append("    <td>")
                          .append(cell.blocks()
                                      .stream()
                                      .map(AsciiDocToHtml::renderBlock)
                                      .collect(joining()))
                          .append("</td>\n");
                    }
                    sb.append("  </tr>\n");
                }
                sb.append("</table>\n");
                return sb.toString();
            }
            case QuoteBlock(List<Inline> inlines) -> {
                return "<blockquote>" + renderInlines(inlines)
                       + "</blockquote>\n";
            }
            case CodeBlock(String language, String content) -> {
                return String.format(
                        "<pre><code class=\"language-%s\">%s</code></pre>\n",
                        language,
                        content);
            }
            case ImageBlock(String url, String altText) -> {
                return String.format("<img src=\"%s\" alt=\"%s\">\n",
                        url,
                        altText);
            }
            default -> { /* DO NOTHING */ }
        }
        return "";
    }

    private static String renderInlines(List<Inline> inlines) {
        var sb = new StringBuilder();
        for (var inline : inlines) {
            switch (inline) {
                case Text(String str) -> sb.append(str);
                case Bold(List<Inline> children) -> sb.append("<b>")
                                                      .append(renderInlines(
                                                              children))
                                                      .append("</b>");
                case Italic(List<Inline> children) -> sb.append("<i>")
                                                        .append(renderInlines(
                                                                children))
                                                        .append("</i>");
                case Tab _ -> sb.append("&nbsp;&nbsp;&nbsp;&nbsp;");
                case Link(String url, String str) -> sb.append(String.format(
                        "<a href=\"%s\">%s</a>",
                        url,
                        str));
                case ImageInline(String url, Map<String, String> map) ->
                        sb.append(String.format("<img src=\"%s\" alt=\"%s\">",
                                url,
                                map.get("title")));
                default -> { /* DO NOTHING */ }
            }
        }
        return sb.toString();
    }

    /// Converts the given AsciiDoc model into an HTML document string.
    ///
    /// @param model the parsed AsciiDoc model
    ///
    /// @return the HTML document as a string
    public String apply(AsciiDocModel model) {
        var html = new StringBuilder("""
                <!DOCTYPE html>
                <html>
                  <head>
                    <meta charset="UTF-8">
                  </head>
                  <body>
                """);
        for (var block : model.getBlocks()) html.append(renderBlock(block));
        html.append("""
                  </body>
                </html>""");
        return html.toString();
    }
}
