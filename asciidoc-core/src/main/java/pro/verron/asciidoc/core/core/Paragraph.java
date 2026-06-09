package pro.verron.asciidoc.core.core;

import java.util.List;

import static java.util.Collections.emptyList;

/// Paragraph block.
///
/// @param header  AsciiDoc block headers containing optional information
/// @param inlines inline fragments
public record Paragraph(List<String> header, List<Inline> inlines)
        implements Block {

    /// Constructs a [Paragraph] without block headers.
    ///
    /// @param inlines inline fragments
    public Paragraph(List<Inline> inlines) {
        this(emptyList(), inlines);
    }

    /// Constructs a [Paragraph] with block headers.
    ///
    /// @param header  header elements, if any
    /// @param inlines inline fragments
    public Paragraph(List<String> header, List<Inline> inlines) {
        this.header = List.copyOf(header);
        this.inlines = List.copyOf(inlines);
    }

    @Override
    public int size() {
        return 1;
    }
}
