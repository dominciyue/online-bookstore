package com.bookstore.online_bookstore_backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    /**
     * Stores a file to a predefined location or a dynamic subdirectory.
     * 
     * @param file The file to store.
     * @param userId Can be used to create a user-specific subdirectory (optional).
     * @return The path (e.g., relative URL or unique filename) of the stored file.
     * @throws RuntimeException if storing the file fails.
     */
    String storeFile(MultipartFile file, Long userId);

    // You might add other methods likeloadFileAsResource, deleteFile, etc.
} 