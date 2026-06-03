package pro.verron.asciidoc.core.core;

/// Link inline.
///
/// @param url link URL
/// @param text link text
public record Link(String url, String text)
        implements Inline {}
