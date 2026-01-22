package File.Node.storage.utils;

import java.io.File;
import java.io.IOException;

public class VideoWebConverter implements WebOptimizedConverter {

    private final String format; // usually "webm"
    private final int crf;       // 0-best, 63-worst

    public VideoWebConverter(String format, int crf) {
        this.format = format;
        this.crf = crf;
    }

    @Override
    public void convert(File inputFile, File outputFile) throws IOException, InterruptedException {
        if (!inputFile.exists()) throw new IllegalArgumentException("Input file does not exist");

        String command = String.format(
                "ffmpeg -y -i \"%s\" -c:v libvpx-vp9 -crf %d -b:v 0 -c:a libopus \"%s\"",
                inputFile.getAbsolutePath(),
                crf,
                outputFile.getAbsolutePath()
        );

        Process process = Runtime.getRuntime().exec(command);
        int exitCode = process.waitFor();
        if (exitCode != 0) throw new IOException("FFmpeg conversion failed with code " + exitCode);
    }

    @Override
    public String getTargetExtension() {
        return format;
    }
}
