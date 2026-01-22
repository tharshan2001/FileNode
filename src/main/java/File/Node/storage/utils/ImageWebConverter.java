package File.Node.storage.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.IIOImage;

public class ImageWebConverter implements WebOptimizedConverter {

    private final String format; // "webp" or "avif"
    private final float quality; // 0.0 - 1.0

    public ImageWebConverter(String format, float quality) {
        this.format = format;
        this.quality = quality;
    }

    @Override
    public void convert(File inputFile, File outputFile) throws IOException {
        BufferedImage image = ImageIO.read(inputFile);
        ImageWriter writer = ImageIO.getImageWritersByFormatName(format).next();

        try (var ios = ImageIO.createImageOutputStream(outputFile)) {
            writer.setOutput(ios);
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality);
            }
            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }
    }

    @Override
    public String getTargetExtension() {
        return format;
    }
}
