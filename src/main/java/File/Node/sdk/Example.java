package File.Node.sdk;

import java.io.File;
import java.util.List;
import java.util.Map;

public class Example {

    public static void main(String[] args) throws Exception {
        CloudClient client = new CloudClient("http://localhost:8090", "615ff210-d74c-4a26-976e-3b2d6bd5ab3b");

        // Upload multiple files
        List<String> urls = client.upload(
                new File("/Users/antonabitharshan/Desktop/storage/2/drako.jpg"),
                new File("/Users/antonabitharshan/Desktop/storage/2/drako.jpg")
        );
        urls.forEach(System.out::println);

        // List files
        List<Map<String, Object>> files = client.list();
        files.forEach(f -> System.out.println(f.get("filename") + " -> " + f.get("fileKey")));


        // Delete a file
        if (!files.isEmpty()) {
            boolean deleted = client.delete((String) files.get(0).get("fileKey"));
            System.out.println("Deleted: " + deleted);
        }
    }
}