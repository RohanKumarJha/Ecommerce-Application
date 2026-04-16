package ecommerce.product.service.impl;

import ecommerce.product.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);

    // ---------------------- UPLOAD IMAGE ----------------------
    @Override
    public String uploadImage(String path, MultipartFile file) throws IOException {
        // Get original file name and generate unique ID for file
        String originalFileName = file.getOriginalFilename();
        String randomId = UUID.randomUUID().toString();
        assert originalFileName != null;

        // Construct new file name with UUID to avoid collisions
        String fileName = randomId.concat(originalFileName.substring(originalFileName.lastIndexOf('.')));
        String filePath = path + File.separator + fileName;

        log.info("Preparing to upload image: originalFileName={}, fileName={}, filePath={}",
                originalFileName, fileName, filePath);

        // Create folder if it does not exist
        File folder = new File(path);
        if (!folder.exists()) {
            folder.mkdir();
            log.info("Folder did not exist, created folder at {}", path);
        } else {
            log.info("Folder already exists at {}", path);
        }

        // Copy file to the target location
        Files.copy(file.getInputStream(), Paths.get(filePath));
        log.info("Image uploaded successfully to {}", filePath);

        // Return the stored file name
        return fileName;
    }
}