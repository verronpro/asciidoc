package pro.verron.asciidoc.converters.svg;

import java.awt.*;

/// Mutable helper for building a single line of text that must not exceed
/// a given pixel width, as measured by an AWT [FontMetrics].
final class DocLine {
    private final FontMetrics metrics;
    private final int maxWidth;
    private final StringBuilder line;

    DocLine(FontMetrics metrics, int maxWidth) {
        this.metrics = metrics;
        this.maxWidth = maxWidth;
        this.line = new StringBuilder();
    }

    public boolean canFit(String word) {
        if (line.isEmpty()) return metrics.stringWidth(word) <= maxWidth;
        return metrics.stringWidth(line + " " + word) <= maxWidth;
    }

    public DocLine add(String word) {
        if (!line.isEmpty()) line.append(" ");
        line.append(word);
        return this;
    }

    public boolean isEmpty() {
        return line.isEmpty();
    }

    public String line() {
        return line.toString();
    }
}
