package File.Node.service.cube;

import File.Node.dto.CubeDTO;
import File.Node.dto.CubeInfoDTO;
import File.Node.entity.Cube;
import File.Node.entity.User;
import File.Node.repository.CubeRepository;
import File.Node.security.CubeApiSecretUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CubeService {

    private final CubeRepository cubeRepository;
    private final CubeApiSecretUtil secretUtil;

    public CubeService(CubeRepository cubeRepository,
                       CubeApiSecretUtil secretUtil) {
        this.cubeRepository = cubeRepository;
        this.secretUtil = secretUtil;
    }

    public CubeDTO createCube(String name, String description, User owner) {
        if (cubeRepository.existsByNameAndOwner(name, owner)) {
            throw new IllegalArgumentException("Cube with this name already exists for this user");
        }

        Cube cube = new Cube();
        cube.setName(name);
        cube.setDescription(description);
        cube.setOwner(owner);

        cube.setApiKey(UUID.randomUUID().toString());

        String rawSecret = UUID.randomUUID().toString();
        cube.setApiSecret(secretUtil.encode(rawSecret));

        cube = cubeRepository.save(cube);

        return new CubeDTO(cube.getName(), cube.getDescription(), cube.getApiKey(), rawSecret);
    }

    public List<Cube> listUserCubes(User owner) {
        return cubeRepository.findByOwner(owner);
    }

    public List<CubeInfoDTO> listUserCubesInfo(User owner) {
        return cubeRepository.findByOwner(owner)
                .stream()
                .map(cube -> new CubeInfoDTO(
                        cube.getName(),
                        cube.getDescription(),
                        cube.getApiKey()
                ))
                .toList();
    }

    public Cube getCubeByNameForUser(String cubeName, User owner) {
        return cubeRepository.findByNameAndOwner(cubeName, owner)
                .orElseThrow(() -> new RuntimeException("Cube not found"));
    }

    public CubeInfoDTO getCubeInfoByNameForUser(String cubeName, User owner) {
        Cube cube = getCubeByNameForUser(cubeName, owner);
        return new CubeInfoDTO(cube.getName(), cube.getDescription(), cube.getApiKey());
    }

    public Cube getCubeByApiKeyAndSecret(String username, String apiKey, String apiSecret) {
        return cubeRepository.findByApiKey(apiKey)
                .filter(cube ->
                        cube.getOwner().getUsername().equals(username) &&
                                secretUtil.matches(apiSecret, cube.getApiSecret())
                )
                .orElseThrow(() -> new RuntimeException("Invalid cube credentials or unauthorized"));
    }

    public String regenerateSecret(Cube cube) {
        String rawSecret = UUID.randomUUID().toString();
        cube.setApiSecret(secretUtil.encode(rawSecret));
        cubeRepository.save(cube);
        return rawSecret;
    }
}
