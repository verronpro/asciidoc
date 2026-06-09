package pro.verron.asciidoc.core.core;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/// Table cell containing block-level content.
public class Cell {
    public final @Nullable String style;
    private final List<Block> blocks;

    /// Constructs a [Cell] without a style.
    ///
    /// @param blocks cell content blocks
    public Cell(List<Block> blocks) {
        this(blocks, null);
    }

    /// Constructs a [Cell] with an optional style.
    ///
    /// @param blocks cell content blocks
    /// @param style  optional cell style, or `null`
    public Cell(List<Block> blocks, @Nullable String style) {
        this.blocks = List.copyOf(blocks);
        this.style = style;
    }

    /// Creates a [Cell] by wrapping [Inline] elements into a [Paragraph].
    ///
    /// @param inlines inline elements to wrap into a [Paragraph]
    /// @return a [Cell] containing the specified inlines as a single [Paragraph]
    public static Cell ofInlines(List<Inline> inlines) {
        return new Cell(List.of(new Paragraph(inlines)));
    }

    public List<Block> blocks() {
        return blocks;
    }

    public Optional<String> style() {
        return Optional.ofNullable(style);
    }
}
