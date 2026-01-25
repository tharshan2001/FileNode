package File.Node.controller;

import File.Node.dto.FileDTO;
import File.Node.entity.Cube;
import File.Node.entity.User;
import File.Node.service.File.FileManagementService;
import File.Node.service.File.FileStreamingService;
import File.Node.service.File.FileUploadService;
import File.Node.service.cube.CubeService;
import File.Node.utils.user.UserResolverService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
public class FileController {

    private final UserResolverService userResolverService;
    private final CubeService cubeService;
    private final FileUploadService uploadService;
    private final FileManagementService managementService;
    private final FileStreamingService streamingService;

    public FileController(UserResolverService userResolverService,
                          CubeService cubeService,
                          FileUploadService uploadService,
                          FileManagementService managementService,
                          FileStreamingService streamingService) {
        this.userResolverService = userResolverService;
        this.cubeService = cubeService;
        this.uploadService = uploadService;
        this.managementService = managementService;
        this.streamingService = streamingService;
    }

    // ============================
    // UPLOAD FILES TO CUBE
    // ============================
    @PostMapping("/api/files/{cubeId}")
    public ResponseEntity<List<String>> uploadFiles(@PathVariable Long cubeId,
                                                    @RequestParam("files") MultipartFile[] files,
                                                    Authentication auth) throws IOException {

        User user = userResolverService.resolveUser(auth, null);
        Cube cube = cubeService.getCubeEntity(cubeId, user);

        List<String> fileKeys = uploadService.saveFiles(cube, user, files);
        return ResponseEntity.ok(fileKeys);
    }

    // ============================
    // LIST FILES IN CUBE
    // ============================
    @GetMapping("/files/{cubeId}")
    public List<FileDTO> listFiles(@PathVariable Long cubeId,
                                   Authentication auth) {

        User user = userResolverService.resolveUser(auth, null);
        Cube cube = cubeService.getCubeEntity(cubeId, user);

        return managementService.listFiles(cube).stream()
                .map(f -> new FileDTO(
                        f.getId(),
                        f.getFilename(),
                        f.getRelativePath(),
                        f.getFileKey(),
                        f.getUploadedAt()
                ))
                .toList();
    }

    // ============================
    // STREAM FILE BY FILEKEY
    // ============================
    @GetMapping("/meta/{fileKey}")
    public void streamFile(
            @PathVariable String fileKey,
            @RequestParam(required = false) Integer w,        // width
            @RequestParam(required = false) Integer h,        // height
            @RequestParam(required = false) Integer q,        // JPEG quality
            @RequestParam(required = false) String format,   // output format
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        streamingService.streamFile(fileKey, w, h, q, format, request, response);
    }



    // ============================
    // DELETE FILE BY FILEKEY
    // ============================
    @DeleteMapping("/meta/{fileKey}")
    public ResponseEntity<String> deleteFile(@PathVariable String fileKey,
                                             Authentication auth) throws IOException {

        User user = userResolverService.resolveUser(auth, null);
        String result = managementService.deleteFile(user, fileKey);

        return switch (result) {
            case "OK" -> ResponseEntity.ok("File deleted");
            case "UNAUTHORIZED" -> ResponseEntity.status(403).body("Unauthorized");
            default -> ResponseEntity.status(404).body("File not found");
        };
    }
}
