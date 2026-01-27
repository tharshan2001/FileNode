package File.Node.controller;

import File.Node.dto.FileDTO;
import File.Node.dto.ResponseWrapper;
import File.Node.entity.Cube;
import File.Node.entity.User;
import File.Node.security.CurrentUser;
import File.Node.service.File.FileManagementService;
import File.Node.service.File.FileStreamingService;
import File.Node.service.File.FileUploadService;
import File.Node.service.cube.CubeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
public class FileController {

    private final CubeService cubeService;
    private final FileUploadService uploadService;
    private final FileManagementService managementService;
    private final FileStreamingService streamingService;

    public FileController(
            CubeService cubeService,
            FileUploadService uploadService,
            FileManagementService managementService,
            FileStreamingService streamingService
    ) {
        this.cubeService = cubeService;
        this.uploadService = uploadService;
        this.managementService = managementService;
        this.streamingService = streamingService;
    }

    // ============================
    // UPLOAD SINGLE FILE
    // ============================
    @PostMapping(
            value = "/api/files/{cubeName}",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<ResponseWrapper<String>> uploadFile(
            @PathVariable String cubeName,
            @RequestPart("file") MultipartFile file,
            @CurrentUser User user
    ) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, "File is missing", null));
            }

            // fetch cube by name + user
            Cube cube = cubeService.getCubeByNameForUser(cubeName, user);

            String fileKey = uploadService.saveFile(cube, user, file);

            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "File uploaded successfully", fileKey)
            );

        } catch (RuntimeException ex) {
            // Handles cases like cube not found or unauthorized
            return ResponseEntity.status(404)
                    .body(new ResponseWrapper<>(false, ex.getMessage(), null));

        } catch (IOException ex) {
            return ResponseEntity.status(500)
                    .body(new ResponseWrapper<>(false, "Error saving file: " + ex.getMessage(), null));
        } catch (Exception ex) {
            return ResponseEntity.status(500)
                    .body(new ResponseWrapper<>(false, "Unexpected error occurred", null));
        }
    }


    // ============================
    // LIST FILES
    // ============================
    @GetMapping("/api/files/{cubeName}")
    public ResponseEntity<List<FileDTO>> listFiles(
            @PathVariable String cubeName,
            @CurrentUser User user
    ) {
        Cube cube = cubeService.getCubeByNameForUser(cubeName, user);

        List<FileDTO> files = managementService.listFiles(cube)
                .stream()
                .map(f -> new FileDTO(
                        f.getId(),
                        f.getFilename(),
                        f.getRelativePath(),
                        f.getFileKey(),
                        f.getUploadedAt()
                ))
                .toList();

        return ResponseEntity.ok(files);
    }

    // ============================
    // STREAM FILE
    // ============================
    @GetMapping("/meta/{fileKey}")
    public void streamFile(
            @PathVariable String fileKey,
            @RequestParam(required = false) Integer w,
            @RequestParam(required = false) Integer h,
            @RequestParam(required = false) Integer q,
            @RequestParam(required = false) String format,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        streamingService.streamFile(
                fileKey,
                w,
                h,
                q,
                format,
                request,
                response
        );
    }

    // DELETE FILE
    @DeleteMapping("/meta/{fileKey}")
    public ResponseEntity<String> deleteFile(
            @PathVariable String fileKey,
            @CurrentUser User user
    ) throws IOException {

        String result = managementService.deleteFile(user, fileKey);

        return switch (result) {
            case "OK" -> ResponseEntity.ok("File deleted");
            case "UNAUTHORIZED" -> ResponseEntity.status(403).body("Unauthorized");
            default -> ResponseEntity.status(404).body("File not found");
        };
    }
}
