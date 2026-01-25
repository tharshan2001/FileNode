package File.Node.utils.FileConvertor;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * Converts images to web-optimized format with optional width, height, and quality.
 */
public class ImageWebConverter implements WebOptimizedConverter {

    private final String targetFormat; // jpg, webp, png

    public ImageWebConverter(String targetFormat) {
        this.targetFormat = targetFormat;
    }

    @Override
    public void convert(File inputFile, File outputFile) throws IOException {
        // default: keep original size, 85% quality
        convert(inputFile, outputFile, null, null, 85);
    }

    @Override
    public void convert(File inputFile, File outputFile,
                        Integer width, Integer height, Integer qualityPercent)
            throws IOException {

        BufferedImage original = ImageIO.read(inputFile);
        if (original == null) throw new IOException("Unsupported image format");

        // Determine target width/height
        int targetWidth = (width != null) ? width : original.getWidth();
        int targetHeight = (height != null) ? height : original.getHeight();

        // Maintain aspect ratio if only width or height is provided
        double aspect = (double) original.getWidth() / original.getHeight();
        if (width != null && height == null) targetHeight = (int) (targetWidth / aspect);
        if (height != null && width == null) targetWidth = (int) (targetHeight * aspect);

        // Resize image
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, targetWidth, targetHeight);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(original, 0, 0, targetWidth, targetHeight, null);
        g.dispose();

        // Determine quality
        float q = (qualityPercent != null) ? qualityPercent / 100f : 0.85f;
        if (q < 0f) q = 0f;
        if (q > 1f) q = 1f;

        // Write image
        try (FileOutputStream fos = new FileOutputStream(outputFile);
             ImageOutputStream ios = ImageIO.createImageOutputStream(fos)) {

            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(targetFormat);
            if (!writers.hasNext()) throw new IOException("No writer for format " + targetFormat);

            ImageWriter writer = writers.next();
            writer.setOutput(ios);

            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(q);
            }

            writer.write(null, new IIOImage(resized, null, null), param);
            writer.dispose();
        }
    }

    @Override
    public String getTargetExtension() {
        return targetFormat;
    }
}
