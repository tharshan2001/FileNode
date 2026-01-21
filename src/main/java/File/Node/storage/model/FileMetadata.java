package File.Node.storage.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "\"filedata\"")
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filename;
    private String relativePath; // can be just filename if you want

    private String fileKey;
    private LocalDateTime uploadedAt;

    // JPA handles storing the foreign key user_id automatically
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public FileMetadata() {}

    public FileMetadata(String filename, String relativePath, String fileKey, LocalDateTime uploadedAt, User user) {
        this.filename = filename;
        this.relativePath = relativePath;
        this.fileKey = fileKey;
        this.uploadedAt = uploadedAt;
        this.user = user;
    }
}