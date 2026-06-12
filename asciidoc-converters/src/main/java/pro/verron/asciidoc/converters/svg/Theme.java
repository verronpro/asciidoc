package pro.verron.asciidoc.converters.svg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static pro.verron.asciidoc.converters.svg.AsciiDocIcon.findIcon;
import static pro.verron.asciidoc.converters.svg.Icon.*;
import static pro.verron.asciidoc.converters.svg.SvgAttribute.attr;

/// Editor theme used by the SVG converter to simulate different word-processor
/// interfaces (Word, Google Docs, LibreOffice).
public enum Theme {
    /// No editor chrome; renders only the document content.
    NONE,
    /// Microsoft Word-style chrome.
    WORD,
    /// Google Docs-style chrome.
    GDOCS,
    /// LibreOffice Writer-style chrome.
    LIBRE;

    /// Returns the stroke color used for comment connectors and element outlines.
    ///
    /// @return the stroke color, or empty if the theme has none
    public Optional<String> getStrokeColor() {
        return switch (this) {
            case NONE -> Optional.empty();
            case WORD -> Optional.of("#ffc000");
            case GDOCS -> Optional.of("#0b57d0");
            case LIBRE -> Optional.of("#808080");
        };
    }

    /// Returns the background highlight color for commented blocks.
    ///
    /// @return the highlight color, or empty if the theme has none
    public Optional<String> getHighlightColor() {
        return switch (this) {
            case NONE -> Optional.empty();
            case WORD -> Optional.of("#fff2cc");
            case GDOCS -> Optional.of("#c2e7ff");
            case LIBRE -> Optional.of("#ffff00");
        };
    }

    /// Returns the overall background color of the simulated editor.
    ///
    /// @return the background color, or empty if the theme has none
    public Optional<String> getBgColor() {
        return switch (this) {
            case NONE -> Optional.empty();
            case WORD -> Optional.of("#e6e6e6");
            case GDOCS -> Optional.of("#f8f9fa");
            case LIBRE -> Optional.of("#dfdfdf");
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

    /// Returns the primary font name for AWT font instantiation.
    Optional<String> getPrimaryFont() {
        return switch (this) {
            case NONE -> Optional.empty();
            case WORD -> Optional.of("Calibri");
            case GDOCS -> Optional.of("Arial");
            case LIBRE -> Optional.of("Liberation Serif");
        };
    }

    /// Renders the editor banner (toolbar and menu bar) as a collection of SVG elements.
    ///
    /// @param title        document title displayed in the banner
    /// @param bannerHeight total height of the banner area in pixels
    ///
    /// @return SVG elements that compose the banner
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
        // Blue title bar with Quick Access Toolbar
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

        // Quick Access Toolbar icons
        findIcon(SAVE, 10, 7, 16, "white").ifPresent(elements::add);
        findIcon(UNDO, 35, 7, 16, "white").ifPresent(elements::add);
        findIcon(REDO, 60, 7, 16, "white").ifPresent(elements::add);

        // Ribbon area background
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

        // Ribbon tab names
        elements.add(new SvgText("20",
                "55",
                "11",
                "#333",
                "File  Home  Insert  Design  Layout  References  Mailings  Review  View",
                attr("font-family", "Segoe UI, Arial")));

        // Clipboard group
        findIcon(PASTE, 20, 70, 16, "#333").ifPresent(elements::add);
        findIcon(CUT, 45, 70, 14, "#333").ifPresent(elements::add);
        findIcon(COPY, 65, 70, 14, "#333").ifPresent(elements::add);
        findIcon(FORMAT_PAINTER, 85, 70, 14, "#333").ifPresent(elements::add);

        // Font group
        findIcon(BOLD, 125, 70, 16, "#333").ifPresent(elements::add);
        findIcon(ITALIC, 150, 70, 16, "#333").ifPresent(elements::add);
        findIcon(UNDERLINE, 175, 70, 16, "#333").ifPresent(elements::add);
        findIcon(STRIKETHROUGH, 200, 70, 14, "#333").ifPresent(elements::add);
        findIcon(SUBSCRIPT, 220, 70, 14, "#333").ifPresent(elements::add);
        findIcon(SUPERSCRIPT, 240, 70, 14, "#333").ifPresent(elements::add);
        findIcon(CLEAR_FORMATTING, 260, 70, 14, "#333").ifPresent(elements::add);
        findIcon(HIGHLIGHT, 280, 70, 14, "#333").ifPresent(elements::add);
        findIcon(FONT_COLOR, 300, 70, 14, "#333").ifPresent(elements::add);

        // Paragraph group
        findIcon(BULLETS, 330, 70, 14, "#333").ifPresent(elements::add);
        findIcon(NUMBERING, 350, 70, 14, "#333").ifPresent(elements::add);
        findIcon(INDENT_DECREASE, 370, 70, 14, "#333").ifPresent(elements::add);
        findIcon(INDENT_INCREASE, 390, 70, 14, "#333").ifPresent(elements::add);
        findIcon(ALIGN_LEFT, 415, 70, 14, "#333").ifPresent(elements::add);
        findIcon(ALIGN_CENTER, 435, 70, 14, "#333").ifPresent(elements::add);
        findIcon(ALIGN_RIGHT, 455, 70, 14, "#333").ifPresent(elements::add);
        findIcon(JUSTIFY, 475, 70, 14, "#333").ifPresent(elements::add);

        // Editing group
        findIcon(SEARCH, 510, 70, 14, "#333").ifPresent(elements::add);

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
        findIcon(TABLE, 22, 22, 16, "white").ifPresent(elements::add);

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

        // Star and Share
        findIcon(STAR, 520, 18, 16, "#5f6368").ifPresent(elements::add);
        findIcon(SHARE, 545, 18, 16, "#5f6368").ifPresent(elements::add);

        // Toolbar background
        elements.add(new SvgRect("0",
                "65",
                "100%",
                String.valueOf(bannerHeight - 70),
                attr("fill", "#edf2fa"),
                attr("rx", "15")));
        elements.add(new SvgLine("0", "100", "100%", "100", "#ccc"));

        // Undo / Redo / Print
        findIcon(UNDO, 20, 72, 16, "#5f6368").ifPresent(elements::add);
        findIcon(REDO, 45, 72, 16, "#5f6368").ifPresent(elements::add);
        findIcon(PRINT, 70, 72, 16, "#5f6368").ifPresent(elements::add);
        findIcon(SPELLCHECK, 95, 72, 16, "#5f6368").ifPresent(elements::add);

        // Formatting icons
        findIcon(BOLD, 135, 72, 16, "#5f6368").ifPresent(elements::add);
        findIcon(ITALIC, 160, 72, 16, "#5f6368").ifPresent(elements::add);
        findIcon(UNDERLINE, 185, 72, 16, "#5f6368").ifPresent(elements::add);

        // Text color
        findIcon(FONT_COLOR, 215, 72, 14, "#5f6368").ifPresent(elements::add);
        findIcon(HIGHLIGHT, 235, 72, 14, "#5f6368").ifPresent(elements::add);

        // Link / Comment / Image / Chart
        findIcon(LINK, 265, 72, 14, "#5f6368").ifPresent(elements::add);
        findIcon(COMMENT, 290, 72, 14, "#5f6368").ifPresent(elements::add);
        findIcon(IMAGE, 315, 72, 14, "#5f6368").ifPresent(elements::add);

        // Alignment
        findIcon(ALIGN_LEFT, 350, 72, 14, "#5f6368").ifPresent(elements::add);
        findIcon(ALIGN_CENTER, 370, 72, 14, "#5f6368").ifPresent(elements::add);
        findIcon(ALIGN_RIGHT, 390, 72, 14, "#5f6368").ifPresent(elements::add);

        // Line spacing / Bullets / Numbering
        findIcon(BULLETS, 420, 72, 14, "#5f6368").ifPresent(elements::add);
        findIcon(NUMBERING, 440, 72, 14, "#5f6368").ifPresent(elements::add);
        findIcon(INDENT_DECREASE, 460, 72, 14, "#5f6368").ifPresent(elements::add);
        findIcon(INDENT_INCREASE, 480, 72, 14, "#5f6368").ifPresent(elements::add);

        return elements;
    }

    private List<SvgElement> renderLibreOfficeBanner(
            String title,
            double bannerHeight
    ) {
        var elements = new ArrayList<SvgElement>();
        // Title bar
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

        // Standard toolbar row
        elements.add(new SvgRect("0",
                "55",
                "100%",
                "22",
                attr("fill", "#eeeeee")));
        elements.add(new SvgLine("0", "55", "100%", "55", "#ccc"));

        // File operations
        findIcon(NEW, 10, 58, 16, "#333").ifPresent(elements::add);
        findIcon(OPEN, 35, 58, 16, "#333").ifPresent(elements::add);
        findIcon(SAVE, 60, 58, 16, "#333").ifPresent(elements::add);
        findIcon(PDF_EXPORT, 85, 58, 16, "#333").ifPresent(elements::add);
        findIcon(PRINT, 110, 58, 16, "#333").ifPresent(elements::add);

        // Clipboard operations
        findIcon(CUT, 145, 58, 14, "#333").ifPresent(elements::add);
        findIcon(COPY, 165, 58, 14, "#333").ifPresent(elements::add);
        findIcon(PASTE, 185, 58, 14, "#333").ifPresent(elements::add);
        findIcon(FORMAT_PAINTER, 210, 58, 14, "#333").ifPresent(elements::add);

        // Undo / Redo / Search
        findIcon(UNDO, 240, 58, 16, "#333").ifPresent(elements::add);
        findIcon(REDO, 265, 58, 16, "#333").ifPresent(elements::add);
        findIcon(SEARCH, 295, 58, 14, "#333").ifPresent(elements::add);
        findIcon(SPELLCHECK, 315, 58, 14, "#333").ifPresent(elements::add);

        // Insert objects
        findIcon(TABLE, 350, 58, 14, "#333").ifPresent(elements::add);
        findIcon(IMAGE, 370, 58, 14, "#333").ifPresent(elements::add);
        findIcon(CHART, 390, 58, 14, "#333").ifPresent(elements::add);
        findIcon(LINK, 410, 58, 14, "#333").ifPresent(elements::add);
        findIcon(COMMENT, 430, 58, 14, "#333").ifPresent(elements::add);

        // Formatting toolbar row
        elements.add(new SvgRect("0",
                "77",
                "100%",
                String.valueOf(bannerHeight - 77),
                attr("fill", "#eeeeee")));
        elements.add(new SvgLine("0", "77", "100%", "77", "#ccc"));
        elements.add(new SvgLine("0", "100", "100%", "100", "#ccc"));

        // Text formatting
        findIcon(BOLD, 10, 80, 16, "#333").ifPresent(elements::add);
        findIcon(ITALIC, 35, 80, 16, "#333").ifPresent(elements::add);
        findIcon(UNDERLINE, 60, 80, 16, "#333").ifPresent(elements::add);
        findIcon(STRIKETHROUGH, 85, 80, 14, "#333").ifPresent(elements::add);
        findIcon(SUBSCRIPT, 105, 80, 14, "#333").ifPresent(elements::add);
        findIcon(SUPERSCRIPT, 125, 80, 14, "#333").ifPresent(elements::add);
        findIcon(FONT_COLOR, 150, 80, 14, "#333").ifPresent(elements::add);
        findIcon(HIGHLIGHT, 170, 80, 14, "#333").ifPresent(elements::add);

        // Alignment
        findIcon(ALIGN_LEFT, 205, 80, 14, "#333").ifPresent(elements::add);
        findIcon(ALIGN_CENTER, 225, 80, 14, "#333").ifPresent(elements::add);
        findIcon(ALIGN_RIGHT, 245, 80, 14, "#333").ifPresent(elements::add);
        findIcon(JUSTIFY, 265, 80, 14, "#333").ifPresent(elements::add);

        // Lists
        findIcon(BULLETS, 295, 80, 14, "#333").ifPresent(elements::add);
        findIcon(NUMBERING, 315, 80, 14, "#333").ifPresent(elements::add);
        findIcon(INDENT_DECREASE, 340, 80, 14, "#333").ifPresent(elements::add);
        findIcon(INDENT_INCREASE, 360, 80, 14, "#333").ifPresent(elements::add);

        // Paragraph marks
        findIcon(PARAGRAPH_MARKS, 390, 80, 14, "#333").ifPresent(elements::add);

        return elements;
    }
}
