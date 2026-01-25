package File.Node.utils.FileConvertor;

import java.io.File;
import java.io.IOException;

/**
 * Interface for converting files to web-optimized formats.
 */
public interface WebOptimizedConverter {

    /**
     * Convert an input file to a web-optimized file (default method, simple version)
     */
    void convert(File inputFile, File outputFile) throws IOException, InterruptedException;

    /**
     * Convert with optional dynamic parameters: width, height, quality (percent)
     * Default implementation calls the simple convert method
     */
    default void convert(File inputFile, File outputFile,
                         Integer width, Integer height, Integer qualityPercent)
            throws IOException, InterruptedException {
        convert(inputFile, outputFile); // fallback for non-image converters
    }

    /**
     * Return the target web-optimized file extension (without dot)
     */
    String getTargetExtension();
}
