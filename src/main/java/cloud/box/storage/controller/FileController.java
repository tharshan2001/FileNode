package cloud.box.storage.controller;

import cloud.box.storage.model.FileMetadata;
import cloud.box.storage.repository.FileMetadataRepository;
import cloud.box.storage.repository.UserRepository;
import cloud.box.storage.service.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
public class FileController {

    @Autowired private FileStorageService storageService;
    @Autowired private FileMetadataRepository metadataRepository;
    @Autowired private UserRepository userRepository;

    private boolean authorize(String apiKey){ return userRepository.findByApiKey(apiKey).isPresent(); }
    private String getUsername(String apiKey){ return userRepository.findByApiKey(apiKey).map(u->u.getUsername()).orElse(null); }

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam MultipartFile file, @RequestParam String apiKey, HttpServletRequest request) throws IOException {
        if(!authorize(apiKey)) return ResponseEntity.status(401).body("Invalid API key");
        String owner = getUsername(apiKey);

        storageService.saveFile(file, owner);
        String relativePath = owner + "/" + file.getOriginalFilename();
        String fileKey = UUID.randomUUID().toString();

        metadataRepository.save(new FileMetadata(file.getOriginalFilename(), owner, relativePath, fileKey, LocalDateTime.now()));

        String baseUrl = request.getScheme() + "://" + request.getServerName();
        if(request.getServerPort() != 80 && request.getServerPort() != 443) baseUrl += ":" + request.getServerPort();
        String streamingUrl = baseUrl + "/meta/" + fileKey;

        return ResponseEntity.ok(streamingUrl);
    }

    @GetMapping("/meta/{fileKey}")
    public void streamFile(@PathVariable String fileKey, HttpServletResponse response) throws IOException {
        FileMetadata meta = metadataRepository.findByFileKey(fileKey).orElse(null);
        if(meta == null){ response.setStatus(404); return; }

        Path file = storageService.getFilePath(meta.getFilename(), meta.getOwner());
        if(!Files.exists(file)){ response.setStatus(404); return; }

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + meta.getFilename() + "\"");

        try(InputStream in = Files.newInputStream(file); OutputStream out = response.getOutputStream()){
            byte[] buffer = new byte[4096];
            int bytesRead;
            while((bytesRead=in.read(buffer))!=-1) out.write(buffer,0,bytesRead);
        }
    }

    @GetMapping("/my-files")
    public List<FileMetadata> listFiles(@RequestParam String apiKey){
        if(!authorize(apiKey)) return null;
        String owner = getUsername(apiKey);
        return metadataRepository.findByOwner(owner);
    }
}