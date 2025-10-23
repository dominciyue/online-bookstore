package com.bookstore.online_bookstore_backend.service;

import com.bookstore.online_bookstore_backend.payload.response.MessageResponse;
import com.bookstore.online_bookstore_backend.payload.response.UserResponse;

import java.util.List;

public interface AdminService {
    List<UserResponse> getAllUsers();
    MessageResponse disableUser(Long userId);
    MessageResponse enableUser(Long userId);
} 