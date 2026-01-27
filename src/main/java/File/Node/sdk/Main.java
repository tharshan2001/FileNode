package File.Node.sdk;

import File.Node.dto.FileDTO;

import java.io.File;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            // =============================
            // CONFIGURE SDK
            // =============================
            String baseUrl = "http://localhost:8090/"; // replace with your server URL
            String username = "Anton_00";              // your user
            String apiKey = "303e406e-b0dc-40eb-9f1b-33abdb417caa";    // from cube creation
            String apiSecret = "022ee71f-8710-4d60-9466-0845920fce1c"; // from cube creation
            String cubeName = "FireCube";

            FileNodeSDK sdk = new FileNodeSDK(baseUrl, username, apiKey, apiSecret);

            // =============================
            // UPLOAD FILE
            // =============================
            File fileToUpload = new File("/Users/antonabitharshan/Desktop/x1.png"); // make sure this file exists
            String fileKey = sdk.uploadFile(cubeName, fileToUpload);
            System.out.println("Uploaded file key: " + fileKey);

            // =============================
            // LIST FILES
            // =============================
            List<FileDTO> files = sdk.listFiles(cubeName);
            System.out.println("Files in cube:");
            for (FileDTO f : files) {
                System.out.println(f.getFilename() + " -> " + f.getFileKey());
            }

            // =============================
            // DELETE FILE (optional)
            // =============================
            boolean deleted = sdk.deleteFile(fileKey);
            System.out.println("File deleted: " + deleted);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
