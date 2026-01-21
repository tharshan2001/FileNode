package File.Node.storage.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

public class CloudClient {

    private final String apiUrl;
    private final String apiKey;
    private final ObjectMapper mapper = new ObjectMapper();

    public CloudClient(String apiUrl, String apiKey) {
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
    }

    // -----------------------------
    // Upload files easily
    // -----------------------------
    public List<String> upload(File... files) throws IOException {
        if (files == null || files.length == 0) return List.of();

        URL url = new URL(apiUrl + "/upload?apiKey=" + apiKey);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        String boundary = "----CloudBoundary" + System.currentTimeMillis();
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (OutputStream out = conn.getOutputStream()) {
            for (File file : files) {
                String header = "--" + boundary + "\r\n" +
                        "Content-Disposition: form-data; name=\"files\"; filename=\"" + file.getName() + "\"\r\n\r\n";
                out.write(header.getBytes());
                out.write(Files.readAllBytes(file.toPath()));
                out.write("\r\n".getBytes());
            }
            out.write(("--" + boundary + "--\r\n").getBytes());
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            return mapper.readValue(reader.readLine(), new TypeReference<List<String>>() {});
        }
    }

    // -----------------------------
    // List all files for this API key
    // -----------------------------
    public List<Map<String, Object>> list() throws IOException {
        URL url = new URL(apiUrl + "/my-files?apiKey=" + apiKey);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            return mapper.readValue(reader.readLine(), new TypeReference<List<Map<String, Object>>>() {});
        }
    }

    // -----------------------------
    // Download a file
    // -----------------------------
    public void download(String fileUrl, File saveAs) throws IOException {
        URL url = new URL(fileUrl);
        try (InputStream in = url.openStream(); FileOutputStream out = new FileOutputStream(saveAs)) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) != -1) out.write(buffer, 0, len);
        }
    }

    // -----------------------------
    // Delete a file by fileKey
    // -----------------------------
    public boolean delete(String fileKey) throws IOException {
        URL url = new URL(apiUrl + "/delete/" + fileKey + "?apiKey=" + apiKey);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        return conn.getResponseCode() == 200;
    }
}