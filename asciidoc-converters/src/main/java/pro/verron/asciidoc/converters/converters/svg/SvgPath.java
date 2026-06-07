package pro.verron.asciidoc.converters.converters.svg;

/// SVG {@code <path>} element model.
record SvgPath(
        String d, String fill
)
        implements SvgElement {

    @Override
    public String markup() {
        return "path";
    }

    @Override
    public SvgAttributes attributes() {
        return new SvgAttributes(SvgAttribute.attr("d", d),
                SvgAttribute.attr("fill", fill));
    }
}
