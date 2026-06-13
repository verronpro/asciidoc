package pro.verron.asciidoc.core;

import java.util.List;

/// Unordered (bulleted) list.
///
/// @param items list items
public record UnorderedList(List<ListItem> items)
        implements Block {
    @Override
    public int size() {
        return items.size();
    }
}
