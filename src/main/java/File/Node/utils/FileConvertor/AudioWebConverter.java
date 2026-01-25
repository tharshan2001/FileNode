package File.Node.utils.FileConvertor;

import java.io.File;
import java.io.IOException;

/**
 * Convert audio files to web-friendly format (ogg)
 */
public class AudioWebConverter implements WebOptimizedConverter {

    @Override
    public void convert(File inputFile, File outputFile) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", "-i", inputFile.getAbsolutePath(),
                "-c:a", "libvorbis",
                outputFile.getAbsolutePath()
        );
        Process process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) throw new IOException("Audio conversion failed");
    }

    @Override
    public String getTargetExtension() {
        return "ogg";
    }
}
