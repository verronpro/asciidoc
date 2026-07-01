package pro.verron.asciidoc.core;

import java.util.List;

/// Block-level quotation.
///
/// @param inlines inline fragments
public record QuoteBlock(List<Inline> inlines) implements Block {
    /// Constructs a [QuoteBlock] with the given inline fragments.
    ///
    /// @param inlines inline fragments
    public QuoteBlock(List<Inline> inlines) {
        this.inlines = List.copyOf(inlines);
    }

    @Override
    public int size() {
        return 1;
    }
}
