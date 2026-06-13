/// Converts [AsciiDocModel][pro.verron.asciidoc.core.AsciiDocModel]
/// instances to various text-based and vector output formats.
///
/// Each converter implements [Function][java.util.function.Function]&lt;
/// [AsciiDocModel][pro.verron.asciidoc.core.AsciiDocModel], String
/// &gt; and can be applied directly to a parsed model:
///
/// - [AsciiDocToHtml][pro.verron.asciidoc.converters.AsciiDocToHtml] &mdash;
/// renders to an HTML document
/// - [AsciiDocToText][pro.verron.asciidoc.converters.AsciiDocToText] &mdash;
/// renders to plain AsciiDoc text
/// - [AsciiDocToSvg][pro.verron.asciidoc.converters.AsciiDocToSvg] &mdash;
/// renders to an SVG document simulating an editor
///
/// @see pro.verron.asciidoc.converters.AsciiDocToHtml
/// @see pro.verron.asciidoc.converters.AsciiDocToText
/// @see pro.verron.asciidoc.converters.AsciiDocToSvg
package pro.verron.asciidoc.converters;
