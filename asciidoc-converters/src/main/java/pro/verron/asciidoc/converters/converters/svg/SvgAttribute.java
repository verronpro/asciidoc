package pro.verron.asciidoc.converters.converters.svg;

import java.util.Locale;
import java.util.Objects;

public final class SvgAttribute
        implements Comparable<SvgAttribute> {

    public static final SvgAttribute NONE = new SvgAttribute("", "");

    private final String name;
    private final String value;

    private SvgAttribute(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public static SvgAttribute attr(String name, String value) {
        return new SvgAttribute(name, value);
    }

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

    public String serialize() {
        return String.format(Locale.ROOT, "%s=\"%s\"", name, value);
    }
}
