package File.Node.service.cube;

import File.Node.entity.Cube;
import File.Node.entity.FileMetadata;
import File.Node.entity.User;
import File.Node.service.File.FileManagementService;
import File.Node.service.File.FileUploadService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public class CubeWrapper {

    private final Cube cube;
    private final User owner;
    private final FileUploadService uploadService;
    private final FileManagementService managementService;

    public CubeWrapper(Cube cube, User owner,
                       FileUploadService uploadService,
                       FileManagementService managementService) {
        this.cube = cube;
        this.owner = owner;
        this.uploadService = uploadService;
        this.managementService = managementService;
    }

    public Long getId() { return cube.getId(); }
    public String getName() { return cube.getName(); }
    public String getDescription() { return cube.getDescription(); }
    public String getApiKey() { return cube.getApiKey(); }
    public String getApiSecret() { return cube.getApiSecret(); }

    public List<String> add(MultipartFile[] files) throws IOException {
        return uploadService.saveFiles(cube, owner, files);
    }

    public List<FileMetadata> listFiles() {
        return managementService.listFiles(cube);
    }

    public String delete(String fileKey) throws IOException {
        return managementService.deleteFile(owner, fileKey);
    }
}
