package pro.verron.asciidoc.core.core;

/// Inline tab marker, rendered as a tab stop.
///
/// Yields a `"\t"` character as plain text.
public record Tab()
        implements Inline {
    @Override
    public String text() {
        return "\t";
    }
}
