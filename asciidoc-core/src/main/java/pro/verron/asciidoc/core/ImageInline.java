package pro.verron.asciidoc.core;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/// Inline image.
///
/// @param path image path
/// @param map alternative text
public record ImageInline(String path, Map<String, String> map)
        implements Inline {

    /// Constructs an [ImageInline] with the specified path and attributes.
    ///
    /// @param path the path to the image
    /// @param map  a mapping of alternative text attributes
    public ImageInline(String path, Map<String, String> map) {
        this.path = path;
        this.map = Collections.unmodifiableMap(new TreeMap<>(map));
    }

    @Override
    public String text() {
        return path;
    }
}
