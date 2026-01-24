package File.Node.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CubeDTO {
    private Long id;
    private String name;
    private String description;
    private String apiKey;
    private String apiSecret;
}
