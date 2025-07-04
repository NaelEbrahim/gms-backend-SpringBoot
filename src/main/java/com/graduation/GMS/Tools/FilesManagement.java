package com.graduation.GMS.Tools;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
public class FilesManagement {

    public static final String UPLOAD_DIR = "uploads";

    public static String upload(MultipartFile file, Integer id, String folderName) {
        if (file == null || file.isEmpty() || id == null || folderName == null) {
            return null;
        }

        try {
            Path basePath = Paths.get("").toAbsolutePath();
            Path targetDir = basePath.resolve(UPLOAD_DIR).resolve(folderName);

            // Create folder if it doesn't exist
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            // Extract file extension
            String originalName = file.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }

            // Create new filename: {id}.{ext}
            String newFilename = id + extension;
            Path filePath = targetDir.resolve(newFilename);

            // Save file
            file.transferTo(filePath.toFile());

            // Return relative URL
            return "/" + UPLOAD_DIR + "/" + folderName + "/" + newFilename;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
