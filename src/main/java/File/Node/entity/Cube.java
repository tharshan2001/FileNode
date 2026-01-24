package File.Node.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "cubes", indexes = {
        @Index(name = "idx_owner_id", columnList = "owner_id"),
        @Index(name = "idx_api_key", columnList = "apiKey", unique = true)
})
public class Cube {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;        // unique per user
    private String description; // optional

    private String apiKey;      // unique API key for cube
    private String apiSecret;   // secret for SDK access

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @OneToMany(mappedBy = "cube", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FileMetadata> files;
}
