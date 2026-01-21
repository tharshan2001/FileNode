package File.Node.storage.controller;

import File.Node.storage.dto.FileDTO;
import File.Node.storage.model.FileMetadata;
import File.Node.storage.model.User;
import File.Node.storage.repository.FileMetadataRepository;
import File.Node.storage.repository.UserRepository;
import File.Node.storage.service.FileStorageService;
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
    // Resolve user by API key or Authentication
    // =============================
    private User resolveUser(Authentication auth, String apiKey) {
        if (apiKey != null) {
            return userRepository.findByApiKey(apiKey)
                    .orElseThrow(() -> new RuntimeException("Invalid API Key"));
        }
        if (auth != null) {
            // Use email instead of username
            return userRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }
        throw new RuntimeException("No authentication provided");
    }

    // =============================
    // UPLOAD FILES
    // =============================
    @PostMapping("/upload")
    public ResponseEntity<List<String>> uploadMultiple(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(required = false) String apiKey,
            Authentication auth) throws IOException {

        if (files == null || files.length == 0)
            return ResponseEntity.badRequest().body(List.of("No files uploaded"));

        if (files.length > 10)
            return ResponseEntity.badRequest().body(List.of("Max 10 files allowed"));

        User user = resolveUser(auth, apiKey);
        List<String> urls = new ArrayList<>();

        for (MultipartFile file : files) {
            // Generate a unique filename
            String uniqueFilename = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            // Save file
            storageService.saveFile(file, String.valueOf(user.getId()), uniqueFilename);

            // Save metadata
            String fileKey = UUID.randomUUID().toString();
            FileMetadata meta = new FileMetadata(
                    file.getOriginalFilename(),
                    user.getId() + "/" + uniqueFilename,
                    fileKey,
                    LocalDateTime.now(),
                    user
            );
            metadataRepository.save(meta);

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
    public void streamFile(@PathVariable String fileKey, HttpServletResponse response) throws IOException {
        FileMetadata meta = metadataRepository.findByFileKey(fileKey).orElse(null);

        if (meta == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String[] pathParts = meta.getRelativePath().split("/", 2);
        String userId = pathParts[0];
        String filename = pathParts[1];

        Path filePath = storageService.getFilePath(userId, filename);

        if (!Files.exists(filePath)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType(Files.probeContentType(filePath) != null
                ? Files.probeContentType(filePath)
                : "application/octet-stream");

        response.setHeader("Content-Disposition", "inline; filename=\"" + meta.getFilename() + "\"");
        response.setContentLengthLong(Files.size(filePath));
        Files.copy(filePath, response.getOutputStream());
        response.flushBuffer();
    }

    // =============================
    // LIST FILES
    // =============================
    @GetMapping("/my-files")
    public List<FileDTO> listFiles(@RequestParam(required = false) String apiKey, Authentication auth) {
        User user = resolveUser(auth, apiKey);

        return metadataRepository.findByUser(user).stream()
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
    public ResponseEntity<String> deleteFile(@PathVariable String fileKey,
                                             @RequestParam(required = false) String apiKey,
                                             Authentication auth) {

        User user = resolveUser(auth, apiKey);
        FileMetadata meta = metadataRepository.findByFileKey(fileKey).orElse(null);

        if (meta == null)
            return ResponseEntity.status(404).body("File not found");

        if (!meta.getUser().getId().equals(user.getId()))
            return ResponseEntity.status(403).body("Unauthorized");

        try {
            String[] pathParts = meta.getRelativePath().split("/", 2);

            storageService.deleteFile(pathParts[0], pathParts[1]);
            metadataRepository.delete(meta);

            return ResponseEntity.ok("File deleted");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Delete failed");
        }
    }
}