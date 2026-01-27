package File.Node.sdk;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import File.Node.dto.FileDTO;
import File.Node.dto.ResponseWrapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public class FileNodeSDK {

    private final String baseUrl;
    private final String username;
    private final String apiKey;
    private final String apiSecret;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())       // <-- enable Java 8 date/time support
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // optional

    public FileNodeSDK(String baseUrl, String username, String apiKey, String apiSecret) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        this.username = username;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    // =============================
    // UPLOAD FILE
    // =============================
    public String uploadFile(String cubeName, File file) throws Exception {
        String url = baseUrl + "api/files/" + cubeName;
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(url);

            post.setHeader("X-USERNAME", username);
            post.setHeader("X-API-KEY", apiKey);
            post.setHeader("X-API-SECRET", apiSecret);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addPart("file", new FileBody(file));
            post.setEntity(builder.build());

            ClassicHttpResponse response = client.execute(post);
            InputStream body = response.getEntity().getContent();

            ResponseWrapper<String> wrapper = objectMapper.readValue(body, new TypeReference<>() {});
            if (!wrapper.isSuccess()) { // <-- fixed
                throw new RuntimeException("Upload failed: " + wrapper.getMessage());
            }
            return wrapper.getData(); // returns fileKey
        }
    }

    // =============================
    // LIST FILES
    // =============================
    public List<FileDTO> listFiles(String cubeName) throws Exception {
        String url = baseUrl + "api/files/" + cubeName;
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(url);
            get.setHeader("X-USERNAME", username);
            get.setHeader("X-API-KEY", apiKey);
            get.setHeader("X-API-SECRET", apiSecret);

            ClassicHttpResponse response = client.execute(get);
            InputStream body = response.getEntity().getContent();

            return objectMapper.readValue(body, new TypeReference<List<FileDTO>>() {});
        }
    }

    // =============================
    // DELETE FILE
    // =============================
    public boolean deleteFile(String fileKey) throws Exception {
        String url = baseUrl + "api/files/meta/" + fileKey;
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpDelete delete = new HttpDelete(url);
            delete.setHeader("X-USERNAME", username);
            delete.setHeader("X-API-KEY", apiKey);
            delete.setHeader("X-API-SECRET", apiSecret);

            ClassicHttpResponse response = client.execute(delete);
            int code = response.getCode();
            return code == 200; // true if deleted
        }
    }

    // =============================
    // GET FILE STREAM URL
    // =============================
    public String getFileUrl(String fileKey) {
        return baseUrl + "api/files/meta/" + fileKey;
    }

}
