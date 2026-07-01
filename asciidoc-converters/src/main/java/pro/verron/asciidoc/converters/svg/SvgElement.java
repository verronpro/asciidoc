package pro.verron.asciidoc.converters.svg;

import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;

import static java.lang.String.format;

/// Sealed interface for SVG element model objects.
/// Each subtype represents a specific SVG element type.
sealed public interface SvgElement permits SvgCircle, SvgContent, SvgDocument, SvgGroup, SvgImage, SvgLine, SvgPath, SvgRect, SvgText {

    /// Serializes this element and its children into an SVG markup string.
    ///
    /// @return the SVG markup string
    default String serialize() {
        var locale = Locale.ROOT;
        var attrs = new StringJoiner(" ");
        for (var attr : attributes()) {
            attrs.add(attr.serialize());
        }

        if (children().isEmpty()) {
            var format = "<%1s %2s/>";
            return format(locale, format, markup(), attrs);
        }

        var children = new StringJoiner("\n", "\n", "\n");
        for (var child : children()) children.add(child.serialize());

        var format = "<%1$s %2$s>%3$s</%1$s>";
        return format(locale, format, markup(), attrs, children);

    }

    /// Returns the SVG tag name for this element.
    ///
    /// @return the SVG tag name
    default String markup() {
        throw new UnsupportedOperationException();
    }

    /// Returns the SVG attributes for this element.
    ///
    /// @return the SVG attributes
    default SvgAttributes attributes() {
        return new SvgAttributes();
    }

    /// Returns the child elements of this element.
    ///
    /// @return the child elements, or an empty list if none
    default List<SvgElement> children() {
        return List.of();
    }
}
