package File.Node.repository;

import File.Node.entity.Cube;
import File.Node.entity.FileMetadata;
import File.Node.entity.FileMetadataProjection;
import File.Node.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    // Find all files by User entity
    List<FileMetadata> findByUser(User user);

    // Find a single file by its unique file key
    Optional<FileMetadata> findByFileKey(String fileKey);

    @Query("SELECT f.filename AS filename, f.relativePath AS relativePath, f.fileKey AS fileKey, f.uploadedAt AS uploadedAt FROM FileMetadata f WHERE f.fileKey = :fileKey")
    Optional<FileMetadataProjection> findByFileKeyProjected(@Param("fileKey") String fileKey);

    List<FileMetadata> findByCube(Cube cube);

}