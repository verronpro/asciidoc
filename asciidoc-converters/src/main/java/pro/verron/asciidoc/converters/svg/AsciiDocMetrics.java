package pro.verron.asciidoc.converters.svg;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static java.lang.ThreadLocal.withInitial;

/// Utility class for text measurement and wrapping in AsciiDoc previews.
public final class AsciiDocMetrics {

    private static final ThreadLocal<Graphics2D> GRAPHICS = withInitial(() -> {
        var image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        return image.createGraphics();
    });

    private AsciiDocMetrics() {
        throw new UnsupportedOperationException("Utility class");
    }

    /// Wraps text to fit within a maximum width.
    ///
    /// @param text     text to wrap
    /// @param font     font used for measurement
    /// @param maxWidth maximum width in pixels
    /// @return list of wrapped lines
    public static List<String> wrapText(String text, Font font, int maxWidth) {
        if (text.isEmpty()) return List.of("");

        var graphics2D = GRAPHICS.get();
        var metrics = graphics2D.getFontMetrics(font);
        var lines = new ArrayList<String>();
        var words = text.split("\\s+");
        var currentLine = new DocLine(metrics, maxWidth);

        for (var word : words) {
            if (currentLine.canFit(word)) currentLine.add(word);
            else {
                if (!currentLine.isEmpty()) lines.add(currentLine.line());
                currentLine = new DocLine(metrics, maxWidth);
                lines.add(word);
            }
        }
        if (!currentLine.isEmpty()) lines.add(currentLine.line());
        return lines;
    }
}
