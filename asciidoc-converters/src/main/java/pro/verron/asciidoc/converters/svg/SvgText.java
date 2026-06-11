package pro.verron.asciidoc.converters.svg;

import java.util.List;

/// SVG {@code <text>} element model.
///
/// @param x        x coordinate
/// @param y        y coordinate
/// @param fontSize font size
/// @param fill     fill color
/// @param content  text content
/// @param opts     additional SVG attributes
public record SvgText(
        String x,
        String y,
        String fontSize,
        String fill,
        String content,
        SvgAttributes opts
)
        implements SvgElement {

    /// Constructs an [SvgText] element with variable attributes.
    ///
    /// @param x        x coordinate
    /// @param y        y coordinate
    /// @param fontSize font size
    /// @param fill     fill color
    /// @param content  text content
    /// @param opts     additional SVG attributes
    public SvgText(
            String x,
            String y,
            String fontSize,
            String fill,
            String content,
            SvgAttribute... opts
    ) {
        this(x, y, fontSize, fill, content, new SvgAttributes(opts));
    }

    @Override
    public String markup() {
        return "text";
    }

    @Override
    public SvgAttributes attributes() {
        var attrs = new SvgAttributes();
        attrs.add("x", x());
        attrs.add("y", y());
        attrs.add("font-size", fontSize());
        attrs.add("fill", fill());
        attrs.addAll(opts);
        return attrs;
    }

    @Override
    public List<SvgElement> children() {
        return List.of(new SvgContent(content()));
    }
}
