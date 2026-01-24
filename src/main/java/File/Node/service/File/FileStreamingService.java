package File.Node.service.File;

import File.Node.entity.FileMetadata;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class FileStreamingService {

    private final FileStorageService storageService;
    private final FileMetadataService metadataService;

    public FileStreamingService(FileStorageService storageService,
                                FileMetadataService metadataService) {
        this.storageService = storageService;
        this.metadataService = metadataService;
    }

    public void streamFile(String fileKey, HttpServletResponse response) throws IOException {
        FileMetadata meta = metadataService.getFileMetadata(fileKey);
        if (meta == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String[] pathParts = meta.getRelativePath().split("/", 2);
        Path filePath = storageService.getFilePath(pathParts[0], pathParts[1]);
        if (!Files.exists(filePath)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String contentType = Files.probeContentType(filePath);
        if (contentType == null) contentType = "application/octet-stream";

        response.setContentType(contentType);
        response.setHeader("Content-Disposition", "inline; filename=\"" + meta.getFilename() + "\"");
        Files.copy(filePath, response.getOutputStream());
        response.flushBuffer();
    }
}
