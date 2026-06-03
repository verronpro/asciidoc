package pro.verron.asciidoc.core.core;

/// Text fragment.
///
/// @param text text
public record Text(String text)
        implements Inline {}
