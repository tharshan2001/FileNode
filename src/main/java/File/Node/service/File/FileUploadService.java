package File.Node.service.File;

import File.Node.entity.Cube;
import File.Node.entity.FileMetadata;
import File.Node.entity.User;
import File.Node.repository.FileMetadataRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FileUploadService {

    private final FileStorageService storageService;
    private final FileMetadataRepository metadataRepository;

    public FileUploadService(FileStorageService storageService,
                             FileMetadataRepository metadataRepository) {
        this.storageService = storageService;
        this.metadataRepository = metadataRepository;
    }

    public List<String> saveFiles(Cube cube, User user, MultipartFile[] files) throws IOException {
        List<String> fileKeys = new ArrayList<>();

        Path cubePath = Path.of("storage/cubes", String.valueOf(cube.getId()));
        Files.createDirectories(cubePath);

        for (MultipartFile file : files) {
            String ext = file.getOriginalFilename()
                    .substring(file.getOriginalFilename().lastIndexOf("."));
            String fileKey = UUID.randomUUID().toString();
            String filename = fileKey + ext;

            storageService.saveFile(file, String.valueOf(cube.getId()), filename);

            FileMetadata meta = new FileMetadata();
            meta.setFilename(file.getOriginalFilename());
            meta.setRelativePath(cube.getId() + "/" + filename);
            meta.setFileKey(fileKey);
            meta.setUploadedAt(LocalDateTime.now());
            meta.setUser(user);
            meta.setCube(cube);

            metadataRepository.save(meta);
            fileKeys.add(fileKey);
        }

        return fileKeys;
    }
}
