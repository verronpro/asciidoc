package pro.verron.asciidoc.converters.svg;

/// SVG {@code <circle>} element model.
///
/// @param cx   center x coordinate
/// @param cy   center y coordinate
/// @param r    radius
/// @param fill fill color
public record SvgCircle(String cx, String cy, String r, String fill)
        implements SvgElement {

    @Override
    public String markup() {return "circle";}

    @Override
    public SvgAttributes attributes() {
        var attributes = new SvgAttributes();
        attributes.add("cx", cx);
        attributes.add("cy", cy);
        attributes.add("r", r);
        attributes.add("fill", fill);
        return attributes;
    }
}
