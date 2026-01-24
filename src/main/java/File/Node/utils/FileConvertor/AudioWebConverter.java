package File.Node.utils.FileConvertor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class AudioWebConverter implements WebOptimizedConverter {

    @Override
    public void convert(File inputFile, File outputFile) throws IOException {
        // For now, just copy the file
        // You can integrate FFmpeg for real conversion later
        Files.copy(inputFile.toPath(), outputFile.toPath());
    }

    @Override
    public String getTargetExtension() {
        return "mp3"; // all audio files will be saved as .mp3
    }
}
