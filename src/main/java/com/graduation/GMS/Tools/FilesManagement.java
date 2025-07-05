package com.graduation.GMS.Tools;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Objects;

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
    public static String uploadChatImage(MultipartFile file, int senderId, int receiverId) {
        if (file == null || file.isEmpty()) return null;

        try {
            int first = Math.min(senderId, receiverId);
            int second = Math.max(senderId, receiverId);

            String timestamp = LocalDateTime.now().toString().replace(":", "-");
            String extension = Objects.requireNonNull(file.getOriginalFilename())
                    .substring(file.getOriginalFilename().lastIndexOf("."));

            String fileName = senderId + "_" + timestamp + extension;
            Path projectRoot = Paths.get("").toAbsolutePath();
            Path chatDir = projectRoot.resolve("uploads/chats/" + first + "_" + second);

            if (!Files.exists(chatDir)) {
                Files.createDirectories(chatDir);
            }

            Path filePath = chatDir.resolve(fileName);
            file.transferTo(filePath.toFile());

            // Return relative path
            return "/uploads/chats/" + first + "_" + second + "/" + fileName;

        } catch (IOException e) {
            System.err.println("Error saving chat image: " + e.getMessage());
            return null;
        }
    }

}
