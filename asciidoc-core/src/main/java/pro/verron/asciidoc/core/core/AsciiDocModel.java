package pro.verron.asciidoc.core.core;

import java.util.*;

import static java.util.Objects.requireNonNull;

/// Represents a minimal in-memory model of an AsciiDoc document.
///
/// This model intentionally supports a compact subset sufficient for rendering
/// to WordprocessingML and JavaFX Scene: -
/// Headings (levels 1..6) using leading '=' markers - Paragraphs separated by
/// blank lines - Inline emphasis for bold
/// and italic using AsciiDoc-like markers: *bold*, _italic_
public final class AsciiDocModel {
    private final List<Block> blocks;
    private final Map<String, String> attributes;

    private AsciiDocModel(Map<String, String> attributes, List<Block> blocks) {
        requireNonNull(attributes);
        requireNonNull(blocks);
        this.blocks = List.copyOf(blocks);
        this.attributes = Map.copyOf(attributes);
    }

    /// Creates a new [AsciiDocModel] from the provided blocks.
    ///
    /// @param blocks ordered content blocks
    ///
    /// @return immutable AsciiDocModel
    public static AsciiDocModel of(List<Block> blocks) {
        return of(Map.of(), blocks);
    }

    /// Creates a new [AsciiDocModel] from the provided blocks and attributes.
    ///
    /// @param attributes document attributes
    /// @param blocks     ordered content blocks
    ///
    /// @return immutable AsciiDocModel
    public static AsciiDocModel of(
            Map<String, String> attributes,
            List<Block> blocks
    ) {
        requireNonNull(blocks, "blocks");
        requireNonNull(attributes, "attributes");
        return new AsciiDocModel(new HashMap<>(attributes),
                new ArrayList<>(blocks));
    }

    /// Creates a new [AsciiDocModel] from the provided blocks and attributes.
    ///
    /// @param attributes document attributes
    /// @param blocks     ordered content blocks
    ///
    /// @return immutable AsciiDocModel
    public static AsciiDocModel of(
            Map<String, String> attributes,
            Block... blocks
    ) {
        return of(attributes, Arrays.asList(blocks));
    }

    /// Creates a new [AsciiDocModel] from the provided blocks.
    ///
    /// @param blocks ordered content blocks
    ///
    /// @return immutable AsciiDocModel
    public static AsciiDocModel of(Block... blocks) {
        return of(Map.of(), Arrays.asList(blocks));
    }

    /// Returns the ordered list of blocks comprising the document.
    ///
    /// @return immutable list of blocks
    public List<Block> getBlocks() {
        return blocks;
    }

    /// Returns the document attributes.
    ///
    /// @return immutable map of attributes
    public Map<String, String> getAttributes() {
        return attributes;
    }
}
