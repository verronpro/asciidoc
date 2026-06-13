package pro.verron.asciidoc.core;

/// Marker interface for document blocks.
///
/// Each block reports its content size via [#size()].
public sealed interface Block
        permits QuoteBlock, Break, CodeBlock, CommentBlock, Heading,
        ImageBlock, MacroBlock, OpenBlock, OrderedList, Paragraph, Table,
        UnorderedList {

    /// Returns the content size of this block.
    ///
    /// @return the number of content units in this block
    int size();
}
