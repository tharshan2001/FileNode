package File.Node.service.File;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public interface FileStorageService {
    void saveFile(MultipartFile file, Long userId, Long cubeId, String filename) throws IOException;
    Path getFilePath(Long userId, Long cubeId, String filename);
    void deleteFile(Long userId, Long cubeId, String filename) throws IOException;
}
