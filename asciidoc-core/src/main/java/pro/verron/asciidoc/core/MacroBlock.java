package pro.verron.asciidoc.core;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/// Block-level macro in an AsciiDoc document.
///
/// @param header an ordered list of strings associated with the macro
/// @param name   the macro name
/// @param id     the unique identifier for the macro
public record MacroBlock(List<String> header, String name, String id)
        implements Block {

    @Override
    public int size() {
        return 1;
    }

    /// Returns the value of the named attribute from the header.
    ///
    /// @param name attribute name
    ///
    /// @return attribute value, or `null` if absent
    public Optional<String> attribute(String name) {
        return Optional.ofNullable(attributes().get(name));
    }

    /// Parses the header list into a key-value attribute map.
    ///
    /// @return attribute map parsed from `key="value"` entries in the header
    public Map<String, String> attributes() {
        var attributeMap = new TreeMap<String, String>();
        for (var attr : header) {
            var trimmed = attr.trim();
            var keyValueSeparator = "=";
            if (!trimmed.contains(keyValueSeparator)) continue;
            var split = trimmed.split(keyValueSeparator);
            attributeMap.put(split[0], split[1].replace("\"", ""));
        }
        return attributeMap;
    }
}
