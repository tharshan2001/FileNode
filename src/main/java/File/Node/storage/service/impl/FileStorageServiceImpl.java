package File.Node.storage.service.impl;

import File.Node.storage.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path storagePath;

    public FileStorageServiceImpl(@Value("${storage.location}") String storageLocation)
            throws IOException {

        this.storagePath = Paths.get(storageLocation);
        Files.createDirectories(storagePath);
    }

    @Override
    public void saveFile(MultipartFile file, String owner, String filename)
            throws IOException {

        Path userFolder = storagePath.resolve(owner);
        Files.createDirectories(userFolder);

        Path destination = userFolder.resolve(filename);

        Files.copy(
                file.getInputStream(),
                destination,
                StandardCopyOption.REPLACE_EXISTING
        );
    }

    @Override
    public Path getFilePath(String owner, String filename) {
        return storagePath.resolve(owner).resolve(filename);
    }

    @Override
    public void deleteFile(String owner, String filename) throws IOException {
        Files.deleteIfExists(getFilePath(owner, filename));
    }
}