package pro.verron.asciidoc.core;

import java.util.List;

/// Open block, a container for grouping other blocks.
///
/// The size is the sum of the sizes of its content blocks.
///
/// @param header  metadata or informational content about the block
/// @param content blocks grouped by this open block
/// @see Block
public record OpenBlock(List<String> header, List<? extends Block> content) implements Block {
    @Override
    public int size() {
        return content.stream().mapToInt(Block::size).sum();
    }
}
