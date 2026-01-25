package File.Node.controller;

import File.Node.dto.CubeDTO;
import File.Node.entity.Cube;
import File.Node.entity.User;
import File.Node.service.cube.CubeService;
import File.Node.utils.user.UserResolverService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/cubes")
public class CubeController {

    private final UserResolverService userResolverService;
    private final CubeService cubeService;

    public CubeController(UserResolverService userResolverService,
                          CubeService cubeService) {
        this.userResolverService = userResolverService;
        this.cubeService = cubeService;
    }

    // ============================
    // CREATE CUBE
    // ============================
    @PostMapping
    public ResponseEntity<CubeDTO> createCube(@RequestParam String name,
                                              @RequestParam(required = false) String description,
                                              Authentication auth) throws IOException {

        User user = userResolverService.resolveUser(auth, null);

        boolean exists = cubeService.listUserCubes(user).stream()
                .anyMatch(c -> c.getName().equals(name));
        if (exists) return ResponseEntity.badRequest().build();

        Cube cube = cubeService.createCube(name, description, user);

        // Create folder for storage
        Path cubePath = Path.of("storage", "users", String.valueOf(user.getId()), cube.getId().toString());
        Files.createDirectories(cubePath);

        return ResponseEntity.ok(cubeService.toDTO(cube));
    }

    // ============================
    // LIST USER CUBES
    // ============================
    @GetMapping
    public List<CubeDTO> listCubes(Authentication auth) {
        User user = userResolverService.resolveUser(auth, null);
        return cubeService.listUserCubes(user).stream()
                .map(cubeService::toDTO)
                .toList();
    }
}
