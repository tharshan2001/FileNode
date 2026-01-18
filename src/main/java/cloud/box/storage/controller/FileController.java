package cloud.box.storage.controller;

import cloud.box.storage.dto.FileDTO;
import cloud.box.storage.model.FileMetadata;
import cloud.box.storage.model.User;
import cloud.box.storage.repository.FileMetadataRepository;
import cloud.box.storage.repository.UserRepository;
import cloud.box.storage.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
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

    // -------------------- UPLOAD --------------------
    @PostMapping("/upload")
    public ResponseEntity<List<String>> uploadMultiple(
            @RequestParam("files") MultipartFile[] files, // must match the field name in request
            Authentication auth) throws IOException {

        if (files.length == 0) {
            return ResponseEntity.badRequest().body(List.of());
        }
        if (files.length > 10) {
            return ResponseEntity.badRequest()
                    .body(List.of("You can upload up to 10 files at a time"));
        }

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<String> urls = new java.util.ArrayList<>();

        for (MultipartFile file : files) {
            storageService.saveFile(file, user.getUsername());

            FileMetadata metadata = new FileMetadata(
                    file.getOriginalFilename(),
                    file.getOriginalFilename(),
                    UUID.randomUUID().toString(),
                    LocalDateTime.now(),
                    user
            );

            metadataRepository.save(metadata);

            String streamingUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/meta/")
                    .path(metadata.getFileKey())
                    .toUriString();

            urls.add(streamingUrl);
        }

        return ResponseEntity.ok(urls);
    }

    // -------------------- PUBLIC STREAMING --------------------
    @GetMapping("/meta/{fileKey}")
    public void streamFile(@PathVariable String fileKey, HttpServletResponse response) throws IOException {
        FileMetadata meta = metadataRepository.findByFileKey(fileKey).orElse(null);
        if (meta == null) {
            response.setStatus(404);
            return;
        }

        Path file = storageService.getFilePath(meta.getFilename(), meta.getUser().getUsername());
        if (!Files.exists(file)) {
            response.setStatus(404);
            return;
        }

        String mimeType = Files.probeContentType(file);
        if (mimeType == null) mimeType = "application/octet-stream";

        response.setContentType(mimeType);
        response.setHeader("Content-Disposition", "inline; filename=\"" + meta.getFilename() + "\"");

        try (InputStream in = Files.newInputStream(file); OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) out.write(buffer, 0, bytesRead);
        }
    }



    // -------------------- LIST FILES --------------------
    @GetMapping("/my-files")
    public List<FileDTO> listFiles(Authentication auth) {
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Fetch files for this user
        List<FileMetadata> files = metadataRepository.findByUser(user);

        // Map to DTOs (exclude User info)
        return files.stream()
                .map(f -> new FileDTO(
                        f.getFilename(),
                        f.getRelativePath(),
                        f.getFileKey(),
                        f.getUploadedAt()
                ))
                .toList();
    }
}