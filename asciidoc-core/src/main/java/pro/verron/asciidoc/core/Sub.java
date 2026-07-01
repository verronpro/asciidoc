package pro.verron.asciidoc.core;

import java.util.List;

import static java.util.stream.Collectors.joining;

/// Subscript inline element.
///
/// @param children inline fragments within this subscript element
public record Sub(List<Inline> children) implements Inline {

    /// Constructs a [Sub] with the given nested inlines.
    ///
    /// @param children inline fragments to include as children
    public Sub(List<Inline> children) {
        this.children = List.copyOf(children);
    }

    @Override
    public String text() {
        return children.stream().map(Inline::text).collect(joining());
    }
}
