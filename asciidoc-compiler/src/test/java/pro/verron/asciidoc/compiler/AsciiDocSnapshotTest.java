package pro.verron.asciidoc.compiler;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/// This class contains snapshot tests for AsciiDoc rendering with different
/// themes. The tests generate visual representations of AsciiDoc
/// documents in PNG format and compare them against predefined golden files to
/// detect any rendering inconsistencies.
///
/// The test outputs are saved in a directory and compared with the expected
/// outputs using a tolerance for any minor differences.
class AsciiDocSnapshotTest {

    private static final Path GOLDEN_DIR = Path.of(
            "src/test/resources/pro/verron/asciidoc/compiler/golden");
    private static final Path ACTUAL_DIR = Path.of("target/test-snapshots");

    @BeforeAll
    static void setup()
            throws IOException {
        Files.createDirectories(ACTUAL_DIR);
    }

    @CsvSource({
            "word-basic.adoc,word-basic.png",
            "gdocs-basic.adoc,gdocs-basic.png",
            "libre-basic.adoc,libre-basic.png"
    })
    @ParameterizedTest(name = "{0} theme snapshot")
    @DisplayName("Snapshot tests for different themes")
    void snapshotThemes(String adocTemplate, String imgPath)
            throws IOException {
        var asciidoc = """
                = Document Title
                
                This is a very long paragraph that should definitely wrap into multiple lines because it exceeds the maximum width of the page. We want to make sure that the text wrapping logic is working correctly for all themes and that the line height is preserved.
                
                * Item 1 with some long text that might also wrap if it is long enough for the current page width.
                * Item 2
                
                [source,java]
                ----
                public class Hello {
                    public static void main(String[] args) {
                        System.out.println("Hello World");
                    }
                }
                ----
                
                ____
                This is a blockquote that is also very long and should wrap into multiple lines. The vertical line on the left should grow to match the height of the wrapped text.
                ____
                
                comment::[id="c1", author="John Doe", value="This is a multi-line comment. It is long enough to wrap into several lines in the side panel.", start="1,0"]
                """;

        // Handle theme based on name
        if (adocTemplate.startsWith("gdocs"))
            asciidoc = ":theme: gdocs\n" + asciidoc;
        else if (adocTemplate.startsWith("libre"))
            asciidoc = ":theme: libre\n" + asciidoc;

        var actualPath = ACTUAL_DIR.resolve(imgPath);
        AsciiDocCompiler.toImage(asciidoc, actualPath);

        var goldenPath = GOLDEN_DIR.resolve(imgPath);
        SnapshotUtils.assertSnapshotMatch(actualPath, goldenPath, 0.02);
    }
}
