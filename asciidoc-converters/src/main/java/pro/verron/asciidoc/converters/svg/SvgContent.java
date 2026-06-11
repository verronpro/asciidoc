package pro.verron.asciidoc.converters.svg;

/// Raw text content inside an SVG element.
/// Special XML characters (`& < > " '`) are escaped on serialization.
///
/// @param content the text content
public record SvgContent(String content)
        implements SvgElement {

    @Override
    public String serialize() {
        return content.replace("&", "&amp;")
                      .replace("<", "&lt;")
                      .replace(">", "&gt;")
                      .replace("\"", "&quot;")
                      .replace("'", "&apos;")
                      .replaceAll("\\p{Cntrl}", "")
                      .stripTrailing();
    }
}
