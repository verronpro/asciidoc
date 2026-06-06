package pro.verron.asciidoc.preview;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.jruby.extension.spi.ExtensionRegistry;

/// Registry class for integrating the AsciiDoc preview extension with
/// Asciidoctor.
///
/// This class is responsible for registering the [AsciiDocPreviewBlockMacro]
/// extension, enabling the inclusion of visual previews of AsciiDoc templates
/// within a document. The macro supports rendering previews in formats such as
/// PNG or SVG with customizable attributes like theme and DPI.
///
/// The macro is registered with a default name `preview` and processes
/// attributes to configure the generation of template previews. Users can
/// embed previews of external AsciiDoc files using the block macro syntax.
///
/// By integrating this registry, developers can enhance the capabilities of
/// their Asciidoctor instance by enabling this extension.
///
/// Example use cases include generating diagrams or templates as visual images
/// within documentation, creating consistent theming, or using previews for
/// design and development purposes.
public class AsciiDocPreviewExtensionRegistry
        implements ExtensionRegistry {

    private final String macroName;

    /// Constructs a new instance of the [AsciiDocPreviewExtensionRegistry].
    ///
    /// This registry is responsible for registering an AsciiDoc block macro
    /// extension with the default macro name `preview`. The registered macro
    /// allows users to include visual previews of external AsciiDoc templates
    /// directly within a document. The preview can be rendered in various formats
    /// like PNG or SVG and supports customizable attributes such as theme and DPI.
    ///
    /// By using this registry, developers can extend the capabilities of their
    /// Asciidoctor instance to include the functionality provided by the
    /// [AsciiDocPreviewBlockMacro].
    public AsciiDocPreviewExtensionRegistry() {
        macroName = "preview";
    }

    @Override
    public void register(Asciidoctor asciidoctor) {
        var javaExtensionRegistry = asciidoctor.javaExtensionRegistry();
        var macroProcessor = new AsciiDocPreviewBlockMacro(macroName);
        javaExtensionRegistry.blockMacro(macroProcessor);
    }
}
