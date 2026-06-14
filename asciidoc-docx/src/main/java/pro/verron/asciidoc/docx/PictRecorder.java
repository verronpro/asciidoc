package pro.verron.asciidoc.docx;

import org.docx4j.TextUtils;
import org.docx4j.vml.CTRoundRect;
import org.docx4j.vml.CTShadow;
import org.docx4j.vml.CTTextbox;
import org.docx4j.wml.P;
import org.docx4j.wml.Pict;
import pro.verron.asciidoc.core.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.docx4j.XmlUtils.unwrap;

public class PictRecorder {
    private final List<Pict> picts;

    public PictRecorder() {
        picts = new ArrayList<>();
    }

    public void add(Pict pict) {
        this.picts.add(pict);
    }

    /// @return the comment macro blocks
    public Collection<OpenBlock> all() {
        return picts.stream()
                    .map(this::asBlock)
                    .toList();
    }

    private OpenBlock asBlock(Pict pict) {
        var header = List.of("pict", "anchor=" + pict.getAnchorId());
        var content = pict.getAnyAndAny()
                          .stream()
                          .map(pO -> new Paragraph(asTextContent(pO, 0)))
                          .toList();
        return new OpenBlock(header, content);
    }

    private List<Inline> asTextContent(Object o, int depth) {
        var obj = unwrap(o);
        return switch (obj) {
            case CTRoundRect rr -> asContent(rr, depth);
            case P p -> asContent(p, depth);
            case CTShadow s -> asContent(s, depth);
            case CTTextbox tb -> asContent(tb, depth);
            default -> throw new IllegalArgumentException(
                    "Unsupported object: " + o);
        };
    }

    private List<Inline> asContent(P p, int depth) {
        var inlines = new ArrayList<Inline>();
        for (var i = 0; i < depth; i++) {
            inlines.add(new Tab());
        }
        inlines.add(new Text(TextUtils.getText(p)));
        return inlines;
    }

    private List<Inline> asContent(CTShadow ignored, int depth) {
        var inlines = new ArrayList<Inline>();
        for (var i = 0; i < depth; i++) {
            inlines.add(new Tab());
        }
        inlines.add(new Text("shadow\n"));
        return inlines;
    }

    private List<Inline> asContent(CTTextbox tb, int depth) {
        var inlines = new ArrayList<Inline>();
        for (var i = 0; i < depth; i++) {
            inlines.add(new Tab());
        }
        inlines.add(new Text("textbox\n"));
        tb.getTxbxContent()
          .getContent()
          .stream()
          .map(ee -> asTextContent(ee, depth + 1))
          .forEach(inlines::addAll);
        return inlines;
    }

    private List<Inline> asContent(CTRoundRect rr, int depth) {
        var inlines = new ArrayList<Inline>();
        for (var i = 0; i < depth; i++) {
            inlines.add(new Tab());
        }
        inlines.add(new Text("roundrect\n"));
        rr.getEGShapeElements()
          .stream()
          .map(o -> asTextContent(o, depth + 1))
          .forEach(inlines::addAll);
        return inlines;
    }
}
