package File.Node.utils.FileConvertor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class VideoWebConverter implements WebOptimizedConverter {

    private final String targetFormat; // e.g., "mp4" or "webm"
    private final int bitrate; // in kbps

    public VideoWebConverter(String targetFormat, int bitrate) {
        this.targetFormat = targetFormat;
        this.bitrate = bitrate;
    }

    @Override
    public void convert(File inputFile, File outputFile) throws IOException, InterruptedException {
        // Check if ffmpeg is installed
        boolean ffmpegAvailable = true;
        try {
            ProcessBuilder pbCheck = new ProcessBuilder("ffmpeg", "-version");
            Process p = pbCheck.start();
            p.waitFor();
        } catch (IOException e) {
            ffmpegAvailable = false;
        }

        if (ffmpegAvailable) {
            // Use ffmpeg to convert video
            String cmd = String.format("ffmpeg -y -i \"%s\" -b:v %dk -c:v libx264 -preset fast -c:a aac \"%s\"",
                    inputFile.getAbsolutePath(), bitrate, outputFile.getAbsolutePath());
            Process process = Runtime.getRuntime().exec(cmd);
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("FFmpeg failed to convert video. Exit code: " + exitCode);
            }
        } else {
            // Pure Java fallback: copy file as-is
            try (FileInputStream fis = new FileInputStream(inputFile);
                 FileOutputStream fos = new FileOutputStream(outputFile)) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = fis.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
            }
        }
    }

    @Override
    public String getTargetExtension() {
        return targetFormat;
    }
}
