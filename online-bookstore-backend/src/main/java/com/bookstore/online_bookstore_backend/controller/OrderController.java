package com.bookstore.online_bookstore_backend.controller;

import com.bookstore.online_bookstore_backend.entity.Order;
import com.bookstore.online_bookstore_backend.entity.User;
import com.bookstore.online_bookstore_backend.entity.CartItem;
import com.bookstore.online_bookstore_backend.entity.Book;
import com.bookstore.online_bookstore_backend.service.OrderService;
import com.bookstore.online_bookstore_backend.service.CartService;
import com.bookstore.online_bookstore_backend.service.WebSocketNotificationService;
import com.bookstore.online_bookstore_backend.dao.BookDao;
import com.bookstore.online_bookstore_backend.kafka.OrderRequestMessage;
import com.bookstore.online_bookstore_backend.kafka.OrderRequestMessage.CartItemInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;
    private final BookDao bookDao;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaTemplate<String, OrderRequestMessage> orderRequestKafkaTemplate;
    private final WebSocketNotificationService webSocketNotificationService;

    @Autowired
    public OrderController(OrderService orderService, CartService cartService,
                          BookDao bookDao, KafkaTemplate<String, String> kafkaTemplate,
                          KafkaTemplate<String, OrderRequestMessage> orderRequestKafkaTemplate,
                          WebSocketNotificationService webSocketNotificationService) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.bookDao = bookDao;
        this.kafkaTemplate = kafkaTemplate;
        this.orderRequestKafkaTemplate = orderRequestKafkaTemplate;
        this.webSocketNotificationService = webSocketNotificationService;
    }

    // 创建新订单 (从购物车)
    // 请求体可以包含例如: { "shippingAddress": "Some Address" }
    @PostMapping("/create")
    public ResponseEntity<?> createOrderFromCart(@AuthenticationPrincipal User currentUser, @RequestBody(required = false) Map<String, String> payload) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "用户未登录"));
        }
        try {
            // 如果不希望有地址环节，可以移除 payload 和 shippingAddress
            String shippingAddress = (payload != null && payload.containsKey("shippingAddress")) ? payload.get("shippingAddress") : "用户未提供地址"; // 修改默认地址
            Order createdOrder = orderService.createOrderFromCart(currentUser.getId(), shippingAddress);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) { // Covers cart empty, stock issues etc.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "创建订单时发生未知错误: " + e.getMessage()));
        }
    }

    // 创建新订单 (单本书)
    @PostMapping("/create-single")
    public ResponseEntity<?> createOrderForSingleBook(@AuthenticationPrincipal User currentUser, @RequestBody Map<String, Object> payload) { // Changed to Map<String, Object> for diverse types
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "用户未登录"));
        }
        try {
            Long bookId = Long.parseLong(payload.get("bookId").toString());
            // Quantity can be an integer, default to 1 if not provided or invalid
            int quantity = 1;
            if (payload.containsKey("quantity")) {
                try {
                    quantity = Integer.parseInt(payload.get("quantity").toString());
                } catch (NumberFormatException e) {
                    // Keep quantity as 1 if parsing fails, or return bad request
                    // For now, let's be lenient and default to 1
                }
            }
            if (quantity <= 0) {
                quantity = 1; // Ensure quantity is positive, or throw error
            }

            String shippingAddress = payload.getOrDefault("shippingAddress", "用户未提供地址").toString();

            Order createdOrder = orderService.createOrderForSingleBook(currentUser.getId(), bookId, quantity, shippingAddress);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
        } catch (NullPointerException | NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "无效的书籍ID或数量格式。 bookId是必需的。"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) { // Catches stock issues etc.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "创建单品订单时发生未知错误: " + e.getMessage()));
        }
    }

    // Get current user's orders with pagination and optional date filtering
    @GetMapping
    public ResponseEntity<?> getUserOrders(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDate,desc") String[] sort, // Default sort by orderDate descending
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String bookName
            // bookName filter will be added later
            ) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "用户未登录"));
        }
        try {
            Sort.Direction direction = sort.length > 1 && sort[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            String sortField = sort.length > 0 ? sort[0] : "orderDate"; // Ensure a default sort field
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
            
            Page<Order> ordersPage = orderService.getOrdersByUserId(currentUser.getId(), pageable, startDate, endDate, bookName);
            return ResponseEntity.ok(ordersPage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "获取订单列表时出错: " + e.getMessage()));
        }
    }
    
    // 获取特定订单的详情
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderDetails(@AuthenticationPrincipal User currentUser, @PathVariable Long orderId) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "用户未登录"));
        }
        try {
            Order order = orderService.getOrderDetails(orderId, currentUser.getId());
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("未找到订单ID") || e.getMessage().contains("无权访问此订单")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "获取订单详情时出错: " + e.getMessage()));
        }
    }

    // 异步创建订单 (从购物车) - 发送到Kafka
    @PostMapping("/create-async")
    public ResponseEntity<?> createOrderFromCartAsync(@AuthenticationPrincipal User currentUser, @RequestBody(required = false) Map<String, String> payload) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "用户未登录"));
        }
        try {
            String shippingAddress = (payload != null && payload.containsKey("shippingAddress"))
                ? payload.get("shippingAddress") : "用户未提供地址";

            // 获取购物车商品信息
            List<CartItem> cartItems = cartService.getCartItemsByUserId(currentUser.getId());
            if (cartItems == null || cartItems.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "购物车为空，无法创建订单"));
            }

            // 构建Kafka消息
            String requestId = UUID.randomUUID().toString();
            OrderRequestMessage requestMessage = buildOrderRequestMessage(requestId, "CART_ORDER", currentUser, shippingAddress, cartItems);

            // 发送到Kafka
            orderRequestKafkaTemplate.send("order-requests", requestId, requestMessage);
            System.out.println("=== ASYNC ORDER REQUEST SENT ===");
            System.out.println("Request ID: " + requestId);
            try {
                String jsonMessage = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(requestMessage);
                System.out.println("Message JSON: " + jsonMessage);
            } catch (Exception e) {
                System.out.println("Message JSON: " + requestMessage.toString());
            }

            // 通过WebSocket推送订单创建通知
            webSocketNotificationService.notifyOrderCreated(
                currentUser.getId(),
                null, // orderId将在处理完成后推送
                BigDecimal.ZERO, // 总价将在处理完成后推送
                requestId
            );

            return ResponseEntity.accepted().body(Map.of(
                "message", "订单请求已提交，正在异步处理",
                "requestId", requestId,
                "status", "PROCESSING"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "提交订单请求时发生错误: " + e.getMessage()));
        }
    }

    // 异步创建单品订单 - 发送到Kafka
    @PostMapping("/create-single-async")
    public ResponseEntity<?> createOrderForSingleBookAsync(@AuthenticationPrincipal User currentUser, @RequestBody Map<String, Object> payload) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "用户未登录"));
        }
        try {
            Long bookId = Long.parseLong(payload.get("bookId").toString());
            int quantity = 1;
            if (payload.containsKey("quantity")) {
                try {
                    quantity = Integer.parseInt(payload.get("quantity").toString());
                } catch (NumberFormatException e) {
                    quantity = 1;
                }
            }
            if (quantity <= 0) {
                quantity = 1;
            }

            String shippingAddress = payload.getOrDefault("shippingAddress", "用户未提供地址").toString();

            // 获取书籍信息
            Book book = bookDao.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("未找到书籍ID: " + bookId));

            // 检查库存（注意：这里不需要注入 inventoryService，因为订单服务会检查）
            // if (book.getStock() < quantity) {
            //     return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "书籍库存不足: " + book.getTitle()));
            // }

            // 构建Kafka消息
            String requestId = UUID.randomUUID().toString();
            OrderRequestMessage requestMessage = buildOrderRequestMessage(requestId, "SINGLE_BOOK_ORDER", currentUser, shippingAddress, book, quantity);

            // 发送到Kafka
            orderRequestKafkaTemplate.send("order-requests", requestId, requestMessage);
            System.out.println("=== ASYNC SINGLE ORDER REQUEST SENT ===");
            System.out.println("Request ID: " + requestId);
            try {
                String jsonMessage = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(requestMessage);
                System.out.println("Message JSON: " + jsonMessage);
            } catch (Exception e) {
                System.out.println("Message JSON: " + requestMessage.toString());
            }

            // 通过WebSocket推送订单创建通知
            webSocketNotificationService.notifyOrderCreated(
                currentUser.getId(),
                null, // orderId将在处理完成后推送
                BigDecimal.ZERO, // 总价将在处理完成后推送
                requestId
            );

            return ResponseEntity.accepted().body(Map.of(
                "message", "单品订单请求已提交，正在异步处理",
                "requestId", requestId,
                "status", "PROCESSING"
            ));

        } catch (NullPointerException | NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "无效的书籍ID或数量格式"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "提交单品订单请求时发生错误: " + e.getMessage()));
        }
    }

    // 构建订单请求消息 (购物车订单)
    private OrderRequestMessage buildOrderRequestMessage(String requestId, String requestType, User user, String shippingAddress, List<CartItem> cartItems) {
        OrderRequestMessage message = new OrderRequestMessage();
        message.setRequestId(requestId);
        message.setRequestType(requestType);
        message.setUserId(user.getId());
        message.setUserName(user.getUsername());
        message.setShippingAddress(shippingAddress);
        message.setTimestamp(LocalDateTime.now());

        List<CartItemInfo> cartItemInfos = new ArrayList<>();
        for (CartItem item : cartItems) {
            Book itemBook = bookDao.findById(item.getBookId()).orElse(null);
            if (itemBook != null) {
                CartItemInfo info = new CartItemInfo();
                info.setBookId(itemBook.getId());
                info.setBookTitle(itemBook.getTitle());
                info.setQuantity(item.getQuantity());
                info.setPrice(itemBook.getPrice());
                cartItemInfos.add(info);
            }
        }
        message.setCartItems(cartItemInfos);

        return message;
    }

    // 构建订单请求消息 (单品订单)
    private OrderRequestMessage buildOrderRequestMessage(String requestId, String requestType, User user, String shippingAddress, Book book, int quantity) {
        OrderRequestMessage message = new OrderRequestMessage();
        message.setRequestId(requestId);
        message.setRequestType(requestType);
        message.setUserId(user.getId());
        message.setUserName(user.getUsername());
        message.setShippingAddress(shippingAddress);
        message.setTimestamp(LocalDateTime.now());

        message.setBookId(book.getId());
        message.setBookTitle(book.getTitle());
        message.setQuantity(quantity);
        message.setBookPrice(book.getPrice());

        return message;
    }
} 