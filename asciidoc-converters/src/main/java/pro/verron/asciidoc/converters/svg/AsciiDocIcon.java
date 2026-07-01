package pro.verron.asciidoc.converters.svg;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.emptyList;

/// Provides SVG paths for icons used in simulated editor interfaces.
/// Icons are sourced from Bootstrap Icons (MIT License).
///
/// Icon path data is lazily loaded from individual SVG resource files
/// in the {@code icons/} package directory and cached on first access.
public final class AsciiDocIcon {

    private static final ConcurrentHashMap<Icon, List<String>> CACHE = new ConcurrentHashMap<>();

    private AsciiDocIcon() {
        throw new UnsupportedOperationException("Utility class");
    }

    /// Finds an icon and returns it as an SVG group model.
    /// The icon's path data is loaded from its resource file on first
    /// access and cached for subsequent calls.
    ///
    /// @param icon  the icon identifier
    /// @param x     x coordinate
    /// @param y     y coordinate
    /// @param size  icon size (width and height)
    /// @param color icon color
    /// @return an [Optional] containing the [SvgGroup] for the icon,
    ///         or empty if the resource could not be found
    public static Optional<SvgGroup> findIcon(Icon icon, int x, int y, int size, String color) {
        var paths = CACHE.computeIfAbsent(icon, AsciiDocIcon::loadPaths);
        if (paths.isEmpty()) return Optional.empty();

        var transform = String.format(Locale.ROOT, "translate(%d, %d) scale(%f)", x, y, size / 16.0);
        var elements = paths.stream().map(d -> new SvgPath(d, color)).map(SvgElement.class::cast).toList();
        return Optional.of(new SvgGroup(transform, elements));
    }

    private static List<String> loadPaths(Icon icon) {
        var resource = AsciiDocIcon.class.getResource(icon.resourceName());
        if (resource == null) return emptyList();
        try (var in = resource.openStream()) {
            return extractAllPathData(new String(in.readAllBytes()));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read icon resource: " + icon, e);
        }
    }

    /// Extracts all {@code d} attribute values from {@code <path>}
    /// elements in an SVG document.
    private static List<String> extractAllPathData(String svg) {
        var paths = new ArrayList<String>();
        int from = 0;
        while (true) {
            var dStart = svg.indexOf("d=\"", from);
            if (dStart < 0) break;
            var valueStart = dStart + 3;
            var valueEnd = svg.indexOf('"', valueStart);
            if (valueEnd < 0) break;
            paths.add(svg.substring(valueStart, valueEnd).replaceAll("\\s+", " ").trim());
            from = valueEnd + 1;
        }
        return paths;
    }

}
