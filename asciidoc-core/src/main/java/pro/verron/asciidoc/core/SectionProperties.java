package pro.verron.asciidoc.core;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

/// Utilities for the `[section-properties]` block: parses and renders the
/// key/value lines carrying a section's layout (page size, margins, …).
///
/// Two equivalent line formats are accepted and may be mixed freely:
///
/// ```
/// pgSz={w=11906, h=16838}
/// pgSz.w=11906
/// pgSz.h=16838
/// ```
///
/// Lines are rendered canonically as nested brace maps with keys sorted
/// alphabetically, so a parsed-then-rendered model is a fixed point.
public final class SectionProperties {
    private static final Pattern INTEGER = Pattern.compile("-?\\d+");
    private static final String ROLE = "section-properties";

    private SectionProperties() {
    }

    /// The block role identifying a `[section-properties]` block.
    public static String role() {
        return ROLE;
    }

    /// Parses property lines into a nested ordered map. Values may be nested
    /// brace maps, dotted paths, integers, booleans, or plain strings.
    ///
    /// @param lines the raw property lines
    ///
    /// @return the parsed properties, as nested ordered maps
    public static Map<String, Object> parse(List<String> lines) {
        var map = new TreeMap<String, Object>();
        for (var line : lines) {
            var trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
            var eq = trimmed.indexOf('=');
            if (eq <= 0)
                throw new IllegalArgumentException(
                        "Invalid [section-properties] line, expected key=value: '%s'".formatted(line));
            insert(map, List.of(trimmed.substring(0, eq)
                                       .trim()
                                       .split("\\.")), parseValue(trimmed.substring(eq + 1)
                                                                                .trim()));
        }
        return map;
    }

    /// Renders a parsed properties map back to canonical lines: one line per
    /// top-level key, nested values as brace maps, keys sorted alphabetically.
    ///
    /// @param map the parsed properties
    ///
    /// @return the canonical property lines
    public static List<String> render(Map<String, Object> map) {
        var lines = new ArrayList<String>();
        for (var entry : map.entrySet()) lines.add(entry.getKey() + "=" + renderValue(entry.getValue()));
        return lines;
    }

    private static void insert(TreeMap<String, Object> map, List<String> path, Object value) {
        var head = path.getFirst();
        if (path.size() == 1) {
            var previous = map.put(head, value);
            if (previous != null && !previous.equals(value))
                throw new IllegalArgumentException(
                        "Conflicting values for property '%s': '%s' vs '%s'".formatted(head, previous, value));
            return;
        }
        Object current = map.get(head);
        TreeMap<String, Object> nested;
        if (current instanceof TreeMap<?, ?> existing) {
            @SuppressWarnings("unchecked") var typed = (TreeMap<String, Object>) existing;
            nested = typed;
        } else {
            if (current != null)
                throw new IllegalArgumentException(
                        "Conflicting values for property '%s': '%s'".formatted(head, current));
            nested = new TreeMap<>();
            map.put(head, nested);
        }
        insert(nested, path.subList(1, path.size()), value);
    }

    private static Object parseValue(String value) {
        if (value.startsWith("{")) return parseBraceMap(value);
        if (INTEGER.matcher(value)
                   .matches()) return new BigInteger(value);
        if ("true".equals(value) || "false".equals(value)) return Boolean.valueOf(value);
        return value;
    }

    private static TreeMap<String, Object> parseBraceMap(String value) {
        if (!value.startsWith("{") || !value.endsWith("}"))
            throw new IllegalArgumentException("Malformed nested property value: '%s'".formatted(value));
        var map = new TreeMap<String, Object>();
        for (var part : splitTopLevel(value.substring(1, value.length() - 1))) {
            var eq = part.indexOf('=');
            if (eq <= 0)
                throw new IllegalArgumentException("Malformed nested property entry: '%s'".formatted(part));
            var key = part.substring(0, eq)
                          .trim();
            insert(map, List.of(key.split("\\.")), parseValue(part.substring(eq + 1)
                                                                   .trim()));
        }
        return map;
    }

    private static List<String> splitTopLevel(String content) {
        var parts = new ArrayList<String>();
        var depth = 0;
        var start = 0;
        for (int i = 0; i < content.length(); i++) {
            switch (content.charAt(i)) {
                case '{' -> depth++;
                case '}' -> depth--;
                case ',' -> {
                    if (depth == 0) {
                        parts.add(content.substring(start, i)
                                         .trim());
                        start = i + 1;
                    }
                }
                default -> {
                }
            }
        }
        var last = content.substring(start)
                          .trim();
        if (!last.isEmpty()) parts.add(last);
        return parts;
    }

    private static String renderValue(Object value) {
        if (value instanceof Map<?, ?> nested) {
            var joined = nested.entrySet()
                               .stream()
                               .map(e -> e.getKey() + "=" + renderValue(e.getValue()))
                               .reduce((a, b) -> a + ", " + b)
                               .orElse("");
            return "{%s}".formatted(joined);
        }
        return String.valueOf(value);
    }
}
