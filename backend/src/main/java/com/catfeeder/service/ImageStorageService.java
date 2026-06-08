package com.catfeeder.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class ImageStorageService {

    @Value("${app.image-storage-path:./uploads/images}")
    private String imageStoragePath;

    private Path storageLocation;

    @PostConstruct
    public void init() {
        storageLocation = Paths.get(imageStoragePath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(storageLocation);
            Files.createDirectories(storageLocation.resolve("captures"));
            Files.createDirectories(storageLocation.resolve("avatars"));
        } catch (IOException e) {
            throw new RuntimeException("无法创建图片存储目录", e);
        }
    }

    public String saveImage(MultipartFile file, String feederId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String extension = getFileExtension(file.getOriginalFilename());
        String filename = String.format("%s_%s_%s%s", feederId, timestamp, uniqueId, extension);

        try {
            Path targetPath = storageLocation.resolve("captures").resolve(filename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return "/api/images/captures/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("保存图片失败", e);
        }
    }

    public Path getImagePath(String type, String filename) {
        return storageLocation.resolve(type).resolve(filename);
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return ".jpg";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(lastDotIndex);
        }
        return ".jpg";
    }
}
