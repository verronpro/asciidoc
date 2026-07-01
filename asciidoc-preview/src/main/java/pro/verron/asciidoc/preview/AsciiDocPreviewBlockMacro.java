package pro.verron.asciidoc.preview;

import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.BlockMacroProcessor;
import org.asciidoctor.extension.Name;
import pro.verron.asciidoc.compiler.AsciiDocCompiler;
import pro.verron.asciidoc.core.AsciiDocModel;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.Files.exists;
import static java.nio.file.Files.getLastModifiedTime;
import static java.util.Collections.emptyList;

/// Block macro that embeds a visual preview of an external AsciiDoc template
/// as an image within the current document.
///
/// Usage: `preview::template.adoc[theme=word,format=png,dpi=192]`
///
/// Attributes:
/// - `theme` &mdash; the rendering theme (default: `word`)
/// - `format` &mdash; output format, `png` or `svg` (default: `png`)
/// - `dpi` &mdash; resolution in dots per inch (default: `96`)
///
/// Output images are cached and only regenerated when the source file changes.
///
/// @see AsciiDocPreviewExtensionRegistry
@Name("preview")
public class AsciiDocPreviewBlockMacro extends BlockMacroProcessor {

    /// Constructs a new macro processor with the default macro name.
    public AsciiDocPreviewBlockMacro() {
    }

    /// Constructs a new [AsciiDocPreviewBlockMacro] with the specified macro name.
    ///
    /// @param macroName the name of the macro
    public AsciiDocPreviewBlockMacro(String macroName) {
        super(macroName);
    }

    private static Path getImagesOutDir(Document document, Path docDir) {
        var imagesOutDirAttr = (String) document.getAttribute("imagesoutdir");
        if (imagesOutDirAttr != null) return Paths.get(imagesOutDirAttr);

        var outDirAttr = (String) document.getAttribute("outdir");
        if (outDirAttr != null) return Paths.get(outDirAttr);

        return docDir;
    }

    private static boolean isModifiedLaterThan(Path outputPath, Path adocPath) throws IOException {
        return getLastModifiedTime(outputPath).compareTo(getLastModifiedTime(adocPath)) < 0;
    }

    private static Path getDocDir(Document document) {
        var docDirAttr = (String) document.getAttribute("docdir");
        return docDirAttr == null ? Paths.get(".") : Paths.get(docDirAttr);

    }

    @Override
    public StructuralNode process(StructuralNode parent, String target, Map<String, Object> attr) {
        var attributes = new Attributes(attr);

        var document = parent.getDocument();
        var docDir = getDocDir(document);
        var adocPath = docDir.resolve(target);

        if (!exists(adocPath)) {
            var errorMessage = "Preview file not found: %s".formatted(adocPath.toAbsolutePath());
            return createBlock(parent, "paragraph", errorMessage);
        }

        var theme = attributes.get("theme", "word");
        var format = attributes.get("format", "png");
        var dpi = attributes.get("dpi", 96, Integer::parseInt);

        var hints = new TranscodingHints();
        var mmPerInch = 25.4f;
        hints.put(PNGTranscoder.KEY_BACKGROUND_COLOR, Color.WHITE);
        hints.put(PNGTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER, mmPerInch / dpi);

        try {
            var content = Files.readString(adocPath);
            var model = AsciiDocCompiler.toModel(content);

            // Re-create model with overridden theme
            var newAttributes = new HashMap<>(model.getAttributes());
            newAttributes.put("theme", theme);
            model = AsciiDocModel.of(newAttributes, model.getBlocks());

            var baseName = target.contains(".") ? target.substring(0, target.lastIndexOf('.')) : target;
            var fileName = "%s-%s-%d.%s".formatted(baseName, theme, dpi, format);

            var imagesOutDir = getImagesOutDir(document, docDir);

            var outputPath = imagesOutDir.resolve(fileName);
            Files.createDirectories(imagesOutDir);

            // Caching: check if output exists and is newer than source
            if (!exists(outputPath) || isModifiedLaterThan(outputPath, adocPath)) {
                var svg = AsciiDocCompiler.toSvg(model);
                if ("svg".equalsIgnoreCase(format)) Files.writeString(outputPath, svg);
                else AsciiDocCompiler.saveSvgAsImage(svg, outputPath, hints);
            }

            var imageAttributes = new HashMap<String, Object>();
            imageAttributes.put("target", fileName);
            imageAttributes.put("alt", "Preview of " + target);

            return createBlock(parent, "image", emptyList(), imageAttributes, new HashMap<>());
        } catch (IOException e) {
            return createBlock(parent, "paragraph", "Error generating preview: " + e.getMessage());
        }
    }
}
