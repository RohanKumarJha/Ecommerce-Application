package com.ecommerce.product.service.impl;

import com.ecommerce.product.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private static final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);

    // ---------------------- UPLOAD IMAGE ----------------------
    @Override
    public String uploadImage(String path, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            log.error("File upload failed: file is null or empty");
            throw new IllegalArgumentException("File is empty");
        }
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || !originalFileName.contains(".")) {
            log.error("Invalid file name: {}", originalFileName);
            throw new IllegalArgumentException("Invalid file name");
        }
        String randomId = UUID.randomUUID().toString();
        String extension = originalFileName.substring(originalFileName.lastIndexOf('.'));
        String fileName = randomId + extension;
        Path filePath = Paths.get(path, fileName);
        log.info("Uploading image | original: {}, stored: {}, path: {}",
                originalFileName, fileName, filePath);
        File folder = new File(path);
        if (!folder.exists()) {
            boolean created = folder.mkdirs();
            if (created) {
                log.info("Directory created at {}", path);
            } else {
                log.warn("Failed to create directory at {}", path);
            }
        } else {
            log.debug("Directory already exists at {}", path);
        }
        try {
            Files.copy(file.getInputStream(), filePath);
            log.info("File uploaded successfully: {}", fileName);
        } catch (IOException e) {
            log.error("File upload failed for file: {}", fileName, e);
            throw e;
        }
        return fileName;
    }
}