package com.bookstore.online_bookstore_backend.service;

import com.bookstore.online_bookstore_backend.dao.UserDao;
import com.bookstore.online_bookstore_backend.entity.User;
import com.bookstore.online_bookstore_backend.payload.response.BookSalesStatsDto;
import com.bookstore.online_bookstore_backend.payload.response.PersonalBookStatsItemDto;
import com.bookstore.online_bookstore_backend.payload.response.PersonalOverallStatsDto;
import com.bookstore.online_bookstore_backend.payload.response.UserConsumptionStatsDto;
import com.bookstore.online_bookstore_backend.repository.OrderItemRepository;
import com.bookstore.online_bookstore_backend.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsServiceImpl.class);

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final UserDao userDao;

    @Autowired
    public StatisticsServiceImpl(OrderItemRepository orderItemRepository, 
                                 OrderRepository orderRepository, 
                                 UserDao userDao) {
        this.orderItemRepository = orderItemRepository;
        this.orderRepository = orderRepository;
        this.userDao = userDao;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookSalesStatsDto> getBookSalesStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date are required for book sales statistics.");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }
        return orderItemRepository.findBookSalesStatsBetweenDates(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserConsumptionStatsDto> getUserConsumptionStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date are required for user consumption statistics.");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }
        List<UserConsumptionStatsDto> stats = orderRepository.findUserConsumptionStatsBetweenDates(startDate, endDate);
        
        List<Long> userIds = stats.stream().map(UserConsumptionStatsDto::getUserId).collect(Collectors.toList());
        Map<Long, String> userIdToUsernameMap = userDao.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));

        stats.forEach(stat -> {
            stat.setUsername(userIdToUsernameMap.getOrDefault(stat.getUserId(), "Unknown User"));
        });
        
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonalBookStatsItemDto> getPersonalBookStatistics(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        System.out.println("<<<< SCRATCH_REWRITE_DEBUG_PRINT: StatisticsServiceImpl.getPersonalBookStatistics CALLED for userId: " + userId + ", startDate: " + startDate + ", endDate: " + endDate + " >>>>");
        logger.info("SCRATCH_REWRITE_SERVICE_CALL: Fetching personal book statistics for User ID: {}, Date Range: {} to {}", userId, startDate, endDate);

        if (userId == null) {
            logger.warn("SCRATCH_REWRITE_SERVICE_ERROR: User ID is null. Cannot fetch personal statistics.");
            throw new IllegalArgumentException("User ID cannot be null for personal book statistics.");
        }
        if (startDate == null || endDate == null) {
            logger.warn("SCRATCH_REWRITE_SERVICE_ERROR: Start date or end date is null for User ID: {}.", userId);
            throw new IllegalArgumentException("Start date and end date are required for personal book statistics.");
        }
        if (startDate.isAfter(endDate)) {
            logger.warn("SCRATCH_REWRITE_SERVICE_ERROR: Start date ({}) is after end date ({}) for User ID: {}.", startDate, endDate, userId);
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }

        List<PersonalBookStatsItemDto> purchasedBookItems;
        try {
            purchasedBookItems = orderItemRepository.findPersonalBookStatsByUserAndDateRange(userId, startDate, endDate);
            logger.info("SCRATCH_REWRITE_SERVICE_INFO: Database query for personal book stats for User ID: {} returned {} items.", userId, purchasedBookItems.size());
            
            if (purchasedBookItems == null) {
                logger.warn("SCRATCH_REWRITE_SERVICE_WARN: Database query returned null list for User ID: {}. Returning empty list.", userId);
                return Collections.emptyList();
            }
            
            if (!purchasedBookItems.isEmpty()) {
                logger.debug("SCRATCH_REWRITE_SERVICE_DETAIL: First item for User ID {}: {}", userId, purchasedBookItems.get(0));
            } else {
                logger.info("SCRATCH_REWRITE_SERVICE_INFO: No book purchase items found for User ID: {} in the given date range.", userId);
            }
        } catch (Exception e) {
            logger.error("SCRATCH_REWRITE_SERVICE_ERROR: Exception during database query for personal book stats for User ID: {}. Error: {}", userId, e.getMessage(), e);
            return Collections.emptyList(); 
        }
        
        logger.info("SCRATCH_REWRITE_SERVICE_RESULT: Successfully fetched {} personal book stat items for User ID: {}.", purchasedBookItems.size(), userId);
        System.out.println("<<<< SCRATCH_REWRITE_DEBUG_PRINT: StatisticsServiceImpl.getPersonalBookStatistics COMPLETED for userId: " + userId + " >>>>");
        return purchasedBookItems;
    }
} 