package File.Node.storage.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Path;

public interface FileStorageService {
    void saveFile(MultipartFile file, String owner, String filename) throws IOException;
    Path getFilePath(String owner, String filename);
    void deleteFile(String owner, String filename) throws IOException;
}