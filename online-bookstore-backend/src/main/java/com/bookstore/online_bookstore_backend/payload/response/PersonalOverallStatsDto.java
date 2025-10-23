package com.bookstore.online_bookstore_backend.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonalOverallStatsDto {
    private List<PersonalBookStatsItemDto> bookItemsStats;
    private Long totalUniqueBookTypesCount; // New field for unique book types
    private Long totalBooksBoughtCount; // Represents total quantity of all books
    private BigDecimal overallTotalSpent;
} 