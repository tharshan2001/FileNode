package File.Node.service.File;


import File.Node.utils.FileConvertor.ImageWebConverter;
import File.Node.utils.FileConvertor.WebOptimizedConverter;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class FileConversionService {

    public void convertFile(File inputFile, Path targetPath) throws IOException, InterruptedException {
        WebOptimizedConverter converter = new ImageWebConverter("jpg", 0.95f); // 95% quality
        Files.createDirectories(targetPath.getParent());
        converter.convert(inputFile, targetPath.toFile());
    }
}
