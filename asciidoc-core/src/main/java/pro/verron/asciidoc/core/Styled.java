package pro.verron.asciidoc.core;

import java.util.List;

import static java.util.stream.Collectors.joining;

/// Inline fragment styled with a specific role.
///
/// @param role     the style role
/// @param children the styled content
public record Styled(String role, List<Inline> children) implements Inline {
    @Override
    public String text() {
        return children.stream().map(Inline::text).collect(joining());
    }
}
