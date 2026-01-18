package cloud.box.storage.sdk;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

public class CloudClient {

    private final String apiUrl;
    private final String apiKey;

    public CloudClient(String apiUrl, String apiKey){
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
    }

    public String uploadFile(File file) throws IOException{
        URL url = new URL(apiUrl + "/upload?apiKey=" + apiKey);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST"); conn.setDoOutput(true);

        String boundary = "----CloudBoundary";
        conn.setRequestProperty("Content-Type","multipart/form-data; boundary=" + boundary);

        try(OutputStream out = conn.getOutputStream()){
            String header = "--" + boundary + "\r\n" +
                    "Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\n\r\n";
            out.write(header.getBytes());
            out.write(Files.readAllBytes(file.toPath()));
            out.write(("\r\n--" + boundary + "--\r\n").getBytes());
        }

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))){
            return reader.readLine(); // returns streaming link
        }
    }

    public void downloadFile(String fileLink, File saveAs) throws IOException{
        URL url = new URL(fileLink);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try(InputStream in = conn.getInputStream(); FileOutputStream fos = new FileOutputStream(saveAs)){
            byte[] buffer = new byte[4096]; int len;
            while((len=in.read(buffer))!=-1) fos.write(buffer,0,len);
        }
    }
}