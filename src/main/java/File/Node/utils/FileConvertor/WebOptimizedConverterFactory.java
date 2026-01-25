package File.Node.utils.FileConvertor;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class WebOptimizedConverterFactory {

    private final Map<String, WebOptimizedConverter> registry = new HashMap<>();

    public WebOptimizedConverterFactory() {
        // Only provide default target format, quality will be dynamic
        registry.put("image", new ImageWebConverter("jpg"));
        registry.put("video", new VideoWebConverter("webm", 1920)); // width example
        registry.put("audio", new AudioWebConverter());
        registry.put("document", new DocumentWebConverter());
    }

    public Optional<WebOptimizedConverter> getConverter(String mimeType) {
        if (mimeType == null) return Optional.empty();
        if (mimeType.startsWith("image/")) return Optional.ofNullable(registry.get("image"));
        if (mimeType.startsWith("video/")) return Optional.ofNullable(registry.get("video"));
        if (mimeType.startsWith("audio/")) return Optional.ofNullable(registry.get("audio"));
        if (mimeType.equals("application/pdf")
                || mimeType.equals("application/msword")
                || mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
            return Optional.ofNullable(registry.get("document"));
        }
        return Optional.empty();
    }

    public void registerConverter(String key, WebOptimizedConverter converter) {
        registry.put(key, converter);
    }
}
