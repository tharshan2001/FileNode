package File.Node.storage.utils;

import java.io.File;
import java.io.IOException;

public interface WebOptimizedConverter {

    /**
     * Convert an input file to web-optimized format
     * @param inputFile original file
     * @param outputFile destination file (web-optimized)
     * @throws IOException
     * @throws InterruptedException optional for video/ffmpeg
     */
    void convert(File inputFile, File outputFile) throws IOException, InterruptedException;

    /**
     * Return typical web-friendly extension for this file type
     */
    String getTargetExtension();
}
