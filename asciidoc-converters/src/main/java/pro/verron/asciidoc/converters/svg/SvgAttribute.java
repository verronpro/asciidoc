package pro.verron.asciidoc.converters.svg;

import java.util.Locale;
import java.util.Objects;

/// An SVG attribute name/value pair, used when constructing SVG elements.
///
/// Instances are created via the factory method [#attr(String, String)].
/// The sentinel value [#NONE] represents an absent attribute and is
/// filtered out during serialization.
public final class SvgAttribute
        implements Comparable<SvgAttribute> {

    /// Sentinel value representing an absent attribute; filtered out during
    /// serialization.
    public static final SvgAttribute NONE = new SvgAttribute("", "");

    private final String name;
    private final String value;

    private SvgAttribute(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /// Creates an attribute with a string value.
    ///
    /// @param name  attribute name
    /// @param value attribute value
    ///
    /// @return the new [SvgAttribute]
    public static SvgAttribute attr(String name, String value) {
        return new SvgAttribute(name, value);
    }

    /// Creates an attribute with an integer value.
    ///
    /// @param name attribute name
    /// @param i    attribute value
    ///
    /// @return the new [SvgAttribute]
    public static SvgAttribute attr(String name, int i) {
        return new SvgAttribute(name, String.valueOf(i));
    }

    @Override
    public int compareTo(SvgAttribute that) {
        return name.compareTo(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof SvgAttribute that//
               && Objects.equals(name, that.name) //
               && Objects.equals(value, that.value);
    }

    @Override
    public String toString() {
        return "attribute[%s]".formatted(serialize());
    }

    /// Serializes this attribute as {@code name="value"}.
    ///
    /// @return the serialized attribute string
    public String serialize() {
        return String.format(Locale.ROOT, "%s=\"%s\"", name, value);
    }
}
