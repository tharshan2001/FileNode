package cloud.box.storage.model;

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

    private String username;
    private String password;
    private String apiKey;

    // One user can have many files
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<FileMetadata> files = new ArrayList<>();

    public User() {}

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.apiKey = UUID.randomUUID().toString();
    }

    // Utility method to add a file
    public void addFile(FileMetadata file) {
        files.add(file);
        file.setUser(this);
    }

    // Utility method to remove a file
    public void removeFile(FileMetadata file) {
        files.remove(file);
        file.setUser(null);
    }
}