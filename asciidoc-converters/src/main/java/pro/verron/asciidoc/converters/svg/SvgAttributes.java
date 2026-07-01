package pro.verron.asciidoc.converters.svg;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import static pro.verron.asciidoc.converters.svg.SvgAttribute.NONE;
import static pro.verron.asciidoc.converters.svg.SvgAttribute.attr;

/// An ordered, deduplicated collection of [SvgAttribute] instances.
///
/// [SvgAttribute#NONE] attributes are silently ignored when added. Attributes
/// are sorted by name to produce deterministic SVG output.
public class SvgAttributes
        implements Iterable<SvgAttribute> {
    SortedSet<SvgAttribute> set = new TreeSet<>();

    /// Constructs an attribute set from the given attributes.
    ///
    /// @param attributes initial attributes (none-values are ignored)
    public SvgAttributes(SvgAttribute... attributes) {
        addAll(attributes);
    }

    private void addAll(SvgAttribute[] attributes) {
        for (SvgAttribute attr : attributes) {
            if (!NONE.equals(attr)) {
                set.add(attr);
            }
        }
    }

    @Override
    public Iterator<SvgAttribute> iterator() {
        return set.iterator();
    }

    /// Adds an attribute with the given name and value.
    ///
    /// @param name  attribute name
    /// @param value attribute value
    public void add(String name, String value) {
        set.add(attr(name, value));
    }

    /// Adds all attributes from another [SvgAttributes] instance.
    ///
    /// @param opts attributes to add
    public void addAll(SvgAttributes opts) {
        addAll(opts.set);
    }

    private void addAll(Iterable<SvgAttribute> attributes) {
        for (SvgAttribute attr : attributes) {
            if (!NONE.equals(attr)) {
                set.add(attr);
            }
        }
    }
}
