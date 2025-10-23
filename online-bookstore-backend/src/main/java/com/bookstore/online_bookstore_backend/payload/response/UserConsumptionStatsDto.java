package com.bookstore.online_bookstore_backend.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserConsumptionStatsDto {
    private Long userId;
    private String username;
    // You might want to add email or other user identifiers if needed for display
    private Long totalOrderCount;
    private BigDecimal totalAmountSpent;
} 