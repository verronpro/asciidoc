package pro.verron.asciidoc.core;

/// Hyperlink inline.
///
/// @param url  link URL
/// @param text link display text
public record Link(String url, String text) implements Inline {
}
