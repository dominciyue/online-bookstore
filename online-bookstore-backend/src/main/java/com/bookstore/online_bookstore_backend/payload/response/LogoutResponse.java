package com.bookstore.online_bookstore_backend.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogoutResponse {
    private String message;
    private long sessionDuration;
}

