package File.Node.service.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path storagePath;

    public FileStorageServiceImpl(@Value("${storage.location}") String storageLocation) throws IOException {
        this.storagePath = Paths.get(storageLocation);
        Files.createDirectories(storagePath);
    }

    @Override
    public void saveFile(MultipartFile file, String container, String filename) throws IOException {
        Path containerFolder = storagePath.resolve(container);
        Files.createDirectories(containerFolder);
        Path destination = containerFolder.resolve(filename);
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public Path getFilePath(String container, String filename) {
        return storagePath.resolve(container).resolve(filename);
    }

    @Override
    public void deleteFile(String container, String filename) throws IOException {
        Files.deleteIfExists(getFilePath(container, filename));
    }
}
