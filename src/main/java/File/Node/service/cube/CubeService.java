package File.Node.service.cube;

import File.Node.entity.Cube;
import File.Node.entity.User;
import File.Node.repository.CubeRepository;
import File.Node.service.File.FileManagementService;
import File.Node.service.File.FileUploadService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CubeService {

    private final CubeRepository cubeRepository;
    private final FileUploadService uploadService;
    private final FileManagementService managementService;

    public CubeService(CubeRepository cubeRepository,
                       FileUploadService uploadService,
                       FileManagementService managementService) {
        this.cubeRepository = cubeRepository;
        this.uploadService = uploadService;
        this.managementService = managementService;
    }

    // CREATE A NEW CUBE
    public Cube createCube(String name, String description, User owner) {
        Cube cube = new Cube();
        cube.setName(name);
        cube.setDescription(description);
        cube.setOwner(owner);

        cube.setApiKey(UUID.randomUUID().toString());
        cube.setApiSecret(UUID.randomUUID().toString());

        return cubeRepository.save(cube);
    }

    // LIST ALL CUBES FOR A USER
    public List<Cube> listUserCubes(User owner) {
        return cubeRepository.findByOwner(owner);
    }

    // RETURN SDK-STYLE WRAPPER
    public CubeWrapper getCube(Long cubeId, User owner) {
        Cube cube = cubeRepository.findById(cubeId)
                .filter(c -> c.getOwner().getId().equals(owner.getId()))
                .orElseThrow(() -> new RuntimeException("Cube not found or unauthorized"));
        return new CubeWrapper(cube, owner, uploadService, managementService);
    }

    // RETURN RAW CUBE ENTITY (needed for uploads, list)
    public Cube getCubeEntity(Long cubeId, User owner) {
        return cubeRepository.findById(cubeId)
                .filter(c -> c.getOwner().getId().equals(owner.getId()))
                .orElseThrow(() -> new RuntimeException("Cube not found or unauthorized"));
    }

    // FIND CUBE BY API KEY
    public Cube getCubeByApiKey(String apiKey) {
        return cubeRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new RuntimeException("Cube not found for API key"));
    }
}
