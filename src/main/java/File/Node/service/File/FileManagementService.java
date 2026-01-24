package File.Node.service.File;

import File.Node.entity.Cube;
import File.Node.entity.FileMetadata;
import File.Node.entity.User;
import File.Node.repository.FileMetadataRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class FileManagementService {

    private final FileStorageService storageService;
    private final FileMetadataRepository metadataRepository;

    public FileManagementService(FileStorageService storageService,
                                 FileMetadataRepository metadataRepository) {
        this.storageService = storageService;
        this.metadataRepository = metadataRepository;
    }

    public List<FileMetadata> listFiles(Cube cube) {
        return metadataRepository.findByCube(cube);
    }

    public String deleteFile(User user, String fileKey) throws IOException {
        FileMetadata meta = metadataRepository.findByFileKey(fileKey).orElse(null);
        if (meta == null) return "NOT_FOUND";
        if (!meta.getUser().getId().equals(user.getId())) return "UNAUTHORIZED";

        String[] parts = meta.getRelativePath().split("/", 2);
        String cubeId = parts[0];
        String filename = parts[1];

        storageService.deleteFile(cubeId, filename);
        metadataRepository.delete(meta);

        return "OK";
    }
}
