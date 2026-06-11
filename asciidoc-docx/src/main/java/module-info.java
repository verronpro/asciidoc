import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import pro.verron.asciidoc.core.core.AsciiDocModel;
import pro.verron.asciidoc.docx.AsciiDocToDocx;
import pro.verron.asciidoc.docx.DocxToAsciiDoc;

import java.util.function.Function;

/// Converts between [AsciiDocModel] and Microsoft Word (DOCX) format using
/// Docx4J.
///
/// [AsciiDocToDocx] renders an [AsciiDocModel] into a
/// [WordprocessingMLPackage], while [DocxToAsciiDoc] extracts an
/// [AsciiDocModel] from a [WordprocessingMLPackage].
///
/// ## Exported Packages
/// <dl>
/// <dt>[pro.verron.asciidoc.docx]</dt>
/// <dd>Bidirectional DOCX conversion:
///     [DocxToAsciiDoc] implements
///     [Function]&lt;[WordprocessingMLPackage], [AsciiDocModel]&gt; and
///     [AsciiDocToDocx] implements
///     [Function]&lt;[AsciiDocModel], [WordprocessingMLPackage]&gt;.</dd>
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
