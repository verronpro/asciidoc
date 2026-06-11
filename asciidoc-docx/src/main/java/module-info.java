import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import pro.verron.asciidoc.core.core.AsciiDocModel;
import pro.verron.asciidoc.docx.AsciiDocToDocx;
import pro.verron.asciidoc.docx.DocxToAsciiDoc;

import java.util.function.Function;

/// Bidirectional conversion between AsciiDoc document models and Microsoft
/// Word (DOCX) format, built on top of Docx4J.
///
/// ## Exported Packages
/// <dl>
/// <dt>[pro.verron.asciidoc.docx]</dt>
/// <dd>Contains [DocxToAsciiDoc] and [AsciiDocToDocx], which implement
///     [Function]&lt;[WordprocessingMLPackage], [AsciiDocModel]&gt; and
///     [Function]&lt;[AsciiDocModel], [WordprocessingMLPackage]&gt;
///     respectively.</dd>
/// </dl>
///
/// ## Requirements
/// - [pro.verron.asciidoc.core/] &mdash; the AsciiDoc document model
/// - [pro.verron.asciidoc.converters/] &mdash; text conversion utilities
/// - [org.docx4j.core/] &mdash; DOCX reading and writing (transitive)
module pro.verron.asciidoc.docx {
    requires org.jspecify;
    requires org.slf4j;

    requires transitive org.docx4j.core;
    requires org.docx4j.openxml_objects;
    requires jakarta.xml.bind;

    requires pro.verron.asciidoc.core;
    requires pro.verron.asciidoc.converters;

    exports pro.verron.asciidoc.docx;
    opens pro.verron.asciidoc.docx;
}
