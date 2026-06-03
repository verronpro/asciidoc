package pro.verron.asciidoc.core.core;

/// Inline tab marker to be rendered as a DOCX tab stop.
public record Tab()
        implements Inline {
    @Override
    public String text() {
        return "\t";
    }
}
