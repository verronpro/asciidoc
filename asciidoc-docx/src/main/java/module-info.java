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
