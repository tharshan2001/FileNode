package File.Node.storage.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class AudioWebConverter implements WebOptimizedConverter {

    @Override
    public void convert(File inputFile, File outputFile) throws IOException {
        // Optional: Convert mp3 → AAC/OGG for web
        // Can integrate FFmpeg similar to VideoWebConverter
        Files.copy(inputFile.toPath(), outputFile.toPath());
    }

    @Override
    public String getTargetExtension() {
        return "mp3";
    }
}
