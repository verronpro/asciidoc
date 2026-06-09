package pro.verron.asciidoc.core.core;

/// Marker interface for document blocks.
///
/// Each block reports its content size via [#size()].
public sealed interface Block
        permits QuoteBlock, Break, CodeBlock, CommentBlock, Heading,
        ImageBlock, MacroBlock, OpenBlock, OrderedList, Paragraph, Table,
        UnorderedList {
    int size();
}
