package File.Node.utils.FileConvertor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageWebConverter implements WebOptimizedConverter {

    private final String targetFormat; // jpg
    private final float quality; // 0.0 - 1.0

    public ImageWebConverter(String targetFormat, float quality) {
        this.targetFormat = targetFormat;
        this.quality = quality;
    }

    @Override
    public void convert(File inputFile, File outputFile) throws IOException {
        BufferedImage original = ImageIO.read(inputFile);
        if (original == null) {
            throw new IOException("Unsupported image format or corrupted file");
        }

        // ✅ Normalize to RGB
        BufferedImage rgbImage = new BufferedImage(
                original.getWidth(),
                original.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );
        Graphics2D g = rgbImage.createGraphics();
        g.setColor(Color.WHITE); // fill background for images with alpha
        g.fillRect(0, 0, rgbImage.getWidth(), rgbImage.getHeight());
        g.drawImage(original, 0, 0, null);
        g.dispose();

        // Write as JPEG with high quality
        javax.imageio.plugins.jpeg.JPEGImageWriteParam jpegParams =
                new javax.imageio.plugins.jpeg.JPEGImageWriteParam(null);
        jpegParams.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
        jpegParams.setCompressionQuality(quality); // e.g., 0.95f

        ImageIO.write(rgbImage, targetFormat, outputFile);
    }

    @Override
    public String getTargetExtension() {
        return targetFormat;
    }
}
