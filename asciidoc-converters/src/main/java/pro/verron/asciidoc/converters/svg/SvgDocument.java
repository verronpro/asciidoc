package pro.verron.asciidoc.converters.svg;

import java.util.List;
import java.util.Locale;

import static pro.verron.asciidoc.converters.svg.SvgAttribute.attr;

/// SVG document root model ({@code <svg>} element).
///
/// @param width    viewport width in pixels
/// @param height   viewport height in pixels
/// @param children child elements
public record SvgDocument(int width, int height, List<SvgElement> children)
        implements SvgElement {

    @Override
    public String markup() {
        return "svg";
    }

    @Override
    public SvgAttributes attributes() {
        var viewbox = String.format(Locale.ROOT, "0 0 %d %d", width, height);
        return new SvgAttributes(attr("xmlns", "http://www.w3.org/2000/svg"),
                attr("width", String.valueOf(width)),
                attr("height", String.valueOf(height)),
                attr("viewBox", viewbox));
    }
}
