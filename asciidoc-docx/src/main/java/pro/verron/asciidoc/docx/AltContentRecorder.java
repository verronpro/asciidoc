package pro.verron.asciidoc.docx;

import org.docx4j.TextUtils;
import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingShape.CTTextboxInfo;
import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingShape.CTWordprocessingShape;
import org.docx4j.dml.Graphic;
import org.docx4j.dml.wordprocessingDrawing.Anchor;
import org.docx4j.mce.AlternateContent;
import org.docx4j.mce.AlternateContent.Choice;
import org.docx4j.mce.AlternateContent.Fallback;
import org.docx4j.vml.CTRoundRect;
import org.docx4j.vml.CTShadow;
import org.docx4j.vml.CTTextbox;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.P;
import org.docx4j.wml.Pict;
import pro.verron.asciidoc.core.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.docx4j.XmlUtils.unwrap;

public class AltContentRecorder {
    List<AlternateContent> alternateContents = new ArrayList<>();

    public int add(AlternateContent ac) {
        alternateContents.add(ac);
        return alternateContents.size();
    }

    public Collection<OpenBlock> all() {
        List<OpenBlock> list = new ArrayList<>();
        for (int i = 0; i < alternateContents.size(); i++) {
            AlternateContent index = alternateContents.get(i);
            OpenBlock block = asBlock(i + 1, index);
            list.add(block);
        }
        return list;

    }

    private OpenBlock asBlock(int index, AlternateContent alts) {
        var header = List.of("alternateContent", "anchor=" + index);

        var content = new ArrayList<Block>();
        for (Choice c : alts.getChoice()) content.add(asParagraph(c));
        content.add(asParagraph(alts.getFallback()));

        return new OpenBlock(header, content);
    }

    private Paragraph asParagraph(Object o) {
        var obj = unwrap(o);
        return new Paragraph(switch (obj) {
            case Fallback fb -> asContent(fb);
            case Choice c -> asContent(c);
            case Object objobj -> throw new IllegalArgumentException("Unsupported object: " + objobj);
        });
    }

    private List<Inline> asContent(Fallback fb) {
        var inlines = new ArrayList<Inline>();
        inlines.add(new Text("fallback\n"));
        inlines.addAll(asContent(fb.getAny(), 1));
        return inlines;
    }

    private List<Inline> asContent(Choice c) {
        var inlines = new ArrayList<Inline>();
        inlines.add(new Text("choice " + c.getRequires() + "\n"));
        inlines.addAll(asContent(c.getAny(), 1));
        return inlines;
    }

    private List<Inline> asContent(List<?> list, int depth) {
        var inlines = new ArrayList<Inline>();
        for (var object : list) {
            inlines.addAll(asContent(object, depth));
        }
        return inlines;
    }

    private List<Inline> asContent(Drawing drawing, int depth) {
        var inlines = new ArrayList<Inline>();
        for (var i = 0; i < depth; i++) {
            inlines.add(new Tab());
        }
        inlines.add(new Text("drawing\n"));
        inlines.addAll(asContent(drawing.getAnchorOrInline(), depth + 1));
        return inlines;
    }

    private List<Inline> asContent(Anchor anchor, int depth) {
        var inlines = new ArrayList<Inline>();
        for (var i = 0; i < depth; i++) {
            inlines.add(new Tab());
        }
        inlines.add(new Text("anchor\n"));
        inlines.addAll(asContent(anchor.getGraphic(), depth + 1));
        return inlines;
    }

    private List<Inline> asContent(CTWordprocessingShape shape, int depth) {
        var inlines = new ArrayList<Inline>();
        for (var i = 0; i < depth; i++) {
            inlines.add(new Tab());
        }
        inlines.add(new Text("wordprocessingshape\n"));
        inlines.addAll(asContent(shape.getTxbx(), depth + 1));
        return inlines;
    }

    private List<Inline> asContent(CTTextboxInfo tbi, int depth) {
        var inlines = new ArrayList<Inline>();
        for (var i = 0; i < depth; i++) {
            inlines.add(new Tab());
        }
        inlines.add(new Text("textbox\n"));
        inlines.addAll(asContent(tbi.getTxbxContent().getContent(), depth + 1));
        return inlines;
    }

    private List<Inline> asContent(CTTextbox tb, int depth) {
        var inlines = new ArrayList<Inline>();
        for (var i = 0; i < depth; i++) {
            inlines.add(new Tab());
        }
        inlines.add(new Text("textbox\n"));
        inlines.addAll(asContent(tb.getTxbxContent().getContent(), depth + 1));
        return inlines;
    }

    private List<Inline> asContent(P p, int depth) {
        var inlines = new ArrayList<Inline>();
        for (var i = 0; i < depth; i++) {
            inlines.add(new Tab());
        }
        inlines.add(new Text(TextUtils.getText(p)));
        return inlines;
    }

    private List<Inline> asContent(Pict pict, int depth) {
        var inlines = new ArrayList<Inline>();
        for (var i = 0; i < depth; i++) {
            inlines.add(new Tab());
        }
        inlines.add(new Text("pict\n"));
        inlines.addAll(asContent(pict.getAnyAndAny(), depth + 1));
        return inlines;
    }

    private List<Inline> asContent(CTRoundRect rr, int depth) {
        var inlines = new ArrayList<Inline>();
        for (var i = 0; i < depth; i++) {
            inlines.add(new Tab());
        }
        inlines.add(new Text("roundrect\n"));
        inlines.addAll(asContent(rr.getEGShapeElements(), depth + 1));
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

    private List<Inline> asContent(Graphic graphic, int depth) {
        var inlines = new ArrayList<Inline>();
        for (var i = 0; i < depth; i++) {
            inlines.add(new Tab());
        }
        var graphicData = graphic.getGraphicData();
        inlines.add(new Text("graphic\n"));
        inlines.addAll(asContent(graphicData.getAny(), depth + 1));
        return inlines;
    }

    private List<Inline> asContent(Object obj, int depth) {
        var o = unwrap(obj);
        var inlines = new ArrayList<Inline>();
        inlines.addAll(switch (o) {
            case Anchor a -> asContent(a, depth);
            case P p -> asContent(p, depth);
            case CTTextboxInfo tbi -> asContent(tbi, depth);
            case CTWordprocessingShape ws -> asContent(ws, depth);
            case CTShadow s -> asContent(s, depth);
            case CTTextbox tb -> asContent(tb, depth);
            case Pict p -> asContent(p, depth);
            case Graphic g -> asContent(g, depth);
            case Drawing d -> asContent(d, depth);
            case CTRoundRect rr -> asContent(rr, depth);
            case Object oo -> throw new RuntimeException(oo.getClass().getName());
        });
        return inlines;
    }
}
