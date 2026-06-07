package pro.verron.asciidoc.converters.converters.svg;

import org.jspecify.annotations.NonNull;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import static pro.verron.asciidoc.converters.converters.svg.SvgAttribute.NONE;
import static pro.verron.asciidoc.converters.converters.svg.SvgAttribute.attr;

public class SvgAttributes
        implements Iterable<SvgAttribute> {
    SortedSet<SvgAttribute> set = new TreeSet<>();

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
    public @NonNull Iterator<SvgAttribute> iterator() {
        return set.iterator();
    }

    public void add(String name, String value) {
        set.add(attr(name, value));
    }

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
