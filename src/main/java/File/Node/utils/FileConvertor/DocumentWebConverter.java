package File.Node.utils.FileConvertor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class DocumentWebConverter implements WebOptimizedConverter {

    @Override
    public void convert(File inputFile, File outputFile) throws IOException {
        String name = inputFile.getName().toLowerCase();
        if (name.endsWith(".pdf")) {
            // Already PDF, just copy
            Files.copy(inputFile.toPath(), outputFile.toPath());
        } else {
            // For DOC/DOCX, ideally convert to PDF using Apache POI or LibreOffice
            // For now, copy with .pdf extension as placeholder
            Files.copy(inputFile.toPath(), outputFile.toPath());
        }
    }

    @Override
    public String getTargetExtension() {
        return "pdf";
    }
}
