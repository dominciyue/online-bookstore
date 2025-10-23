package com.bookstore.online_bookstore_backend.controller;

import com.bookstore.online_bookstore_backend.entity.Order;
import com.bookstore.online_bookstore_backend.payload.response.MessageResponse;
import com.bookstore.online_bookstore_backend.payload.response.UserResponse;
import com.bookstore.online_bookstore_backend.service.AdminService;
import com.bookstore.online_bookstore_backend.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map; // For potential error responses

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final OrderService orderService; // Inject OrderService

    @Autowired
    public AdminController(AdminService adminService, OrderService orderService) {
        this.adminService = adminService;
        this.orderService = orderService; // Initialize OrderService
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = adminService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/users/{userId}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> disableUser(@PathVariable Long userId) {
        MessageResponse response = adminService.disableUser(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{userId}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> enableUser(@PathVariable Long userId) {
        MessageResponse response = adminService.enableUser(userId);
        return ResponseEntity.ok(response);
    }

    // New endpoint for admins to get all orders with pagination and filters
    @GetMapping("/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllOrdersForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDate,desc") String[] sort,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String bookName
    ) {
        try {
            Sort.Direction direction = sort.length > 1 && sort[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            String sortField = sort.length > 0 ? sort[0] : "orderDate";
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
            
            Page<Order> ordersPage = orderService.getAllOrders(pageable, startDate, endDate, userId, bookName);
            return ResponseEntity.ok(ordersPage);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "获取所有订单列表时出错: " + e.getMessage()));
        }
    }

    // Endpoint for admin to get details of a specific order
    @GetMapping("/orders/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getOrderDetailsForAdmin(@PathVariable Long orderId) {
        try {
            // Pass null as userId to indicate admin access, bypassing user ownership check in service layer
            Order order = orderService.getOrderDetails(orderId, null);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("未找到订单ID")) {
                return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
            }
            return ResponseEntity.status(400).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "获取订单详情时出错: " + e.getMessage()));
        }
    }
} 