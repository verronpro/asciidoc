package pro.verron.asciidoc.core;

import java.math.BigInteger;

/// Builder for constructing [Comment] instances step-by-step.
public class CommentBuilder {
    private BigInteger id;
    private int blockStart;
    private int lineStart;
    private int blockEnd;
    private int lineEnd;

    /// Constructs a [CommentBuilder] with the given comment identifier.
    ///
    /// @param id unique identifier for the comment
    public CommentBuilder(BigInteger id) {
        this.id = id;
    }

    /// Creates a new [Comment] from the current builder state.
    ///
    /// @return a new [Comment] instance
    public Comment createComment() {
        return new Comment(id, blockStart, lineStart, blockEnd, lineEnd);
    }

    /// Returns the unique identifier.
    ///
    /// @return the comment identifier
    public BigInteger getId() {
        return this.id;
    }

    /// Sets the unique identifier.
    ///
    /// @param id unique identifier as a [BigInteger]
    /// @return this builder for method chaining
    public CommentBuilder setId(BigInteger id) {
        this.id = id;
        return this;
    }

    /// Sets the starting block position.
    ///
    /// @param blockStart starting block position
    /// @return this builder for method chaining
    public CommentBuilder setBlockStart(int blockStart) {
        this.blockStart = blockStart;
        return this;
    }

    /// Sets the starting line position.
    ///
    /// @param lineStart starting line position
    /// @return this builder for method chaining
    public CommentBuilder setLineStart(int lineStart) {
        this.lineStart = lineStart;
        return this;
    }

    /// Sets the ending block position.
    ///
    /// @param blockEnd ending block position
    /// @return this builder for method chaining
    public CommentBuilder setBlockEnd(int blockEnd) {
        this.blockEnd = blockEnd;
        return this;
    }

    /// Sets the ending line position.
    ///
    /// @param lineEnd ending line position
    /// @return this builder for method chaining
    public CommentBuilder setLineEnd(int lineEnd) {
        this.lineEnd = lineEnd;
        return this;
    }
}
