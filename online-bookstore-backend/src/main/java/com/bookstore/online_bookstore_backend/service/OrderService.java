package com.bookstore.online_bookstore_backend.service;

import com.bookstore.online_bookstore_backend.dao.BookDao;
import com.bookstore.online_bookstore_backend.dao.OrderDao;
import com.bookstore.online_bookstore_backend.dao.OrderItemDao;
import com.bookstore.online_bookstore_backend.entity.*;
// import com.bookstore.online_bookstore_backend.repository.BookRepository; // No longer directly used
// import com.bookstore.online_bookstore_backend.repository.CartItemRepository; // Removed as unused
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
// import java.util.stream.Collectors; // Marked as unused by IDE

@Service
public class OrderService {

    private final OrderDao orderDao;
    private final OrderItemDao orderItemDao;
    private final CartService cartService; // 用于获取购物车项和清空购物车
    private final BookDao bookDao; // Use BookDao
    
    @Autowired
    private BookInventoryService inventoryService; // 库存服务

    @Autowired
    public OrderService(OrderDao orderDao, OrderItemDao orderItemDao, CartService cartService, 
                        BookDao bookDao) { // Inject BookDao
        this.orderDao = orderDao;
        this.orderItemDao = orderItemDao;
        this.cartService = cartService;
        this.bookDao = bookDao; // Use BookDao
    }

    @Transactional
    public Order createOrderFromCart(Long userId, String shippingAddress) {
        List<CartItem> cartItems = cartService.getCartItemsByUserId(userId);
        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("购物车为空，无法创建订单。");
        }

        // 1. 创建Order记录
        Order newOrder = createOrder(userId, shippingAddress);
        
        // 2. 创建OrderItem记录
        List<OrderItem> orderItems = createOrderItemsFromCart(newOrder, cartItems);
        
        // 3. 清空购物车（在订单创建成功后）
        cartService.clearCart(userId);
        
        return newOrder;
    }

    /**
     * 创建Order记录
     */
    private Order createOrder(Long userId, String shippingAddress) {
        Order newOrder = new Order();
        newOrder.setUserId(userId);
        newOrder.setShippingAddress(shippingAddress);
        newOrder.setStatus("PENDING");
        newOrder.setTotalPrice(BigDecimal.ZERO); // 将在创建OrderItem后更新
        
        return orderDao.save(newOrder);
    }

    /**
     * 从购物车创建OrderItem记录
     */
    private List<OrderItem> createOrderItemsFromCart(Order order, List<CartItem> cartItems) {
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            Book book = bookDao.findById(cartItem.getBookId())
                    .orElseThrow(() -> new RuntimeException("未找到书籍ID: " + cartItem.getBookId()));

            // 检查库存
            Integer currentStock = inventoryService.getStock(cartItem.getBookId());
            if (currentStock < cartItem.getQuantity()) {
                throw new RuntimeException("书籍库存不足: " + book.getTitle() + " (需求: " + cartItem.getQuantity() + ", 库存: " + currentStock + ")");
            }

            OrderItem orderItem = new OrderItem(order, book, cartItem.getQuantity(), book.getPrice());
            orderItems.add(orderItem);
            totalPrice = totalPrice.add(book.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            
            // 减少库存
            boolean success = inventoryService.reduceStock(cartItem.getBookId(), cartItem.getQuantity());
            if (!success) {
                throw new RuntimeException("减少库存失败: " + book.getTitle());
            }
        }

        // 保存OrderItem记录
        orderItemDao.saveAll(orderItems);

        // 更新Order的总价
        order.setTotalPrice(totalPrice);
        orderDao.save(order);

        return orderItems;
    }

    @Transactional
    public Order createOrderForSingleBook(Long userId, Long bookId, int quantity, String shippingAddress) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("购买数量必须为正数。");
        }

        Book book = bookDao.findById(bookId)
                .orElseThrow(() -> new RuntimeException("未找到书籍ID: " + bookId + "，无法创建订单。"));

        // 检查库存
        Integer currentStock = inventoryService.getStock(bookId);
        if (currentStock < quantity) {
            throw new RuntimeException("书籍库存不足: " + book.getTitle() + " (需求: " + quantity + ", 库存: " + currentStock + ")");
        }

        // 1. 创建Order记录
        Order newOrder = createOrder(userId, shippingAddress);
        
        // 2. 创建OrderItem记录
        List<Long> bookIds = List.of(bookId);
        List<Integer> quantities = List.of(quantity);
        List<OrderItem> orderItems = createOrderItems(newOrder, bookIds, quantities);
        
        return newOrder;
    }

    /**
     * 创建OrderItem记录（通用方法）
     */
    private List<OrderItem> createOrderItems(Order order, List<Long> bookIds, List<Integer> quantities) {
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (int i = 0; i < bookIds.size(); i++) {
            Long bookId = bookIds.get(i);
            Integer quantity = quantities.get(i);

            Book book = bookDao.findById(bookId)
                    .orElseThrow(() -> new RuntimeException("未找到书籍ID: " + bookId));

            // 检查库存
            Integer currentStock = inventoryService.getStock(bookId);
            if (currentStock < quantity) {
                throw new RuntimeException("书籍库存不足: " + book.getTitle());
            }

            OrderItem orderItem = new OrderItem(order, book, quantity, book.getPrice());
            orderItems.add(orderItem);
            totalPrice = totalPrice.add(book.getPrice().multiply(BigDecimal.valueOf(quantity)));

            // 减少库存
            boolean success = inventoryService.reduceStock(bookId, quantity);
            if (!success) {
                throw new RuntimeException("减少库存失败: " + book.getTitle());
            }
        }

        // 保存OrderItem记录
        orderItemDao.saveAll(orderItems);

        // 更新Order的总价
        order.setTotalPrice(totalPrice);
        orderDao.save(order);

        return orderItems;
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByUserId(Long userId) {
        List<Order> orders = orderDao.findByUserIdOrderByOrderDateDesc(userId);
        fillTransientOrderData(orders);
        return orders;
    }
    
    @Transactional(readOnly = true)
    public Page<Order> getOrdersByUserId(Long userId, Pageable pageable, LocalDateTime startDate, LocalDateTime endDate, String bookNameKeyword) {
        Page<Order> ordersPage;
        boolean hasDateFilter = startDate != null && endDate != null;
        boolean hasBookNameFilter = StringUtils.hasText(bookNameKeyword);

        if (hasDateFilter && hasBookNameFilter) {
            ordersPage = orderDao.findByUserIdAndOrderDateBetweenAndBookNameContainingIgnoreCase(userId, startDate, endDate, bookNameKeyword, pageable);
        } else if (hasBookNameFilter) {
            ordersPage = orderDao.findByUserIdAndBookNameContainingIgnoreCase(userId, bookNameKeyword, pageable);
        } else if (hasDateFilter) {
            ordersPage = orderDao.findByUserIdAndOrderDateBetweenOrderByOrderDateDesc(userId, startDate, endDate, pageable);
        } else {
            ordersPage = orderDao.findByUserIdOrderByOrderDateDesc(userId, pageable);
        }
        ordersPage.getContent().forEach(this::fillTransientOrderData);
        return ordersPage;
    }
    
    @Transactional(readOnly = true)
    public Page<Order> getAllOrders(Pageable pageable, LocalDateTime startDate, LocalDateTime endDate, Long filterByUserId, String bookNameKeyword) {
        Page<Order> ordersPage;
        boolean hasDateFilter = startDate != null && endDate != null;
        boolean hasBookNameFilter = StringUtils.hasText(bookNameKeyword);
        boolean hasUserFilter = filterByUserId != null;

        if (hasUserFilter) {
            if (hasDateFilter && hasBookNameFilter) {
                ordersPage = orderDao.findByUserIdAndOrderDateBetweenAndBookNameContainingIgnoreCaseForAdmin(filterByUserId, startDate, endDate, bookNameKeyword, pageable);
            } else if (hasBookNameFilter) {
                ordersPage = orderDao.findByUserIdAndBookNameContainingIgnoreCaseForAdmin(filterByUserId, bookNameKeyword, pageable);
            } else if (hasDateFilter) {
                ordersPage = orderDao.findByUserIdAndOrderDateBetweenOrderByOrderDateDesc(filterByUserId, startDate, endDate, pageable);
            } else {
                ordersPage = orderDao.findByUserIdOrderByOrderDateDesc(filterByUserId, pageable);
            }
        } else { // No specific user filter, search all orders
            if (hasDateFilter && hasBookNameFilter) {
                ordersPage = orderDao.findAllByOrderDateBetweenAndBookNameContainingIgnoreCase(startDate, endDate, bookNameKeyword, pageable);
            } else if (hasBookNameFilter) {
                ordersPage = orderDao.findAllByBookNameContainingIgnoreCase(bookNameKeyword, pageable);
            } else if (hasDateFilter) {
                ordersPage = orderDao.findAllByOrderDateBetweenOrderByOrderDateDesc(startDate, endDate, pageable);
            } else {
                ordersPage = orderDao.findAllByOrderByOrderDateDesc(pageable);
            }
        }

        ordersPage.getContent().forEach(this::fillTransientOrderData);
        return ordersPage;
    }
    
    @Transactional(readOnly = true)
    public Order getOrderDetails(Long orderId, Long userId) {
        Order order = orderDao.findById(orderId)
            .orElseThrow(() -> new RuntimeException("未找到订单ID: " + orderId));
        if (userId != null && !order.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问此订单"); 
        }
        fillTransientOrderData(order);
        return order;
    }

    private void fillTransientOrderData(Order order) {
        if (order != null && order.getOrderItems() != null) {
            order.getOrderItems().forEach(item -> {
                if (item.getBook() != null) {
                    item.setBookTitle(item.getBook().getTitle());
                    item.setBookCover(item.getBook().getCover());
                }
            });
        }
    }

    private void fillTransientOrderData(List<Order> orders) {
        orders.forEach(this::fillTransientOrderData);
    }

    /**
     * 更新订单状态
     */
    @Transactional
    public void updateOrderStatus(Order order) {
        orderDao.save(order);
    }
} 