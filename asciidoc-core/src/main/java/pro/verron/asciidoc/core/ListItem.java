package pro.verron.asciidoc.core;

import java.util.List;

/// List item.
///
/// @param inlines inline fragments
public record ListItem(List<Inline> inlines) {
    /// Constructs a [ListItem] with the given inline fragments.
    ///
    /// @param inlines inline fragments
    public ListItem(List<Inline> inlines) {
        this.inlines = List.copyOf(inlines);
    }

    /// Returns the text of the first inline fragment in this list item.
    ///
    /// @return the text of the leading inline fragment
    public String text() {
        return inlines().getFirst().text();
    }
}
