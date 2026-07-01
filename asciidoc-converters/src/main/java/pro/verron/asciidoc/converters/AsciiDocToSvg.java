package pro.verron.asciidoc.converters;

import pro.verron.asciidoc.converters.svg.*;
import pro.verron.asciidoc.core.*;

import java.util.*;

import static pro.verron.asciidoc.converters.svg.AsciiDocFont.getAwtFont;
import static pro.verron.asciidoc.converters.svg.AsciiDocMetrics.wrapText;
import static pro.verron.asciidoc.converters.svg.SvgAttribute.attr;

/// Converts an [AsciiDocModel] into an SVG document simulating an editor
/// interface.
///
/// The theme is read from the model attribute {@code "theme"}. Supported
/// values are {@code "word"}, {@code "gdocs"}, and {@code "libre"}.
///
/// @see AsciiDocToHtml
/// @see AsciiDocToText
public final class AsciiDocToSvg {
    private static final int VIEWPORT_WIDTH = 1200;
    private static final int BANNER_HEIGHT = 100;
    private static final int PAGE_WIDTH = 800;
    private static final int PAGE_LEFT = 50;
    private static final int PAGE_MARGIN_TOP = 40;
    private static final int PAGE_PADDING = 72;
    private static final int COMMENTS_LEFT = 900;
    private static final int COMMENT_WIDTH = 250;
    private static final int BODY_FONT_SIZE = 14;
    private static final int LINE_HEIGHT = 20;

    /// Constructs a new [AsciiDocToSvg] converter.
    public AsciiDocToSvg() {
    }

    /// Converts the given AsciiDoc model into an SVG document simulating an
    /// editor interface.
    ///
    /// @param model the parsed AsciiDoc model
    /// @return the SVG document as an XML string
    public String apply(AsciiDocModel model) {
        var themeStr = model.getAttribute("theme").orElse("word");
        var theme = Theme.valueOf(themeStr.toUpperCase());

        var comments = extractComments(model);
        var blockToComments = mapCommentsToBlocks(comments);

        var pageY = BANNER_HEIGHT + PAGE_MARGIN_TOP;
        var currentY = pageY + PAGE_PADDING;

        var blockPositions = new ArrayList<BlockPosition>();
        var bodyElements = new ArrayList<SvgElement>();

        var modelBlocks = model.getBlocks();
        for (int i = 0; i < modelBlocks.size(); i++) {
            var block = modelBlocks.get(i);
            if (block instanceof MacroBlock m && "comment".equals(m.name())) continue;

            int startY = currentY;
            boolean isCommented = blockToComments.containsKey(i);

            var result = renderBlock(block, PAGE_LEFT + PAGE_PADDING, currentY, isCommented, theme);
            currentY = result.nextY();
            bodyElements.addAll(result.elements());
            blockPositions.add(new BlockPosition(i, startY, currentY - 8));
        }

        var pageHeight = Math.max(800, currentY - pageY + PAGE_PADDING);
        int totalHeight = pageY + pageHeight + 50;

        var children = new ArrayList<SvgElement>();

        // Background
        var bgColor = theme.getBgColor();
        SvgRect bg;
        if (bgColor.isEmpty()) bg = new SvgRect("0", "0", "100%", "100%");
        else {
            var fill = attr("fill", bgColor.get());
            bg = new SvgRect("0", "0", "100%", "100%", fill);
        }
        children.add(bg);

        // Editor Banner
        var title = model.getAttribute("title").orElse("Document.docx");
        children.addAll(theme.renderBanner(title, BANNER_HEIGHT));

        // Page Shadow
        children.add(new SvgRect(String.valueOf(PAGE_LEFT + 4),
                String.valueOf(pageY + 4),
                String.valueOf(PAGE_WIDTH),
                String.valueOf(pageHeight),
                attr("fill", "#000"),
                attr("fill-opacity", "0.1"),
                attr("rx", "2")
        ));
        // Page
        children.add(new SvgRect(String.valueOf(PAGE_LEFT),
                String.valueOf(pageY),
                String.valueOf(PAGE_WIDTH),
                String.valueOf(pageHeight),
                attr("fill", "white"),
                attr("stroke", "#ccc"),
                attr("stroke-width", "1")
        ));

        // Body content
        children.addAll(bodyElements);

        // Comments and connectors
        children.addAll(renderComments(blockToComments, blockPositions, theme));

        var doc = new SvgDocument(VIEWPORT_WIDTH, totalHeight, children);
        var serialized = doc.serialize();
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + serialized;
    }

    private List<CommentInfo> extractComments(AsciiDocModel model) {
        var comments = new ArrayList<CommentInfo>();
        for (var block : model.getBlocks()) {
            if (!(block instanceof MacroBlock macro)) continue;
            if (!Objects.equals(macro.name(), "comment")) continue;

            var start = macro.attribute("start");
            var author = macro.attribute("author");
            var value = macro.attribute("value");
            var commentInfo = new CommentInfo(macro.id(), start.orElse(""), author.orElse(""), value.orElse(""));
            comments.add(commentInfo);
        }
        return comments;
    }

    private BlockRenderResult renderBlock(Block block, int x, int y, boolean highlight, Theme theme) {
        var elements = new ArrayList<SvgElement>();
        var nextY = y;
        var maxWidth = PAGE_WIDTH - 2 * PAGE_PADDING;
        switch (block) {
            case Heading(_, int level, List<Inline> inlines) -> {
                var fontSize = Math.max(18, 34 - (level * 3));
                var result = appendWrappedText(renderInlines(inlines), x, y + fontSize, fontSize, 700, theme, maxWidth);
                elements.addAll(result.elements());
                nextY = result.nextY() + 10;
            }
            case Paragraph(_, List<Inline> inlines) -> {
                var result = appendWrappedText(renderInlines(inlines),
                        x,
                        y + BODY_FONT_SIZE,
                        BODY_FONT_SIZE,
                        400,
                        theme,
                        maxWidth
                );
                elements.addAll(result.elements());
                nextY = result.nextY() + 8;
            }
            case UnorderedList(List<ListItem> items) -> {
                for (ListItem item : items) {
                    var result = appendWrappedText("• " + renderInlines(item.inlines()),
                            x,
                            nextY + BODY_FONT_SIZE,
                            BODY_FONT_SIZE,
                            400,
                            theme,
                            maxWidth
                    );
                    elements.addAll(result.elements());
                    nextY = result.nextY();
                }
                nextY += 6;
            }
            case OrderedList(List<ListItem> items) -> {
                int index = 1;
                for (ListItem item : items) {
                    var result = appendWrappedText(index + ". " + renderInlines(item.inlines()),
                            x,
                            nextY + BODY_FONT_SIZE,
                            BODY_FONT_SIZE,
                            400,
                            theme,
                            maxWidth
                    );
                    elements.addAll(result.elements());
                    nextY = result.nextY();
                    index++;
                }
                nextY += 6;
            }
            case QuoteBlock(List<Inline> inlines) -> {
                nextY += 8;
                int startY = nextY;
                var result = appendWrappedText(renderInlines(inlines),
                        x + 10,
                        nextY + BODY_FONT_SIZE,
                        BODY_FONT_SIZE,
                        400,
                        theme,
                        maxWidth - 10
                );
                elements.addAll(result.elements());
                nextY = result.nextY();
                elements.add(new SvgLine(String.valueOf(x),
                        String.valueOf(startY),
                        String.valueOf(x),
                        String.valueOf(nextY - 4),
                        "#888",
                        attr("stroke-width", 3)
                ));
            }
            case CodeBlock(String language, String content) -> {
                nextY += 8;
                int blockHeight = Math.max(LINE_HEIGHT * 2, (content.lines().toList().size() + 1) * LINE_HEIGHT);
                elements.add(new SvgRect(String.valueOf(x),
                        String.valueOf(nextY),
                        String.valueOf(PAGE_WIDTH - (PAGE_PADDING * 2)),
                        String.valueOf(blockHeight),
                        attr("fill", "#f6f8fa"),
                        attr("stroke", "#d0d7de"),
                        attr("rx", "6")
                ));
                var textResult = appendTextLine("[" + language + "]", x + 10, nextY + BODY_FONT_SIZE, 12, 600, theme);
                elements.add(textResult.element());
                nextY = textResult.nextY();
                for (String line : content.lines().toList()) {
                    var lineResult = appendTextLine(line, x + 10, nextY + 12, 13, 400, theme);
                    elements.add(lineResult.element());
                    nextY = lineResult.nextY();
                }
                nextY += 8;
            }
            case ImageBlock(String url, String _) -> {
                nextY += 8;
                elements.add(new SvgImage(String.valueOf(x), String.valueOf(nextY), "320", "120", url, attr("rx", 4)));
                nextY += 114 + LINE_HEIGHT;
            }
            case Table tbl -> {
                nextY += 8;
                var rows = tbl.rows();
                var nbRows = rows.size();
                var rowHeight = 8 + LINE_HEIGHT;
                var tblHeight = rowHeight * nbRows;
                var tblWidth = 380;
                elements.add(new SvgRect(String.valueOf(x),
                        String.valueOf(nextY),
                        String.valueOf(tblWidth),
                        String.valueOf(tblHeight),
                        attr("stroke", "black"),
                        attr("fill", "white")
                ));
                for (var row : rows) {
                    elements.add(new SvgRect(String.valueOf(x),
                            String.valueOf(nextY),
                            String.valueOf(tblWidth),
                            String.valueOf(rowHeight),
                            attr("stroke", "black"),
                            attr("fill", "white")
                    ));
                    var nextX = x;
                    var cells = row.cells();
                    var cellWidth = tblWidth / cells.size();
                    for (var _ : cells) {
                        elements.add(new SvgRect(String.valueOf(nextX),
                                String.valueOf(nextY),
                                String.valueOf(cellWidth),
                                String.valueOf(rowHeight),
                                attr("stroke", "black"),
                                attr("fill", "white")
                        ));
                        nextX += cellWidth;
                    }
                    nextY += rowHeight;
                }
            }
            default -> nextY += LINE_HEIGHT;
        }

        if (highlight) {
            var attrFill = theme.getHighlightColor().map(c -> attr("fill", c)).orElse(SvgAttribute.NONE);
            var highlightRect = new SvgRect(String.valueOf(x - 5),
                    String.valueOf(y - 2),
                    String.valueOf(PAGE_WIDTH - 2 * PAGE_PADDING + 10),
                    String.valueOf(nextY - y),
                    attrFill
            );
            elements.addFirst(highlightRect);
        }

        return new BlockRenderResult(nextY, elements);
    }


    private List<SvgElement> renderComments(Map<Integer, List<CommentInfo>> blockToComments, List<BlockPosition> blockPositions, Theme theme) {
        var elements = new ArrayList<SvgElement>();
        var commentY = BANNER_HEIGHT + PAGE_MARGIN_TOP + PAGE_PADDING;
        var strokeColor = theme.getStrokeColor();
        for (var pos : blockPositions) {
            var comments = blockToComments.get(pos.index);
            if (comments == null) continue;
            for (var c : comments) {
                var commentPadding = 10;
                var textWidth = COMMENT_WIDTH - 2 * commentPadding;
                var valueFont = getAwtFont(theme, 11, 400);
                var valueLines = wrapText(c.value, valueFont, textWidth);
                var rectHeight = 30 + (valueLines.size() * 15);
                var attrStroke = strokeColor.map(sc -> attr("stroke", sc)).orElse(SvgAttribute.NONE);
                elements.add(new SvgRect(String.valueOf(COMMENTS_LEFT),
                        String.valueOf(commentY),
                        String.valueOf(COMMENT_WIDTH),
                        String.valueOf(rectHeight),
                        attr("fill", "#f9f9f9"),
                        attr("stroke-width", "1"),
                        attr("rx", "4"),
                        attrStroke
                ));
                if (theme == Theme.GDOCS) {
                    elements.add(new SvgCircle(String.valueOf(COMMENTS_LEFT + 15),
                            String.valueOf(commentY + 20),
                            "5",
                            "#4285f4"
                    ));
                }
                var attrFF = theme.getFontFamily().map(ff -> attr("font-family", ff)).orElse(SvgAttribute.NONE);
                elements.add(new SvgText(String.valueOf(COMMENTS_LEFT + (theme == Theme.GDOCS ? 25 : 10)),
                        String.valueOf(commentY + 20),
                        "11",
                        "#333",
                        c.author,
                        attr("font-weight", "bold"),
                        attrFF
                ));

                var lineY = commentY + 35;
                for (var line : valueLines) {
                    elements.add(new SvgText(String.valueOf(COMMENTS_LEFT + 10),
                            String.valueOf(lineY),
                            "11",
                            "#666",
                            line,
                            attrFF
                    ));
                    lineY += 15;
                }

                var startX = PAGE_LEFT + PAGE_WIDTH;
                var startY = (pos.startY + pos.endY) / 2;
                var endX = COMMENTS_LEFT;
                var endY = commentY + (rectHeight / 2);
                elements.add(new SvgLine(String.valueOf(startX),
                        String.valueOf(startY),
                        String.valueOf(endX),
                        String.valueOf(endY),
                        strokeColor.orElse(""),
                        attr("stroke-width", 1),
                        attr("stroke-dasharray", 4)
                ));

                commentY += rectHeight + 10;
            }
        }
        return elements;
    }

    private Map<Integer, List<CommentInfo>> mapCommentsToBlocks(List<CommentInfo> comments) {
        Map<Integer, List<CommentInfo>> map = new TreeMap<>();
        for (CommentInfo c : comments) {
            try {
                int blockIndex = Integer.parseInt(c.start.split(",")[0]);
                map.computeIfAbsent(blockIndex, _ -> new ArrayList<>()).add(c);
            } catch (Exception ignored) {
            }
        }
        return map;
    }

    private TextLineResult appendTextLine(String line, int x, int y, int fontSize, int weight, Theme theme) {
        var text = new SvgText(String.valueOf(x),
                String.valueOf(y),
                String.valueOf(fontSize),
                "#111",
                line,
                attr("font-weight", weight),
                theme.getFontFamily().map(ff -> attr("font-family", ff)).orElse(SvgAttribute.NONE)
        );
        return new TextLineResult(text, y + LINE_HEIGHT);
    }

    private WrappedTextResult appendWrappedText(String text, int x, int y, int fontSize, int weight, Theme theme, int maxWidth) {
        var font = getAwtFont(theme, fontSize, weight);
        var lines = wrapText(text, font, maxWidth);
        var elements = new ArrayList<SvgElement>();
        var currentY = y;
        for (var line : lines) {
            var result = appendTextLine(line, x, currentY, fontSize, weight, theme);
            elements.add(result.element());
            currentY = result.nextY();
        }
        return new WrappedTextResult(currentY, elements);
    }

    private String renderInlines(List<Inline> inlines) {
        var text = new StringBuilder();
        for (var inline : inlines) {
            var string = switch (inline) {
                case Text(String value) -> value;
                case Bold(List<Inline> children) -> renderInlines(children);
                case Italic(List<Inline> children) -> renderInlines(children);
                case Link(String url, String label) -> label.isBlank() ? url : label;
                case ImageInline(String url, Map<String, String> attributes) ->
                        "[%s: %s]".formatted(attributes.getOrDefault("title", "image"), url);
                case Tab _ -> "    ";
                default -> inline.text();
            };
            text.append(string);
        }
        return text.toString();
    }

    private record CommentInfo(String id, String start, String author, String value) {
    }

    private record BlockPosition(int index, int startY, int endY) {
    }

    private record BlockRenderResult(int nextY, List<SvgElement> elements) {
    }

    private record TextLineResult(SvgText element, int nextY) {
    }

    private record WrappedTextResult(int nextY, List<SvgElement> elements) {
    }
}
