package pro.verron.asciidoc.converters.converters.svg;

/// SVG {@code <rect>} element model.
/// Attributes are stored in a list to preserve insertion order.
public record SvgRect(
        String x, String y, String width, String height, SvgAttributes opts
)
        implements SvgElement {
    public SvgRect(
            String x,
            String y,
            String width,
            String height,
            SvgAttribute... opts
    ) {
        this(x, y, width, height, new SvgAttributes(opts));
    }

    @Override
    public String markup() {
        return "rect";
    }

    @Override
    public SvgAttributes attributes() {
        var attributes = new SvgAttributes();
        attributes.add("x", x);
        attributes.add("y", y);
        attributes.add("width", width);
        attributes.add("height", height);
        attributes.addAll(opts);
        return attributes;
    }
}
