package cloud.box.storage.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Path;

public interface FileStorageService {
    void saveFile(MultipartFile file, String owner) throws IOException;
    Path getFilePath(String filename, String owner);
    void deleteFile(String filename, String owner) throws IOException;
}