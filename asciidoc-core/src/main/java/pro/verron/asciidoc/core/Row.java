package pro.verron.asciidoc.core;

import java.util.List;

import static java.util.Collections.emptyList;

/// Table row.
///
/// @param header AsciiDoc block headers containing optional information
/// @param cells  table cells
public record Row(
        List<String> header, List<Cell> cells
) {
    /// Constructs a [Row] without block headers.
    ///
    /// @param cells table cells
    public Row(List<Cell> cells) {
        this(emptyList(), cells);
    }

    /// Constructs a [Row] with block headers.
    ///
    /// @param header header elements, if any
    /// @param cells  table cells
    public Row(List<String> header, List<Cell> cells) {
        this.cells = List.copyOf(cells);
        this.header = header;
    }
}
