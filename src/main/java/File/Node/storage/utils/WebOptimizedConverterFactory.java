package File.Node.storage.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class WebOptimizedConverterFactory {

    private final Map<String, WebOptimizedConverter> registry = new HashMap<>();

    public WebOptimizedConverterFactory() {
        // Register converters
        registry.put("image", new ImageWebConverter("webp", 0.95f));
        registry.put("video", new VideoWebConverter("webm", 20));
        registry.put("audio", new AudioWebConverter());
        registry.put("document", new DocumentWebConverter());
    }

    /**
     * Get converter by MIME type
     */
    public Optional<WebOptimizedConverter> getConverter(String mimeType) {
        if (mimeType == null) return Optional.empty();

        if (mimeType.startsWith("image/")) return Optional.ofNullable(registry.get("image"));
        if (mimeType.startsWith("video/")) return Optional.ofNullable(registry.get("video"));
        if (mimeType.startsWith("audio/")) return Optional.ofNullable(registry.get("audio"));
        if (mimeType.equals("application/pdf") || mimeType.equals("application/msword"))
            return Optional.ofNullable(registry.get("document"));

        return Optional.empty();
    }

    /**
     * Allow dynamic registration of new converters at runtime
     */
    public void registerConverter(String key, WebOptimizedConverter converter) {
        registry.put(key, converter);
    }
}
