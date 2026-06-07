package pro.verron.asciidoc.core.core;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/// Represents a macro block in an AsciiDoc document, which is a specialized
/// block
/// containing a unique identifier, a name, and a list of associated data.
///
/// The [MacroBlock] is immutable and implements the [Block] interface.
/// It provides a concrete implementation for determining the size of the
/// block.
///
/// @param name the name of the macro block
/// @param id   the unique identifier for the macro block
/// @param list an ordered list of strings associated with the macro block
public record MacroBlock(String name, String id, List<String> list)
        implements Block {

    public Map<String, String> attributes() {
        var attributeMap = new TreeMap<String, String>();
        for (var attr : list) {
            var trimmed = attr.trim();
            var keyValueSeparator = "=";
            if (!trimmed.contains(keyValueSeparator)) continue;
            var split = trimmed.split(keyValueSeparator);
            attributeMap.put(split[0], split[1].replace("\"", ""));
        }

        return attributeMap;
    }

    @Override
    public int size() {
        return 1;
    }
}
