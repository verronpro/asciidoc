package pro.verron.asciidoc.preview;

import org.asciidoctor.Asciidoctor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

/// Unit test for functionalities related to AsciiDoc preview generation.
///
/// The [AsciiDocPreviewTest] class sets up and tears down the testing
/// environment for
/// working with AsciiDoc templates. It facilitates the integration and testing
/// of
/// AsciiDoc processing with a block macro extension for generating previews.
///
/// The key responsibilities of this class include:
/// 1. Initializing and configuring the Asciidoctor instance with the required
/// block macro extension.
/// 2. Managing temporary directories and template files used for the previews.
/// 3. Cleaning up resources after test execution.
///
/// Methods:
/// - [#setUp()]: Initializes the testing environment and registers the block
///  macro extension.
/// - [#tearDown()]: Cleans up resources, ensuring the Asciidoctor instance is
/// closed.
///
/// Fields:
/// - [templatePath]: Path to the temporary AsciiDoc template used for preview
/// generation.
/// - [asciidoctor]: Instance of Asciidoctor used for processing AsciiDoc
/// templates.
/// - [tempDir]: Temporary directory used for creating and managing test
/// artifacts.
///
/// Usage of this class is intended as a base class for more specific test
/// implementations
/// involving AsciiDoc preview functionality.
public class AsciiDocPreviewTest {
    Path templatePath;
    Asciidoctor asciidoctor;
    @TempDir Path tempDir;

    /// Unit test base class for verifying AsciiDoc preview generation
    /// functionality.
    ///
    /// The [AsciiDocPreviewTest] class provides the necessary setup and teardown
    /// mechanisms for testing the integration of AsciiDoc preview
    /// functionality, including an AsciiDoc block macro extension for
    /// generating preview images.
    ///
    /// Responsibilities:
    /// - Initializes [Asciidoctor] instance with block macro extension.
    /// - Manages a temporary directory used for testing AsciiDoc previews.
    /// - Cleans up resources, such as the [Asciidoctor] instance, after test.
    ///
    /// This class is intended to be extended by specific test implementations,
    /// such as [AsciiDocPreviewExtensionTest], to run and validate their
    /// respective tests.
    public AsciiDocPreviewTest() {}

    @BeforeEach
    void setUp() {
        // Prepare a template adoc
        templatePath = tempDir.resolve("template.adoc");
        asciidoctor = Asciidoctor.Factory.create();
        var extensionRegistry = asciidoctor.javaExtensionRegistry();
        extensionRegistry.blockMacro(AsciiDocPreviewBlockMacro.class);
    }

    @AfterEach
    void tearDown() {
        asciidoctor.close();
    }
}
