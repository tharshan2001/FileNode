package File.Node.storage.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "\"user\"")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // replaced username with name

    @Column(unique = true, nullable = false)
    private String email; // new email field, required and unique

    private String password;
    private String apiKey;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<FileMetadata> files = new ArrayList<>();

    public User() {
        this.apiKey = UUID.randomUUID().toString(); // always generate
    }

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.apiKey = UUID.randomUUID().toString();
    }

    @PrePersist
    public void prePersist() {
        if (this.apiKey == null || this.apiKey.isEmpty()) {
            this.apiKey = UUID.randomUUID().toString();
        }
    }

    public void addFile(FileMetadata file) {
        files.add(file);
        file.setUser(this);
    }

    public void removeFile(FileMetadata file) {
        files.remove(file);
        file.setUser(null);
    }
}