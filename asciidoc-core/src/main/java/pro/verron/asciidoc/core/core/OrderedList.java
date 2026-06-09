package pro.verron.asciidoc.core.core;

import java.util.List;

/// Ordered (numbered) list.
///
/// @param items list items
public record OrderedList(List<ListItem> items)
        implements Block {
    @Override
    public int size() {
        return items.size();
    }
}
