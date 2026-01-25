package File.Node.service.File;

import File.Node.entity.Cube;
import File.Node.entity.FileMetadata;
import File.Node.entity.User;
import File.Node.repository.FileMetadataRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FileUploadService {

    private final FileStorageService storageService;
    private final FileMetadataRepository metadataRepository;
    private final VideoConversionService videoConversionService;

    public FileUploadService(FileStorageService storageService,
                             FileMetadataRepository metadataRepository,
                             VideoConversionService videoConversionService) {
        this.storageService = storageService;
        this.metadataRepository = metadataRepository;
        this.videoConversionService = videoConversionService;
    }

    /**
     * Save uploaded files without worrying about overwriting.
     * Returns list of fileKeys for reference.
     */
    public List<String> saveFiles(Cube cube, User user, MultipartFile[] files) throws IOException {
        List<String> fileKeys = new ArrayList<>();

        for (MultipartFile file : files) {
            String originalName = file.getOriginalFilename();
            if (originalName == null) originalName = "unknown";

            // Generate unique fileKey for metadata
            String fileKey = UUID.randomUUID().toString();

            // Generate unique filename on disk (original name + random suffix)
            String uniqueFilename = appendRandomSuffix(originalName);

            // Save file
            storageService.saveFile(file, user.getId(), cube.getId(), uniqueFilename);

            // Save metadata
            FileMetadata meta = new FileMetadata();
            meta.setFilename(originalName); // keep original name in metadata
            meta.setRelativePath(user.getId() + "/" + cube.getId() + "/" + uniqueFilename); // actual stored file
            meta.setFileKey(fileKey);
            meta.setUploadedAt(LocalDateTime.now());
            meta.setUser(user);
            meta.setCube(cube);
            metadataRepository.save(meta);

            fileKeys.add(fileKey);

            // If video, trigger async conversion to WebM using fileKey.webm
            if (file.getContentType() != null && file.getContentType().startsWith("video/")) {
                Path originalPath = storageService.getFilePath(user.getId(), cube.getId(), uniqueFilename);
                Path webmPath = storageService.getFilePath(user.getId(), cube.getId(), fileKey + ".webm");
                videoConversionService.convertVideoAsync(originalPath.toFile(), webmPath.toFile());
            }
        }

        return fileKeys;
    }

    /**
     * Append a short random suffix to the original filename.
     */
    private String appendRandomSuffix(String originalName) {
        String baseName;
        String ext = "";

        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex != -1) {
            baseName = originalName.substring(0, dotIndex);
            ext = originalName.substring(dotIndex); // includes dot
        } else {
            baseName = originalName;
        }

        String suffix = UUID.randomUUID().toString().substring(0, 8); // short random suffix
        return baseName + "_" + suffix + ext;
    }
}
