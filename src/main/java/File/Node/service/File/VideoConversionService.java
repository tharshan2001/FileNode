package File.Node.service.File;


import File.Node.utils.FileConvertor.VideoWebConverter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class VideoConversionService {

    @Async
    public void convertVideoAsync(File inputFile, File outputFile) {
        try {
            VideoWebConverter converter = new VideoWebConverter("webm", 1920);
            converter.convert(inputFile, outputFile);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
