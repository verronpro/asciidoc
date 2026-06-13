package pro.verron.asciidoc.converters;

import pro.verron.asciidoc.core.*;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/// Converts an [AsciiDocModel] to a plain AsciiDoc text string.
///
/// Implements [Function]&lt;[AsciiDocModel], String&gt; and renders headings,
/// paragraphs, lists, tables, blockquotes, code blocks, images, inline
/// elements (bold, italic, superscript, subscript, styled, links), and macros
/// into their AsciiDoc text representation.
///
/// @see AsciiDocToHtml
/// @see AsciiDocToSvg
public final class AsciiDocToText
        implements Function<AsciiDocModel, String> {

    private final boolean skipComments;

    /// Constructs a converter that optionally skips comment blocks.
    ///
    /// @param skipComments whether to omit comment blocks in the output
    public AsciiDocToText(boolean skipComments) {
        this.skipComments = skipComments;
    }

    private static String renderInlines(List<Inline> inlines) {
        var sb = new StringBuilder();
        for (var inline : inlines) {
            sb.append(switch (inline) {
                case Text(String text) -> text;
                case Bold(List<Inline> children) ->
                        "*%s*".formatted(renderInlines(children));
                case Italic(List<Inline> children) ->
                        "_%s_".formatted(renderInlines(children));
                case Sup(List<Inline> children) ->
                        "^%s^".formatted(renderInlines(children));
                case Sub(List<Inline> children) ->
                        "~%s~".formatted(renderInlines(children));
                case Tab _ -> sb.append("\t");
                case Link(String url, String text) ->
                        "%s[%s]".formatted(url, text);
                case ImageInline(String path, Map<String, String> map) ->
                        "image:%s[%s]".formatted(path,
                                map.entrySet()
                                   .stream()
                                   .map(e -> e.getKey() + "=" + e.getValue())
                                   .collect(Collectors.joining(", ")));
                case Styled(String role, List<Inline> children) ->
                        "[%s]#%s#".formatted(role, renderInlines(children));
                case MacroInline(String name, String id, List<String> list) ->
                        "%s:%s[%s]".formatted(name,
                                id,
                                String.join(", ", list));
            });
        }
        return sb.toString();
    }

    private static String renderImageBlock(String url, String altText) {
        return "image::%s[%s]".formatted(url, altText);
    }

    private static String renderCodeBlock(String language, String content) {
        return language.isEmpty() ? """
                ----
                %s
                ----""".formatted(content) : """
                [source,%s]
                ----
                %s
                ----""".formatted(language, content);
    }

    private static String renderBlockquote(List<Inline> inlines) {
        return """
                ____
                %s
                ____""".formatted(renderInlines(inlines));
    }

    private static String renderList(List<ListItem> items, String x) {
        return items.stream()
                    .map(item -> x + renderInlines(item.inlines()))
                    .collect(Collectors.joining("\n"));
    }

    private static String renderHeading(int level, List<Inline> inlines) {
        return "%s %s".formatted("=".repeat(level), renderInlines(inlines));
    }

    private static String renderHeader(List<String> header) {
        return header.isEmpty()
                ? ""
                : "[%s]\n".formatted(String.join(", ", header));
    }

    private String renderCellContent(Cell cell, boolean isAsciidoc, int level) {
        var blockList = cell.blocks();
        if (!isAsciidoc) {
            if (blockList.isEmpty()) return "";
            var p = (Paragraph) blockList.getFirst();
            return renderInlines(p.inlines());
        }
        else return blockList.stream()
                             .map(block -> renderBlock(block, level))
                             .collect(Collectors.joining())
                             .trim();
    }

    private String renderBlock(Block block, int tableLevel) {
        var string = switch (block) {
            case Heading(_, int level, List<Inline> inlines) ->
                    renderHeading(level, inlines);
            case Paragraph(List<String> header, List<Inline> inlines) ->
                    renderHeader(header) + renderInlines(inlines);
            case UnorderedList(List<ListItem> items1) ->
                    renderList(items1, "* ");
            case OrderedList(List<ListItem> items) -> renderList(items, ". ");
            case Table(List<Row> rows) -> renderTable(rows, tableLevel);
            case QuoteBlock(List<Inline> inlines) -> renderBlockquote(inlines);
            case CodeBlock(String language, String content) ->
                    renderCodeBlock(language, content);
            case ImageBlock(String url, String altText) ->
                    renderImageBlock(url, altText);
            case OpenBlock openBlock -> render(openBlock);
            case MacroBlock(List<String> list, String name, String id) ->
                    "%s::%s[%s]".formatted(name, id, String.join(", ", list));
            case Break _ -> "<<<";
            case CommentBlock(String comment) ->
                    skipComments ? null : ("// %s").formatted(comment);
        };
        return string == null ? "" : string + "\n\n";
    }

    private String render(OpenBlock openBlock) {
        var sb = new StringBuilder();
        sb.append("[%s]\n".formatted(String.join(", ", openBlock.header())));
        sb.append("--\n");
        openBlock.content()
                 .stream()
                 .map(p -> renderBlock(p, 0))
                 .forEach(sb::append);
        sb.append("--\n");
        return sb.toString();
    }

    private String renderTable(List<Row> rows, int level) {
        var cellDelimiter = switch (level) {
            case 0 -> "|";
            case 1 -> "!";
            default -> throw new IllegalArgumentException(
                    "Table nesting level must be between 0 and 1");
        };
        var tableDelimiter = cellDelimiter + "===";
        var sb = new StringBuilder();
        sb.append(tableDelimiter);
        sb.append("\n");
        for (var row : rows) {
            var header = String.join(",", row.header());
            if (!header.isEmpty()) sb.append('[')
                                     .append(header)
                                     .append("]\n");
            for (var cell : row.cells()) {
                var blockList = cell.blocks();
                var size = blockList.size();
                boolean isAsciidoc = size > 1 || (size == 1
                                                  && !(blockList.getFirst() instanceof Paragraph));
                cell.style()
                    .ifPresent(s -> sb.append("[%s]\n".formatted(s)));
                sb.append(isAsciidoc ? "a" + cellDelimiter : cellDelimiter)
                  .append(renderCellContent(cell, isAsciidoc, level + 1))
                  .append("\n");
            }
        }
        sb.append(tableDelimiter);
        return sb.toString();
    }

    /// Applies the conversion on the given AsciiDoc model and renders its
    /// blocks into a concatenated string representation.
    ///
    /// @param model the parsed AsciiDoc model
    ///
    /// @return the rendered AsciiDoc text
    public String apply(AsciiDocModel model) {
        return model.getBlocks()
                    .stream()
                    .map((Block block) -> renderBlock(block, 0))
                    .collect(Collectors.joining());
    }
}
