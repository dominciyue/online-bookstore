package com.bookstore.online_bookstore_backend.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookSalesStatsDto {
    private Long bookId;
    private String bookTitle;
    private String bookAuthor; // Optional: useful for display
    private String bookCover;  // Optional: useful for display
    private Long totalQuantitySold;
    private BigDecimal totalRevenue;
} 