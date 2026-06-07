package pro.verron.asciidoc.converters.converters.svg;

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
