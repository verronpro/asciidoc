package pro.verron.asciidoc.core;

/// Marks the end of a document section, written as a standalone `[section]`
/// line.
///
/// The content preceding this block forms one Word section; the content
/// following it starts a new section. A section with no `[header]`/`[footer]`
/// declaration inherits the previous section's ones, matching Word's
/// per-reference-kind inheritance ("link to previous") semantics.
public record SectionBreak() implements Block {
    @Override
    public int size() {
        return 0;
    }
}
