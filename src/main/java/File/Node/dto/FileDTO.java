package File.Node.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class FileDTO {
    private String filename;
    private String relativePath;
    private String fileKey;
    private LocalDateTime uploadedAt;
}