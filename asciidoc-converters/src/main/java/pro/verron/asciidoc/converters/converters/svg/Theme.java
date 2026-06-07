package pro.verron.asciidoc.converters.converters.svg;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static pro.verron.asciidoc.converters.converters.svg.AsciiDocIcon.findIcon;
import static pro.verron.asciidoc.converters.converters.svg.SvgAttribute.attr;

public enum Theme {
    NONE,
    WORD,
    GDOCS,
    LIBRE;

    public Optional<String> getStrokeColor() {
        return switch (this) {
            case NONE -> Optional.empty();
            case WORD -> Optional.of("#ffc000");
            case GDOCS -> Optional.of("#0b57d0");
            case LIBRE -> Optional.of("#808080");
        };
    }

    public Optional<String> getHighlightColor() {
        return switch (this) {
            case NONE -> Optional.empty();
            case WORD -> Optional.of("#fff2cc");
            case GDOCS -> Optional.of("#c2e7ff");
            case LIBRE -> Optional.of("#ffff00");
        };
    }

    // Background
    public Optional<String> getBgColor() {
        return switch (this) {
            case NONE -> Optional.empty();
            case WORD -> Optional.of("#e6e6e6");
            case GDOCS -> Optional.of("#f8f9fa");
            case LIBRE -> Optional.of("#dfdfdf");
        };
    }

    Optional<String> getAwtFallbacks() {
        return switch (this) {
            case NONE -> Optional.empty();
            case WORD, GDOCS -> Optional.of(Font.SANS_SERIF);
            case LIBRE -> Optional.of(Font.SERIF);
        };
    }

    /// Returns the SVG font-family string for a given theme.
    ///
    /// @return font-family string
    public Optional<String> getFontFamily() {
        return switch (this) {
            case NONE -> Optional.empty();
            case WORD -> Optional.of("Calibri, 'Carlito', 'Arial', sans-serif");
            case GDOCS -> Optional.of("Arial, 'Arimo', sans-serif");
            case LIBRE -> Optional.of("'Liberation Serif', 'Tinos', 'Times "
                                      + "New Roman', serif");
        };
    }

    Optional<String> getPrimaryFont() {
        return switch (this) {
            case NONE -> Optional.empty();
            case WORD -> Optional.of("Calibri");
            case GDOCS -> Optional.of("Arial");
            case LIBRE -> Optional.of("Liberation Serif");
        };
    }

    public Collection<? extends SvgElement> renderBanner(
            String title,
            double bannerHeight
    ) {
        return switch (this) {
            case NONE -> List.of();
            case WORD -> renderWordBanner(title, bannerHeight);
            case GDOCS -> renderGoogleDocsBanner(title, bannerHeight);
            case LIBRE -> renderLibreOfficeBanner(title, bannerHeight);
        };
    }

    private List<SvgElement> renderWordBanner(
            String title,
            double bannerHeight
    ) {
        var elements = new ArrayList<SvgElement>();
        // Blue top bar
        elements.add(new SvgRect("0",
                "0",
                "100%",
                "30",
                attr("fill", "#2b579a")));
        elements.add(new SvgText("50%",
                "20",
                "12",
                "white",
                title + " - Word",
                attr("font-family", "Segoe UI, Arial"),
                attr("text-anchor", "middle")));

        // Top bar icons
        findIcon("save", 10, 7, 16, "white").ifPresent(elements::add);
        findIcon("undo", 35, 7, 16, "white").ifPresent(elements::add);
        findIcon("redo", 60, 7, 16, "white").ifPresent(elements::add);

        // Ribbon area
        elements.add(new SvgRect("0",
                "30",
                "100%",
                String.valueOf(bannerHeight - 30),
                attr("fill", "#f3f2f1")));
        elements.add(new SvgLine("0", "30", "100%", "30", "#ccc"));
        elements.add(new SvgLine("0",
                String.valueOf(bannerHeight),
                "100%",
                String.valueOf(bannerHeight),
                "#ccc"));

        // Simple ribbon icons simulation
        elements.add(new SvgText("20",
                "55",
                "11",
                "#333",
                "File  Home  Insert  Layout  References  Review  View",
                attr("font-family", "Segoe UI, Arial")));

        findIcon("bold", 20, 70, 16, "#333").ifPresent(elements::add);
        findIcon("italic", 45, 70, 16, "#333").ifPresent(elements::add);
        findIcon("image", 70, 70, 16, "#333").ifPresent(elements::add);
        findIcon("table", 95, 70, 16, "#333").ifPresent(elements::add);
        findIcon("link", 120, 70, 16, "#333").ifPresent(elements::add);

        return elements;
    }

    private List<SvgElement> renderGoogleDocsBanner(
            String title,
            double bannerHeight
    ) {
        var elements = new ArrayList<SvgElement>();
        // Top bar
        elements.add(new SvgRect("0",
                "0",
                "100%",
                "60",
                attr("fill", "white")));
        elements.add(new SvgCircle("30", "30", "15", "#4285f4"));
        findIcon("table", 22, 22, 16, "white").ifPresent(elements::add);

        elements.add(new SvgText("60",
                "25",
                "18",
                "#3c4043",
                title,
                attr("font-family", "Product Sans, Arial")));
        elements.add(new SvgText("60",
                "45",
                "12",
                "#5f6368",
                "File  Edit  View  Insert  Format  Tools  Extensions  Help",
                attr("font-family", "Arial")));

        // Toolbar
        elements.add(new SvgRect("0",
                "65",
                "100%",
                String.valueOf(bannerHeight - 70),
                attr("fill", "#edf2fa"),
                attr("rx", "15")));
        elements.add(new SvgLine("0", "100", "100%", "100", "#ccc"));

        findIcon("undo", 20, 72, 16, "#5f6368").ifPresent(elements::add);
        findIcon("redo", 45, 72, 16, "#5f6368").ifPresent(elements::add);
        findIcon("print", 70, 72, 16, "#5f6368").ifPresent(elements::add);
        findIcon("bold", 110, 72, 16, "#5f6368").ifPresent(elements::add);
        findIcon("italic", 135, 72, 16, "#5f6368").ifPresent(elements::add);
        findIcon("link", 160, 72, 16, "#5f6368").ifPresent(elements::add);
        findIcon("chat-left-text",
                185,
                72,
                16,
                "#5f6368").ifPresent(elements::add);

        return elements;
    }

    private List<SvgElement> renderLibreOfficeBanner(
            String title,
            double bannerHeight
    ) {
        var elements = new ArrayList<SvgElement>();
        // Top bar
        elements.add(new SvgRect("0",
                "0",
                "100%",
                "30",
                attr("fill", "#dfdfdf")));
        elements.add(new SvgText("10",
                "20",
                "12",
                "black",
                title + " - LibreOffice Writer",
                attr("font-family", "Arial")));

        // Menu bar
        elements.add(new SvgRect("0",
                "30",
                "100%",
                "25",
                attr("fill", "#eeeeee")));
        elements.add(new SvgText("10",
                "47",
                "11",
                "black",
                "File  Edit  View  Insert  Format  Styles  Table  Form  Tools"
                + "  Window  Help",
                attr("font-family", "Arial")));

        // Toolbars
        elements.add(new SvgRect("0",
                "55",
                "100%",
                String.valueOf(bannerHeight - 55),
                attr("fill", "#eeeeee")));
        elements.add(new SvgLine("0", "55", "100%", "55", "#ccc"));
        elements.add(new SvgLine("0", "100", "100%", "100", "#ccc"));

        findIcon("save", 10, 65, 20, "#333").ifPresent(elements::add);
        findIcon("print", 40, 65, 20, "#333").ifPresent(elements::add);
        findIcon("undo", 70, 65, 20, "#333").ifPresent(elements::add);
        findIcon("redo", 100, 65, 20, "#333").ifPresent(elements::add);
        findIcon("bold", 150, 65, 20, "#333").ifPresent(elements::add);
        findIcon("italic", 180, 65, 20, "#333").ifPresent(elements::add);

        return elements;
    }
}
