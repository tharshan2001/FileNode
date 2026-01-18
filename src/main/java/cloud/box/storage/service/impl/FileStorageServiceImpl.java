package cloud.box.storage.service.impl;

import cloud.box.storage.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path storagePath;

    public FileStorageServiceImpl(@Value("${storage.location}") String storageLocation) throws IOException {
        storagePath = Paths.get(storageLocation);
        if (!Files.exists(storagePath)) {
            Files.createDirectories(storagePath);
        }
    }

    /**
     * Save file in /storage/{owner}/ with unique name
     * Does NOT overwrite existing files
     */
    @Override
    public void saveFile(MultipartFile file, String owner) throws IOException {
        Path userFolder = storagePath.resolve(owner);
        if (!Files.exists(userFolder)) {
            Files.createDirectories(userFolder);
        }

        // Create unique filename with timestamp
        String uniqueFilename = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        Path destination = userFolder.resolve(uniqueFilename);

        // Save file
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Get the file path for a given owner and filename
     */
    @Override
    public Path getFilePath(String filename, String owner) {
        return storagePath.resolve(owner).resolve(filename);
    }

    /**
     * Delete file
     */
    @Override
    public void deleteFile(String filename, String owner) throws IOException {
        Files.deleteIfExists(storagePath.resolve(owner).resolve(filename));
    }
}