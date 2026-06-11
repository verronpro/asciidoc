import pro.verron.asciidoc.converters.AsciiDocToHtml;
import pro.verron.asciidoc.converters.AsciiDocToSvg;
import pro.verron.asciidoc.converters.AsciiDocToText;
import pro.verron.asciidoc.core.core.AsciiDocModel;

/// Converts [AsciiDocModel] instances to various output formats, including
/// HTML, plain text, and SVG.
///
/// Each converter implements [Function]&lt;[AsciiDocModel], String&gt; and can
/// be used as a standalone mapping function.
///
/// ## Exported Packages
/// <dl>
/// <dt>[pro.verron.asciidoc.converters]</dt>
/// <dd>Core converter implementations:
///     [AsciiDocToHtml], [AsciiDocToText], and [AsciiDocToSvg].</dd>
/// <dt>[pro.verron.asciidoc.converters.svg]</dt>
/// <dd>SVG element model and theming support used by the SVG converter.</dd>
/// </dl>
///
/// ## Requirements
/// - [pro.verron.asciidoc.core/] &mdash; the AsciiDoc document model
/// - [java.desktop/] &mdash; AWT font metrics for SVG text layout
module pro.verron.asciidoc.converters {
    requires pro.verron.asciidoc.core;
    requires java.desktop;
    requires org.jspecify;

    exports pro.verron.asciidoc.converters;
    exports pro.verron.asciidoc.converters.svg;
}
