package org.ies.fenix.server.services;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${fenix.upload-dir:uploads}")
    private String uploadDir;

    public String saveGameFile(Integer gameId, MultipartFile file) {
        return saveFile(gameId, file, "files", "game");
    }

    public String saveGameLogo(Integer gameId, MultipartFile file) {
        return saveFile(gameId, file, "logo", "logo");
    }

    public String saveTeaser(Integer gameId, MultipartFile file, String teaserName) {
        return saveFile(gameId, file, "teasers", teaserName);
    }

    private String saveFile(Integer gameId, MultipartFile file, String folderName, String baseName) {
        if (gameId == null) {
            throw new IllegalArgumentException("Game id cannot be null");
        }

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        try {
            String originalFileName = file.getOriginalFilename();
            String extension = FilenameUtils.getExtension(originalFileName);

            if (extension == null || extension.isBlank()) {
                extension = "bin";
            }

            String cleanFileName = baseName + "_" + UUID.randomUUID().toString().replace("-", "") + "." + extension;

            Path relativePath = Path.of(
                    "games",
                    String.valueOf(gameId),
                    folderName,
                    cleanFileName
            );

            Path fullPath = Path.of(uploadDir)
                    .toAbsolutePath()
                    .normalize()
                    .resolve(relativePath)
                    .normalize();

            Files.createDirectories(fullPath.getParent());

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, fullPath, StandardCopyOption.REPLACE_EXISTING);
            }

            return relativePath.toString().replace("\\", "/");

        } catch (IOException e) {
            throw new RuntimeException("Error saving file", e);
        }
    }

    public Path resolvePath(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            throw new IllegalArgumentException("Object key cannot be empty");
        }

        return Path.of(uploadDir)
                .toAbsolutePath()
                .normalize()
                .resolve(objectKey)
                .normalize();
    }
}