package File.Node.storage.sdk;

import java.io.File;
import java.util.List;
import java.util.Map;

public class Example {

    public static void main(String[] args) throws Exception {
        CloudClient client = new CloudClient("http://localhost:8090", "60b81517-b900-4fb0-a090-1f72e29eb469");

        // Upload multiple files
        List<String> urls = client.upload(
                new File("/Users/antonabitharshan/Desktop/storage/1/1768758164570_d002.jpg"),
                new File("/Users/antonabitharshan/Desktop/storage/1/1768758164570_d002.jpg")
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