package File.Node.service.File;

import File.Node.entity.FileMetadata;
import File.Node.utils.FileConvertor.WebOptimizedConverter;
import File.Node.utils.FileConvertor.WebOptimizedConverterFactory;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Service
public class FileStreamingService {

    private final FileStorageService storageService;
    private final FileMetadataService metadataService;
    private final WebOptimizedConverterFactory converterFactory;

    public FileStreamingService(FileStorageService storageService,
                                FileMetadataService metadataService,
                                WebOptimizedConverterFactory converterFactory) {
        this.storageService = storageService;
        this.metadataService = metadataService;
        this.converterFactory = converterFactory;
    }

    /**
     * Stream a file by fileKey.
     * Images are converted dynamically to temp files.
     * Videos support HTTP Range and streaming without overwriting originals.
     */
    public void streamFile(String fileKey,
                           Integer width,
                           Integer height,
                           Integer quality,
                           String format,
                           HttpServletRequest request,
                           HttpServletResponse response) throws IOException {

        // 1️⃣ Get metadata
        FileMetadata meta = metadataService.getFileMetadata(fileKey);
        if (meta == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Long userId = meta.getUser().getId();
        Long cubeId = meta.getCube().getId();

        String[] pathParts = meta.getRelativePath().split("/");
        String storedFilename = pathParts[pathParts.length - 1];

        Path filePath = storageService.getFilePath(userId, cubeId, storedFilename);
        if (!Files.exists(filePath)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String mimeType = Files.probeContentType(filePath);
        if (mimeType == null) mimeType = "application/octet-stream";

        Optional<WebOptimizedConverter> converterOpt = converterFactory.getConverter(mimeType);
        File tmpOutput = null;

        try {
            // ✅ Images: convert dynamically to temp file
            if (converterOpt.isPresent() && !mimeType.startsWith("video/")) {
                WebOptimizedConverter converter = converterOpt.get();
                String targetFormat = (format != null && !format.isEmpty()) ? format : converter.getTargetExtension();

                // Use temp file, original never overwritten
                tmpOutput = File.createTempFile("stream-", "." + targetFormat);
                converter.convert(filePath.toFile(), tmpOutput, width, height, quality);

                response.setContentType(Files.probeContentType(tmpOutput.toPath()));
                response.setHeader("Content-Disposition", "inline; filename=\"" +
                        getFilenameWithFormat(meta.getFilename(), targetFormat) + "\"");

                streamFile(tmpOutput.toPath(), response);

            }
            // ✅ Videos: stream with HTTP Range without touching original
            else if (mimeType.startsWith("video/")) {
                Path webmPath = storageService.getFilePath(userId, cubeId, fileKey + ".webm");
                Path toStream = Files.exists(webmPath) ? webmPath : filePath;

                response.setContentType(Files.probeContentType(toStream));
                response.setHeader("Content-Disposition", "inline; filename=\"" + meta.getFilename() + "\"");
                response.setHeader("Accept-Ranges", "bytes");

                long fileLength = Files.size(toStream);
                String range = request.getHeader("Range");
                long start = 0;
                long end = fileLength - 1;

                if (range != null && range.startsWith("bytes=")) {
                    String[] partsRange = range.replace("bytes=", "").split("-");
                    try {
                        start = Long.parseLong(partsRange[0]);
                        if (partsRange.length > 1 && !partsRange[1].isEmpty()) {
                            end = Long.parseLong(partsRange[1]);
                        }
                    } catch (NumberFormatException ignored) {}
                    if (start > end) start = 0;
                    response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                    response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);
                    response.setHeader("Content-Length", String.valueOf(end - start + 1));
                } else {
                    response.setHeader("Content-Length", String.valueOf(fileLength));
                }

                try (RandomAccessFile raf = new RandomAccessFile(toStream.toFile(), "r");
                     OutputStream out = response.getOutputStream()) {

                    raf.seek(start);
                    long bytesLeft = end - start + 1;
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = raf.read(buffer, 0, (int)Math.min(buffer.length, bytesLeft))) != -1 && bytesLeft > 0) {
                        out.write(buffer, 0, bytesRead);
                        bytesLeft -= bytesRead;
                    }
                    out.flush();
                }
            }
            // ✅ Other files: stream original
            else {
                response.setContentType(mimeType);
                response.setHeader("Content-Disposition", "inline; filename=\"" + meta.getFilename() + "\"");
                response.setHeader("Content-Length", String.valueOf(Files.size(filePath)));
                streamFile(filePath, response);
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        } finally {
            if (tmpOutput != null && tmpOutput.exists()) tmpOutput.delete(); // clean temp file
        }
    }

    /** Buffered streaming helper */
    private void streamFile(Path path, HttpServletResponse response) throws IOException {
        try (InputStream in = Files.newInputStream(path);
             OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
        }
    }

    /** Get filename with format for converted images */
    private String getFilenameWithFormat(String original, String targetFormat) {
        int dotIndex = original.lastIndexOf('.');
        String name = (dotIndex != -1) ? original.substring(0, dotIndex) : original;
        return name + "." + targetFormat;
    }
}
