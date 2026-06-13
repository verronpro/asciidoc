package pro.verron.asciidoc.core;

import java.util.List;

/// Inline macro in an AsciiDoc document.
///
/// @param name the macro name
/// @param id   an identifier for the macro
/// @param list content strings
public record MacroInline(String name, String id, List<String> list)
        implements Inline {

    @Override
    public String text() {
        return String.join("", list);
    }
}
