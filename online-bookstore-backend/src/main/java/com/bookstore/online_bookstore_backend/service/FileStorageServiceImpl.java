package com.bookstore.online_bookstore_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${file.upload-dir:./uploads/default}") // Default to ./uploads/default if not set
    private String uploadDir;

    private Path rootLocation;

    @PostConstruct
    public void init() {
        // Normalize the root location path during initialization
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootLocation);
            System.out.println("Upload directory created/verified at: " + this.rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    @Override
    public String storeFile(MultipartFile file, Long userId) {
        if (file.isEmpty()) {
            throw new RuntimeException("Failed to store empty file.");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = "";
        int i = originalFilename.lastIndexOf('.');
        if (i > 0) {
            extension = originalFilename.substring(i);
        }
        // Generate a unique filename to prevent overwrites and anomymize
        String uniqueFilename = UUID.randomUUID().toString() + extension;

        try {
            // userSpecificDir is already normalized and absolute due to init()
            Path userSpecificDir = this.rootLocation;
            // Optional: create a subdirectory for each user if userId is provided and desired
            // if (userId != null) {
            //     userSpecificDir = this.rootLocation.resolve(String.valueOf(userId));
            //     Files.createDirectories(userSpecificDir);
            // }
            
            Path destinationFile = userSpecificDir.resolve(uniqueFilename).normalize(); // It's already absolute from rootLocation

            // --- DEBUGGING LOGS ---
            System.out.println("DEBUG: rootLocation (normalized in init): " + this.rootLocation);
            System.out.println("DEBUG: userSpecificDir (should be same as rootLocation): " + userSpecificDir);
            System.out.println("DEBUG: uniqueFilename being resolved: " + uniqueFilename);
            System.out.println("DEBUG: originalFilename from client: " + file.getOriginalFilename());
            System.out.println("DEBUG: destinationFile (after resolve & normalize): " + destinationFile);
            System.out.println("DEBUG: destinationFile.getParent(): " + destinationFile.getParent());
            // --- END DEBUGGING LOGS ---
            
            // Now both paths being compared should be normalized absolute paths
            if (!destinationFile.getParent().equals(userSpecificDir)) {
                System.err.println("SECURITY CHECK FAILED: Destination parent directory does not match configured upload directory.");
                System.err.println("   Expected Parent: " + userSpecificDir);
                System.err.println("   Actual Parent  : " + destinationFile.getParent());
                // This is a security check
                throw new RuntimeException("Cannot store file outside current directory.");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
            
            // Return a path that can be used to access the file, e.g., relative to a base URL for static content
            // For now, let's assume the files will be served from a path like /avatars/<filename>
            // So we return just the filename, or a relative path like "avatars/" + uniqueFilename
            // This depends on how you configure static resource serving.
            // If uploadDir is inside static path, or you configure a resource handler for uploadDir.
            return uniqueFilename; // Or "avatars/" + uniqueFilename if you serve them from /avatars/

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + originalFilename, e);
        }
    }
} 