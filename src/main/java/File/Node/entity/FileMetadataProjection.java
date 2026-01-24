package File.Node.entity;

import java.time.LocalDateTime;

public interface FileMetadataProjection {
    String getFilename();
    String getRelativePath();
    String getFileKey();
    LocalDateTime getUploadedAt();
}

