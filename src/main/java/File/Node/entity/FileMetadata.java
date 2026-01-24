package File.Node.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "filedata", indexes = {
        @Index(name = "idx_file_key", columnList = "fileKey"),
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_cube_id", columnList = "cube_id")
})
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filename;       // original filename
    private String relativePath;   // storage path: "cubeId/fileKey"
    private String fileKey;        // unique key for file
    private LocalDateTime uploadedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cube_id", nullable = false)
    private Cube cube;
}
