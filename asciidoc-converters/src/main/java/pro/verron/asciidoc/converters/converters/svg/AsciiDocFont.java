package pro.verron.asciidoc.converters.converters.svg;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/// Utility class for font management in AsciiDoc previews.
public final class AsciiDocFont {
    private static final Map<String, Font> FONT_CACHE = new HashMap<>();

    private AsciiDocFont() {
        throw new UnsupportedOperationException("Utility class");
    }

    /// Returns an AWT Font for metrics calculations.
    ///
    /// @param theme    target editor theme
    /// @param fontSize font size
    /// @param weight   font weight (e.g. 400, 700)
    ///
    /// @return AWT Font
    public static Font getAwtFont(Theme theme, int fontSize, int weight) {
        var key = String.format("%s-%d-%d", theme, fontSize, weight);
        var primaryFont = theme.getPrimaryFont();
        var style = (weight >= 700) ? Font.BOLD : Font.PLAIN;
        return FONT_CACHE.computeIfAbsent(key,
                _ -> new Font(primaryFont.orElse(null), style, fontSize));
    }

}
