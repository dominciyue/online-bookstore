package com.bookstore.online_bookstore_backend.service;

import com.bookstore.online_bookstore_backend.payload.response.BookSalesStatsDto;
import com.bookstore.online_bookstore_backend.payload.response.PersonalOverallStatsDto;
import com.bookstore.online_bookstore_backend.payload.response.UserConsumptionStatsDto;
import com.bookstore.online_bookstore_backend.payload.response.PersonalBookStatsItemDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatisticsService {
    List<BookSalesStatsDto> getBookSalesStatistics(LocalDateTime startDate, LocalDateTime endDate);
    List<UserConsumptionStatsDto> getUserConsumptionStatistics(LocalDateTime startDate, LocalDateTime endDate);
    List<PersonalBookStatsItemDto> getPersonalBookStatistics(Long userId, LocalDateTime startDate, LocalDateTime endDate);
} 