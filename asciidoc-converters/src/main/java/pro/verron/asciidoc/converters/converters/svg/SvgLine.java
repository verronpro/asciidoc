package pro.verron.asciidoc.converters.converters.svg;

/// SVG {@code <line>} element model.
public record SvgLine(
        String x1,
        String y1,
        String x2,
        String y2,
        String stroke,
        SvgAttributes opts
)
        implements SvgElement {
    public SvgLine(
            String x1,
            String y1,
            String x2,
            String y2,
            String stroke,
            SvgAttribute... opts
    ) {
        this(x1, y1, x2, y2, stroke, new SvgAttributes(opts));
    }

    @Override
    public String markup() {
        return "line";
    }

    @Override
    public SvgAttributes attributes() {
        var attributes = new SvgAttributes();
        attributes.add("x1", x1);
        attributes.add("y1", y1);
        attributes.add("x2", x2);
        attributes.add("y2", y2);
        attributes.add("stroke", stroke);
        attributes.addAll(opts);
        return attributes;
    }
}
