package pro.verron.asciidoc.core;

/// Fenced code block.
///
/// @param language source language identifier
/// @param content code content
public record CodeBlock(String language, String content)
        implements Block {
    @Override
    public int size() {
        return 1;
    }
}
