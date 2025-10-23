package com.bookstore.online_bookstore_backend.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private List<String> roles;
    private String phone;
    private String address;
    private String avatarUrl;
    private boolean enabled;

    public UserResponse(Long id, String username, String email, List<String> roles, String phone, String address, String avatarUrl, boolean enabled) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.phone = phone;
        this.address = address;
        this.avatarUrl = avatarUrl;
        this.enabled = enabled;
    }
} 