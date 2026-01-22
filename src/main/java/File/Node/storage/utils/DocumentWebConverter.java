package File.Node.storage.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class DocumentWebConverter implements WebOptimizedConverter {

    @Override
    public void convert(File inputFile, File outputFile) throws IOException {
        // Optional: Convert DOCX → PDF or optimize PDF
        Files.copy(inputFile.toPath(), outputFile.toPath());
    }

    @Override
    public String getTargetExtension() {
        return "pdf";
    }
}

