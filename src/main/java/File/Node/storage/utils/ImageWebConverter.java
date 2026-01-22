package File.Node.storage.utils;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Iterator;

public class ImageWebConverter implements WebOptimizedConverter {

    private final String targetFormat; // "png" or "jpg"
    private final float quality;       // 0.0f - 1.0f (only used for JPEG)

    public ImageWebConverter(String targetFormat, float quality) {
        this.targetFormat = targetFormat.toLowerCase();
        this.quality = quality;
    }

    @Override
    public void convert(File inputFile, File outputFile) throws IOException {
        try (InputStream is = new FileInputStream(inputFile)) {
            convert(is, outputFile);
        }
    }

    private void convert(InputStream input, File outputFile) throws IOException {
        BufferedImage image = ImageIO.read(input);
        if (image == null) throw new IOException("Invalid image input");

        if (targetFormat.equals("jpg") || targetFormat.equals("jpeg")) {
            // Handle JPEG quality
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            if (!writers.hasNext()) throw new IOException("No JPEG writer found");

            ImageWriter writer = writers.next();
            try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputFile)) {
                writer.setOutput(ios);
                ImageWriteParam param = writer.getDefaultWriteParam();
                if (param.canWriteCompressed()) {
                    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    param.setCompressionQuality(quality);
                }
                writer.write(null, new IIOImage(image, null, null), param);
                writer.dispose();
            }
        } else {
            // For PNG or other supported formats
            if (!ImageIO.write(image, targetFormat, outputFile)) {
                throw new IOException("Failed to write image in format: " + targetFormat);
            }
        }
    }

    @Override
    public String getTargetExtension() {
        return targetFormat;
    }
}
