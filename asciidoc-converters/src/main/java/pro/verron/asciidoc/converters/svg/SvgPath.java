package pro.verron.asciidoc.converters.svg;

/// SVG {@code <path>} element model.
record SvgPath(String d, String fill) implements SvgElement {

    @Override
    public String markup() {
        return "path";
    }

    @Override
    public SvgAttributes attributes() {
        var dAttr = SvgAttribute.attr("d", d);
        var fillAttr = SvgAttribute.attr("fill", fill);
        return new SvgAttributes(dAttr, fillAttr);
    }
}
