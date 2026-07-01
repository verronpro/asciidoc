package pro.verron.asciidoc.core;

import java.util.List;

import static java.util.stream.Collectors.joining;

/// Superscript inline element.
///
/// @param children inline fragments within this superscript element
public record Sup(List<Inline> children) implements Inline {
    /// Constructs a [Sup] with the given nested inlines.
    ///
    /// @param children inline fragments to include as children
    public Sup(List<Inline> children) {
        this.children = List.copyOf(children);
    }

    @Override
    public String text() {
        return children.stream().map(Inline::text).collect(joining());
    }
}
