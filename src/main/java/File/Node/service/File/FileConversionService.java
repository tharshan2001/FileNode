package File.Node.service.File;

import File.Node.utils.FileConvertor.ImageWebConverter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class FileConversionService {

    /**
     * Converts a file asynchronously with optional width, height, quality, and format.
     * The HTTP request will return immediately while conversion happens in the background.
     */
    @Async
    public void convertFileAsync(File inputFile,
                                 Path targetPath,
                                 Integer width,
                                 Integer height,
                                 Integer qualityPercent,
                                 String format) {
        try {
            convertFile(inputFile, targetPath, width, height, qualityPercent, format);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace(); // log error, optionally send alert
        }
    }

    /**
     * Synchronous conversion logic
     */
    public void convertFile(File inputFile,
                            Path targetPath,
                            Integer width,
                            Integer height,
                            Integer qualityPercent,
                            String format) throws IOException, InterruptedException {

        // Default format and quality
        String targetFormat = (format != null && !format.isEmpty()) ? format : "jpg";
        int q = (qualityPercent != null) ? qualityPercent : 85; // default 85%

        Files.createDirectories(targetPath.getParent());

        ImageWebConverter converter = new ImageWebConverter(targetFormat);

        // Call the dynamic convert method from ImageWebConverter
        converter.convert(inputFile, targetPath.toFile(), width, height, q);
    }
}
