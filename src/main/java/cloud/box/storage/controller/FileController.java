package cloud.box.storage.controller;

import cloud.box.storage.dto.FileDTO;
import cloud.box.storage.model.FileMetadata;
import cloud.box.storage.model.User;
import cloud.box.storage.repository.FileMetadataRepository;
import cloud.box.storage.repository.UserRepository;
import cloud.box.storage.service.FileStorageService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
public class FileController {

    @Autowired
    private FileStorageService storageService;

    @Autowired
    private FileMetadataRepository metadataRepository;

    @Autowired
    private UserRepository userRepository;

    // =============================
    // UPLOAD FILES
    // =============================
    @PostMapping("/upload")
    public ResponseEntity<List<String>> uploadMultiple(
            @RequestParam("files") MultipartFile[] files,
            Authentication auth) throws IOException {

        if (files == null || files.length == 0)
            return ResponseEntity.badRequest().body(List.of("No files uploaded"));

        if (files.length > 10)
            return ResponseEntity.badRequest().body(List.of("Max 10 files allowed"));

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<String> urls = new ArrayList<>();

        for (MultipartFile file : files) {

            // Generate unique filename
            String uniqueFilename = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            // Save file in /storage/{userId}/
            storageService.saveFile(file, String.valueOf(user.getId()));

            // Save metadata with the actual stored filename
            String fileKey = UUID.randomUUID().toString();
            FileMetadata meta = new FileMetadata(
                    file.getOriginalFilename(),                  // original filename
                    user.getId() + "/" + uniqueFilename,        // actual stored path
                    fileKey,
                    LocalDateTime.now(),
                    user
            );
            metadataRepository.save(meta);

            Path fullPath = storageService.getFilePath(uniqueFilename, String.valueOf(user.getId()));

            String url = ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/meta/")
                    .path(fileKey)
                    .toUriString();

            urls.add(url);
        }

        return ResponseEntity.ok(urls);
    }

    // =============================
    // STREAM FILE
    // =============================
    @GetMapping("/meta/{fileKey}")
    public void streamFile(@PathVariable String fileKey,
                           HttpServletResponse response) throws IOException {

        FileMetadata meta = metadataRepository.findByFileKey(fileKey).orElse(null);

        if (meta == null) {
            response.setStatus(404);
            return;
        }

        // Extract stored filename from relativePath
        String[] pathParts = meta.getRelativePath().split("/", 2);
        String storedFilename = pathParts[1]; // everything after userId/

        Path filePath = storageService.getFilePath(storedFilename, pathParts[0]);

        if (!Files.exists(filePath)) {
            response.setStatus(404);
            return;
        }

        response.setContentType(
                Files.probeContentType(filePath) != null
                        ? Files.probeContentType(filePath)
                        : "application/octet-stream"
        );

        response.setHeader(
                "Content-Disposition",
                "inline; filename=\"" + meta.getFilename() + "\""
        );

        Files.copy(filePath, response.getOutputStream());
    }

    // =============================
    // LIST FILES
    // =============================
    @GetMapping("/my-files")
    public List<FileDTO> listFiles(Authentication auth) {

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return metadataRepository.findByUser(user)
                .stream()
                .map(f -> new FileDTO(
                        f.getFilename(),
                        f.getRelativePath(),
                        f.getFileKey(),
                        f.getUploadedAt()
                ))
                .toList();
    }

    // =============================
    // DELETE FILE
    // =============================
    @DeleteMapping("/delete/{fileKey}")
    public ResponseEntity<String> deleteFile(
            @PathVariable String fileKey,
            Authentication auth) {

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        FileMetadata meta = metadataRepository.findByFileKey(fileKey).orElse(null);

        if (meta == null)
            return ResponseEntity.status(404).body("File not found");

        if (!meta.getUser().getId().equals(user.getId()))
            return ResponseEntity.status(403).body("Unauthorized");

        try {
            // Extract stored filename from relativePath
            String[] pathParts = meta.getRelativePath().split("/", 2);
            String storedFilename = pathParts[1];

            Path path = storageService.getFilePath(storedFilename, pathParts[0]);

            Files.deleteIfExists(path);
            metadataRepository.delete(meta);

            return ResponseEntity.ok("File deleted");

        } catch (IOException e) {
            return ResponseEntity.status(500).body("Delete failed");
        }
    }
}