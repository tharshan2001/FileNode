package cloud.box.storage.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class FileMetadata {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String filename;
    private String owner;
    private String relativePath;
    private String fileKey;
    private LocalDateTime uploadedAt;

    public FileMetadata() {}
    public FileMetadata(String filename, String owner, String relativePath, String fileKey, LocalDateTime uploadedAt){
        this.filename = filename;
        this.owner = owner;
        this.relativePath = relativePath;
        this.fileKey = fileKey;
        this.uploadedAt = uploadedAt;
    }

    // getters & setters
}