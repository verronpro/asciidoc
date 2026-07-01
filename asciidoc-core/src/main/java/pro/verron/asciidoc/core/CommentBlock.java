package pro.verron.asciidoc.core;

/// Represents a comment line in the AsciiDoc document model.
///
/// A comment line is a block-level element that does not contribute visible content.
/// Its size is always zero.
///
/// @param comment the text of the comment line
public record CommentBlock(String comment) implements Block {
    @Override
    public int size() {
        return 0;
    }
}
