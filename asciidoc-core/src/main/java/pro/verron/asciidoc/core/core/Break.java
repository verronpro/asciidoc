package pro.verron.asciidoc.core.core;

/// Represents a line break in a document.
///
/// A marker record with a fixed size of zero, indicating no content spans across the break.
public record Break()
        implements Block {
    @Override
    public int size() {
        return 0;
    }
}
