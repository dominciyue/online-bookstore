package com.bookstore.online_bookstore_backend.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonalBookStatsItemDto {
    private Long bookId;
    private String bookTitle;
    private String bookAuthor;
    private String bookCover;
    private Long quantityBought;
    private BigDecimal totalSpentOnBook;
} 