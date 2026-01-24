package File.Node.service.File;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Path;

public interface FileStorageService {
    void saveFile(MultipartFile file, String container, String filename) throws IOException;
    Path getFilePath(String container, String filename);
    void deleteFile(String container, String filename) throws IOException;
}
