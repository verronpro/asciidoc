package pro.verron.asciidoc.core;

/// Inline fragment inside a paragraph or heading.
///
/// Each inline provides its plain-text representation via [#text()].
public sealed interface Inline permits Bold, ImageInline, MacroInline, Italic, Link, Styled, Sub, Sup, Tab, Text {
    /// Returns the text of the inline fragment.
    ///
    /// @return text
    String text();
}
