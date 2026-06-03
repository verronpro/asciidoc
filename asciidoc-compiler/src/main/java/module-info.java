module pro.verron.asciidoc.compiler {
    requires pro.verron.asciidoc.core;
    requires pro.verron.asciidoc.converters;
    requires pro.verron.asciidoc.docx;
    requires org.apache.xmlgraphics.batik.transcoder;
    requires org.apache.xmlgraphics.batik.codec;
    requires java.desktop;
    requires org.docx4j.core;

    exports pro.verron.asciidoc.compiler;
}
