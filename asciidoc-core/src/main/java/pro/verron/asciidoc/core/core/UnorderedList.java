package pro.verron.asciidoc.core.core;

import java.util.List;

/// Unordered list.
///
/// @param items list items
public record UnorderedList(List<ListItem> items)
        implements Block {
    @Override
    public int size() {
        return items.size();
    }
}
