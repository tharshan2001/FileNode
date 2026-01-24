package File.Node.utils.FileConvertor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class WebOptimizedConverterFactory {

    private final Map<String, WebOptimizedConverter> registry = new HashMap<>();

    public WebOptimizedConverterFactory() {
        registry.put("image", new ImageWebConverter("webp", 80));
        registry.put("video", new VideoWebConverter("webm", 2000));
        registry.put("audio", new AudioWebConverter());
        registry.put("document", new DocumentWebConverter());
    }


    /**
     * Resolve converter by MIME type
     */
    public Optional<WebOptimizedConverter> getConverter(String mimeType) {
        if (mimeType == null) return Optional.empty();

        if (mimeType.startsWith("image/")) {
            return Optional.ofNullable(registry.get("image"));
        }

        if (mimeType.startsWith("video/")) {
            return Optional.ofNullable(registry.get("video"));
        }

        if (mimeType.startsWith("audio/")) {
            return Optional.ofNullable(registry.get("audio"));
        }

        if (mimeType.equals("application/pdf")
                || mimeType.equals("application/msword")
                || mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
            return Optional.ofNullable(registry.get("document"));
        }

        return Optional.empty();
    }

    /**
     * Optional: allow runtime extension
     */
    public void registerConverter(String key, WebOptimizedConverter converter) {
        registry.put(key, converter);
    }
}
