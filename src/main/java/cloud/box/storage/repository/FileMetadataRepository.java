package cloud.box.storage.repository;


import cloud.box.storage.model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    List<FileMetadata> findByOwner(String owner);
    Optional<FileMetadata> findByFileKey(String fileKey);
}