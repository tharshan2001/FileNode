package File.Node.service.File;

import File.Node.entity.FileMetadata;
import File.Node.repository.FileMetadataRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class FileMetadataService {

    private final FileMetadataRepository metadataRepository;

    public FileMetadataService(FileMetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
    }

    /**
     * Fetches file metadata by fileKey with caching.
     */
    @Cacheable(value = "fileMetadataCache", key = "#fileKey")
    public FileMetadata getFileMetadata(String fileKey) {
        return metadataRepository.findByFileKey(fileKey).orElse(null);
    }
}
