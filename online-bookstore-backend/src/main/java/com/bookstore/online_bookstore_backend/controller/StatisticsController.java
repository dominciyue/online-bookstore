package com.bookstore.online_bookstore_backend.controller;

import com.bookstore.online_bookstore_backend.entity.User;
import com.bookstore.online_bookstore_backend.payload.response.BookSalesStatsDto;
import com.bookstore.online_bookstore_backend.payload.response.PersonalOverallStatsDto;
import com.bookstore.online_bookstore_backend.payload.response.UserConsumptionStatsDto;
import com.bookstore.online_bookstore_backend.payload.response.PersonalBookStatsItemDto;
import com.bookstore.online_bookstore_backend.service.StatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map; // For error responses

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsController.class);

    private final StatisticsService statisticsService;

    @Autowired
    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/book-sales")
    @PreAuthorize("hasRole('ROLE_ADMIN')") // Ensure only ADMINs can access
    public ResponseEntity<?> getBookSalesStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        logger.info("API CALL: getBookSalesStatistics with startDate: {}, endDate: {}", startDate, endDate);
        try {
            List<BookSalesStatsDto> salesStats = statisticsService.getBookSalesStatistics(startDate, endDate);
            logger.info("API RESULT: getBookSalesStatistics returned {} items.", salesStats.size());
            return ResponseEntity.ok(salesStats);
        } catch (IllegalArgumentException e) {
            logger.warn("API ERROR: getBookSalesStatistics - IllegalArgumentException: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            logger.error("API ERROR: getBookSalesStatistics - Unexpected exception: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("message", "获取书籍销量统计时发生错误: " + e.getMessage()));
        }
    }

    @GetMapping("/user-consumption")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getUserConsumptionStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        logger.info("API CALL: getUserConsumptionStatistics with startDate: {}, endDate: {}", startDate, endDate);
        try {
            List<UserConsumptionStatsDto> consumptionStats = statisticsService.getUserConsumptionStatistics(startDate, endDate);
            logger.info("API RESULT: getUserConsumptionStatistics returned {} items.", consumptionStats.size());
            return ResponseEntity.ok(consumptionStats);
        } catch (IllegalArgumentException e) {
            logger.warn("API ERROR: getUserConsumptionStatistics - IllegalArgumentException: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            logger.error("API ERROR: getUserConsumptionStatistics - Unexpected exception: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("message", "获取用户消费统计时发生错误: " + e.getMessage()));
        }
    }

    @GetMapping("/my-book-stats")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_MODERATOR')") // Any authenticated user
    public ResponseEntity<List<PersonalBookStatsItemDto>> getMyBookStatistics(
            @AuthenticationPrincipal User currentUser,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            HttpServletRequest request) {
        
        // Log all incoming request parameters
        logger.info("API_SCRATCH_REWRITE_PARAMS_DEBUG: Request to /my-book-stats. Query String: {}", request.getQueryString());
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String paramValue = request.getParameter(paramName);
            logger.info("API_SCRATCH_REWRITE_PARAMS_DEBUG: Param: {} = Value: {}", paramName, paramValue);
        }

        String currentUsername = (currentUser != null) ? currentUser.getUsername() : "ANONYMOUS_USER_OR_TOKEN_ISSUE";
        Long currentUserId = (currentUser != null) ? currentUser.getId() : null;

        logger.info("API_SCRATCH_REWRITE: getMyBookStatistics for user: {} (ID: {}), startDate: {}, endDate: {}", 
                    currentUsername, currentUserId, startDate, endDate);

        if (currentUser == null) {
            logger.warn("API_SCRATCH_REWRITE_ERROR: getMyBookStatistics - currentUser is null. Returning 401.");
            // Consider if a List<PersonalBookStatsItemDto> should be returned with error or just the map
            // For now, returning Map for error consistency with other endpoints, though client might expect list.
            return ResponseEntity.status(401).body(null); // Or  ResponseEntity.status(401).body(Collections.emptyList()) if client expects list
        }
        try {
            logger.debug("API_SCRATCH_REWRITE: Calling statisticsService.getPersonalBookStatistics for userId: {}, startDate: {}, endDate: {}", 
                         currentUser.getId(), startDate, endDate);
            List<PersonalBookStatsItemDto> statsList = statisticsService.getPersonalBookStatistics(currentUser.getId(), startDate, endDate);
            logger.info("API_SCRATCH_REWRITE_RESULT: getMyBookStatistics for user: {} (ID: {}) returned list with {} items.", 
                        currentUsername, currentUser.getId(), statsList != null ? statsList.size() : "null list");
            if (statsList != null && !statsList.isEmpty()) {
                logger.debug("API_SCRATCH_REWRITE_RESULT_DETAIL: First item for user {}: {}", currentUsername, statsList.get(0));
            }
            return ResponseEntity.ok(statsList);
        } catch (IllegalArgumentException e) {
            logger.warn("API_SCRATCH_REWRITE_ERROR: getMyBookStatistics for user: {} (ID: {}) - IllegalArgumentException: {}", 
                        currentUsername, currentUserId, e.getMessage());
            // Again, consider returning empty list or error map.
            return ResponseEntity.badRequest().body(null); // Or ResponseEntity.badRequest().body(Collections.emptyList())
        } catch (Exception e) {
            logger.error("API_SCRATCH_REWRITE_ERROR: getMyBookStatistics for user: {} (ID: {}) - Unexpected exception: {}", 
                         currentUsername, currentUserId, e.getMessage(), e);
            return ResponseEntity.status(500).body(null); // Or ResponseEntity.status(500).body(Collections.emptyList())
        }
    }
} 