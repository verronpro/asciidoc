package pro.verron.asciidoc.core;

/// Block-level image.
///
/// @param url     image URL
/// @param altText alternative text
public record ImageBlock(String url, String altText)
        implements Block {
    @Override
    public int size() {
        return 1;
    }
}
