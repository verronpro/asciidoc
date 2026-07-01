/// AsciidoctorJ extension that embeds visual previews of AsciiDoc templates
/// as images within a document.
///
/// ## Exported Packages
/// <dl>
/// <dt>[pro.verron.asciidoc.preview]</dt>
/// <dd>Contains [AsciiDocPreviewExtensionRegistry] and
///     [AsciiDocPreviewBlockMacro], which implement the `preview::` block
///     macro for embedding rendered previews of external AsciiDoc
///     templates.</dd>
/// </dl>
///
/// ## Requirements
/// - [pro.verron.asciidoc.compiler/] &mdash; document compilation and
///   image generation
/// - [pro.verron.asciidoc.core/] &mdash; the AsciiDoc document model
/// - [org.asciidoctor.asciidoctorj/] &mdash; AsciidoctorJ extension SPI
module pro.verron.asciidoc.preview {
    requires pro.verron.asciidoc.compiler;
    requires pro.verron.asciidoc.core;
    requires static org.asciidoctor.asciidoctorj;
    requires java.desktop;
    requires org.asciidoctor.asciidoctorj.api;
    requires org.jspecify;
    requires org.apache.xmlgraphics.batik.transcoder;

    exports pro.verron.asciidoc.preview;
}
