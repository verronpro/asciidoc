package pro.verron.asciidoc.converters.svg;

/// Predefined icon identifiers used in simulated editor interfaces.
/// Each constant corresponds to an SVG resource file in the
/// {@code icons/} package directory.
/// Icons are sourced from Bootstrap Icons (MIT License).
public enum Icon {

    // File operations
    /// Blank document / new file icon.
    NEW,
    /// Folder opening icon.
    OPEN,
    /// Floppy-disk save icon.
    SAVE,
    /// Printer icon.
    PRINT,
    /// PDF file export icon.
    PDF_EXPORT,

    // Clipboard / edit operations
    /// Clipboard paste icon.
    PASTE,
    /// Scissors cut icon.
    CUT,
    /// Two overlapping pages copy icon.
    COPY,
    /// Paintbrush format-painter icon.
    FORMAT_PAINTER,

    // Undo / redo
    /// Curved-arrow undo icon.
    UNDO,
    /// Curved-arrow redo icon.
    REDO,

    // Text formatting
    /// Bold-text formatting icon.
    BOLD,
    /// Italic-text formatting icon.
    ITALIC,
    /// Underline-text formatting icon.
    UNDERLINE,
    /// Strikethrough-text formatting icon.
    STRIKETHROUGH,
    /// Subscript icon.
    SUBSCRIPT,
    /// Superscript icon.
    SUPERSCRIPT,
    /// Eraser clear-formatting icon.
    CLEAR_FORMATTING,
    /// Highlighter pen icon.
    HIGHLIGHT,
    /// Font-color icon.
    FONT_COLOR,

    // Paragraph / alignment
    /// Left-aligned text lines icon.
    ALIGN_LEFT,
    /// Center-aligned text lines icon.
    ALIGN_CENTER,
    /// Right-aligned text lines icon.
    ALIGN_RIGHT,
    /// Fully-justified text lines icon.
    JUSTIFY,
    /// Bullet list icon.
    BULLETS,
    /// Numbered list icon.
    NUMBERING,
    /// Decrease-indent icon.
    INDENT_DECREASE,
    /// Increase-indent icon.
    INDENT_INCREASE,
    /// Paragraph-marks toggle icon.
    PARAGRAPH_MARKS,

    // Insert / objects
    /// Image/picture icon.
    IMAGE,
    /// Table grid icon.
    TABLE,
    /// Chain-link icon.
    LINK,
    /// Chat/comment bubble icon.
    COMMENT,
    /// Bar chart icon.
    CHART,

    // Search / review
    /// Magnifying-glass search icon.
    SEARCH,
    /// Spell-check icon.
    SPELLCHECK,

    // Status / share
    /// Star/bookmark icon.
    STAR,
    /// Share icon.
    SHARE;

    /// Returns the resource name for this icon (relative to the package).
    String resourceName() {
        return "icons/" + name().toLowerCase() + ".svg";
    }
}
