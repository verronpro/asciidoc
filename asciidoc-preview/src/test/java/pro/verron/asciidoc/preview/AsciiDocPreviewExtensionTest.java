package pro.verron.asciidoc.preview;

import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;

import static java.nio.file.Files.exists;
import static java.nio.file.Files.getLastModifiedTime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AsciiDocPreviewExtensionTest
        extends AsciiDocPreviewTest {

    @Test
    void shouldGeneratePreviewImage()
            throws IOException {
        Files.writeString(templatePath, """
                = Template Title
                
                This is a template paragraph.""");

        // Main adoc with the macro
        var adoc = "preview::template.adoc[theme=word,format=png,dpi=96]";


        var tempDirAbsolutePath = tempDir.toAbsolutePath();
        var tempDirAbsolutePathString = tempDirAbsolutePath.toString();
        var attrs = Attributes.builder()
                              .attribute("docdir", tempDirAbsolutePathString)
                              .attribute("outdir", tempDirAbsolutePathString)
                              .build();
        var options = Options.builder()
                             .safe(SafeMode.UNSAFE)
                             .baseDir(tempDir.toFile())
                             .attributes(attrs)
                             .build();

        var html = asciidoctor.convert(adoc, options);

        // Check if image was generated
        var imagePath = tempDir.resolve("template-word-96.png");
        var previewMessage = "Image should be generated at %s";
        assertTrue(exists(imagePath), previewMessage.formatted(imagePath));

        var htmlPreviewMessage = "HTML should contain image tag";
        var hasPreviewImage = html.contains("src=\"template-word-96.png\"");
        assertTrue(hasPreviewImage, htmlPreviewMessage);

    }

    @Test
    void shouldCachePreviewImage()
            throws IOException, InterruptedException {
        Files.writeString(templatePath, """
                = Template Title
                
                This is a template paragraph.""");

        // Main adoc with the macro
        var adoc = "preview::template.adoc[theme=word,format=png,dpi=96]";

        var tempDirAbsolutePath = tempDir.toAbsolutePath();
        var tempDirAbsolutePathString = tempDirAbsolutePath.toString();
        var attrs = Attributes.builder()
                              .attribute("docdir", tempDirAbsolutePathString)
                              .attribute("outdir", tempDirAbsolutePathString)
                              .build();
        var options = Options.builder()
                             .safe(SafeMode.UNSAFE)
                             .baseDir(tempDir.toFile())
                             .attributes(attrs)
                             .build();

        // First run
        asciidoctor.convert(adoc, options);
        var imagePath = tempDir.resolve("template-word-96.png");
        var firstModified = getLastModifiedTime(imagePath).toMillis();

        // Wait a bit to ensure time difference if it were rewritten
        Thread.sleep(100);

        // Second run
        asciidoctor.convert(adoc, options);
        var secondModified = getLastModifiedTime(imagePath).toMillis();

        var noChanges = "Image should not be regenerated if no source changes";
        assertEquals(firstModified, secondModified, noChanges);

        // Update source
        Thread.sleep(100);
        Files.writeString(templatePath, "= Updated Title\n\nNew content.");
        asciidoctor.convert(adoc, options);
        var thirdModified = getLastModifiedTime(imagePath).toMillis();

        var changes = "Image should be regenerated if source changed";
        assertTrue(thirdModified > firstModified, changes);

    }
}
