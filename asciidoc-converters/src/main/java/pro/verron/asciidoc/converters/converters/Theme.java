package pro.verron.asciidoc.converters.converters;

import java.awt.*;
import java.util.Optional;

public enum Theme {
    NONE,
    WORD,
    GDOCS,
    LIBRE;

    Optional<String> getStrokeColor() {
        return switch (this) {
            case NONE -> Optional.empty();
            case WORD -> Optional.of("#ffc000");
            case GDOCS -> Optional.of("#0b57d0");
            case LIBRE -> Optional.of("#808080");
        };
    }

    Optional<String> getHighlightColor() {
        return switch (this) {
            case NONE -> Optional.empty();
            case WORD -> Optional.of("#fff2cc");
            case GDOCS -> Optional.of("#c2e7ff");
            case LIBRE -> Optional.of("#ffff00");
        };
    }

    // Background
    Optional<String> getBgColor() {
        return switch (this) {
            case NONE -> Optional.empty();
            case WORD -> Optional.of("#e6e6e6");
            case GDOCS -> Optional.of("#f8f9fa");
            case LIBRE -> Optional.of("#dfdfdf");
        };
    }

    Optional<String>  getAwtFallbacks() {
        return switch (this) {
            case NONE -> Optional.empty();
            case WORD, GDOCS -> Optional.of(Font.SANS_SERIF);
            case LIBRE -> Optional.of(Font.SERIF);
        };
    }

    /// Returns the SVG font-family string for a given theme.
    ///
    /// @return font-family string
    Optional<String> getFontFamily() {
        return switch (this) {
            case NONE -> Optional.empty();
            case WORD -> Optional.of("Calibri, 'Carlito', 'Arial', sans-serif");
            case GDOCS -> Optional.of("Arial, 'Arimo', sans-serif");
            case LIBRE -> Optional.of("'Liberation Serif', 'Tinos', 'Times "
                                      + "New Roman', serif");
        };
    }

    Optional<String> getPrimaryFont() {
        return switch (this) {
            case NONE -> Optional.empty();
            case WORD -> Optional.of("Calibri");
            case GDOCS -> Optional.of("Arial");
            case LIBRE -> Optional.of("Liberation Serif");
        };
    }
}
