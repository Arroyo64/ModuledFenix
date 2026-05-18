package org.ies.fenix.server.controller;

import org.ies.fenix.server.services.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping("/image")
    public ResponseEntity<byte[]> getImage(@RequestParam("key") String key) {
        try {
            Path imagePath = fileStorageService.resolvePath(key);

            if (!Files.exists(imagePath)) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(imagePath);

            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(Files.readAllBytes(imagePath));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}