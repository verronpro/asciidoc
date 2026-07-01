/// The module `pro.verron.asciidoc.compiler` is responsible for handling the
/// compilation and processing of AsciiDoc documents using various converters
///  and tools. This module enables advanced functionality such as exporting
/// and transforming documents into different formats.
///
/// Required modules:
/// - `pro.verron.asciidoc.core`: Provides core utilities and structures for
/// working with AsciiDoc content.
/// - `pro.verron.asciidoc.converters`: Offers conversion capabilities for
/// transforming AsciiDoc into different target formats.
/// - `pro.verron.asciidoc.docx`: Includes support for generating DOCX files
/// from AsciiDoc content.
/// - `org.apache.xmlgraphics.batik.transcoder`: Supplies transcoding
/// functionality for processing SVG graphics.
/// - `org.apache.xmlgraphics.batik.codec`: Provides additional codec support
/// for handling image formats.
/// - `java.desktop`: Required for GUI or desktop-based functionalities.
/// - `org.docx4j.core`: A library for manipulating and generating DOCX
/// documents programmatically.
///
/// Exported packages:
/// - `pro.verron.asciidoc.compiler`: Contains the core classes and utilities
/// for document compilation and processing.

module pro.verron.asciidoc.compiler {
    requires pro.verron.asciidoc.core;
    requires pro.verron.asciidoc.converters;
    requires pro.verron.asciidoc.docx;
    requires org.apache.xmlgraphics.batik.transcoder;
    requires org.apache.xmlgraphics.batik.codec;
    requires java.desktop;
    requires org.docx4j.core;
    requires org.jspecify;

    exports pro.verron.asciidoc.compiler;
}
