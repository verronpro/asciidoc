package pro.verron.asciidoc.preview;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.jruby.extension.spi.ExtensionRegistry;

/// Registers the [AsciiDocPreviewBlockMacro] extension with Asciidoctor,
/// enabling the `preview::` block macro for embedding visual previews of
/// AsciiDoc templates within a document.
///
/// @see AsciiDocPreviewBlockMacro
public class AsciiDocPreviewExtensionRegistry
        implements ExtensionRegistry {

    private final String macroName;

    /// Constructs a new registry that registers the `preview` macro.
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
