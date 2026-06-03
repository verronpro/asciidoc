package pro.verron.asciidoc.core.core;

/// Code block.
///
/// @param language language
/// @param content code content
public record CodeBlock(String language, String content)
        implements Block {
    @Override
    public int size() {
        return 1;
    }
}
