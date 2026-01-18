package cloud.box.storage.repository;

import cloud.box.storage.model.FileMetadata;
import cloud.box.storage.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    // Find all files by User entity
    List<FileMetadata> findByUser(User user);

    // Find a single file by its unique file key
    Optional<FileMetadata> findByFileKey(String fileKey);
}