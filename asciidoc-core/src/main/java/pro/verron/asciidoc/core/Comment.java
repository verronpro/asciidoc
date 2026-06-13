package pro.verron.asciidoc.core;

import java.math.BigInteger;

/// Represents a comment with positional information.
///
/// @param id         unique identifier for the comment
/// @param blockStart starting block position
/// @param lineStart  starting line position
/// @param blockEnd   ending block position
/// @param lineEnd    ending line position
public record Comment(
        BigInteger id,
        int blockStart,
        int lineStart,
        int blockEnd,
        int lineEnd
) {
}
