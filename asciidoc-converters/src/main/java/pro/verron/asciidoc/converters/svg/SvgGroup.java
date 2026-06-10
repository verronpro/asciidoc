package pro.verron.asciidoc.converters.svg;

import java.util.List;

import static pro.verron.asciidoc.converters.svg.SvgAttribute.attr;

/// SVG {@code <g>} element model.
public record SvgGroup(String transform, List<SvgElement> children)
        implements SvgElement {
    @Override
    public String markup() {
        return "g";
    }

    @Override
    public SvgAttributes attributes() {
        return new SvgAttributes(attr("transform", transform));
    }
}
