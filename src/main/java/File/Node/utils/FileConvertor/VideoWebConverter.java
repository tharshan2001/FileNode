package File.Node.utils.FileConvertor;

import java.io.File;
import java.io.IOException;

/**
 * Convert videos to web-optimized format using ffmpeg
 */
public class VideoWebConverter implements WebOptimizedConverter {

    private final String targetFormat; // e.g., "webm"
    private final int maxWidth;        // optional resizing

    public VideoWebConverter(String targetFormat, int maxWidth) {
        this.targetFormat = targetFormat;
        this.maxWidth = maxWidth;
    }

    @Override
    public void convert(File inputFile, File outputFile) throws IOException, InterruptedException {
        // Example using ffmpeg command
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", "-i", inputFile.getAbsolutePath(),
                "-vf", "scale='min(" + maxWidth + ",iw)':-2",
                "-c:v", "libvpx-vp9",
                "-c:a", "libopus",
                outputFile.getAbsolutePath()
        );
        Process process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) throw new IOException("Video conversion failed");
    }

    @Override
    public String getTargetExtension() {
        return targetFormat;
    }
}
