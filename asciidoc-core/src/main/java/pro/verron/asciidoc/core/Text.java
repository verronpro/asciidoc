package pro.verron.asciidoc.core;

/// Plain text fragment.
///
/// @param text text content
public record Text(String text) implements Inline {
}
