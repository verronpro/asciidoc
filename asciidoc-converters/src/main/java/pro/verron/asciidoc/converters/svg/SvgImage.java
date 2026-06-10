package pro.verron.asciidoc.converters.svg;

/// SVG {@code <image>} element model.
public record SvgImage(
        String x,
        String y,
        String width,
        String height,
        String href,
        SvgAttributes opts
)
        implements SvgElement {

    public SvgImage(
            String x,
            String y,
            String width,
            String height,
            String href,
            SvgAttribute... opts
    ) {
        this(x, y, width, height, href, new SvgAttributes(opts));
    }

    @Override
    public String markup() {
        return "image";
    }

    @Override
    public SvgAttributes attributes() {
        var attributes = new SvgAttributes();
        attributes.add("x", x);
        attributes.add("y", y);
        attributes.add("width", width);
        attributes.add("height", height);
        attributes.add("href", href);
        attributes.addAll(opts);
        return attributes;
    }
}
