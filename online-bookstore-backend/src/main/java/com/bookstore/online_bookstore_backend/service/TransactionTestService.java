package com.bookstore.online_bookstore_backend.service;

import com.bookstore.online_bookstore_backend.dao.BookDao;
import com.bookstore.online_bookstore_backend.dao.OrderDao;
import com.bookstore.online_bookstore_backend.dao.OrderItemDao;
import com.bookstore.online_bookstore_backend.entity.Book;
import com.bookstore.online_bookstore_backend.entity.Order;
import com.bookstore.online_bookstore_backend.entity.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 事务传播属性测试服务类
 * 用于测试不同@Transactional传播属性的行为
 */
@Service
public class TransactionTestService {

    private final OrderDao orderDao;
    private final OrderItemDao orderItemDao;
    private final BookDao bookDao;
    
    @Autowired
    private BookInventoryService inventoryService;

    @Autowired
    public TransactionTestService(OrderDao orderDao, OrderItemDao orderItemDao, BookDao bookDao) {
        this.orderDao = orderDao;
        this.orderItemDao = orderItemDao;
        this.bookDao = bookDao;
    }

    /**
     * 主事务方法 - 创建订单（包含Order和OrderItem的插入）
     * 使用REQUIRED传播属性（默认）
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Order createOrderWithRequired(Long userId, String shippingAddress, List<Long> bookIds, List<Integer> quantities) {
        System.out.println("=== 主事务方法开始 (REQUIRED) ===");
        System.out.println("当前事务状态: " + org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive());
        
        // 1. 创建Order记录
        Order order = createOrder(userId, shippingAddress);
        System.out.println("Order创建完成，ID: " + order.getId());
        
        // 2. 创建OrderItem记录
        List<OrderItem> orderItems = createOrderItems(order, bookIds, quantities);
        System.out.println("OrderItem创建完成，数量: " + orderItems.size());
        
        System.out.println("=== 主事务方法结束 (REQUIRED) ===");
        return order;
    }

    /**
     * 测试REQUIRES_NEW传播属性
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Order createOrderWithRequiresNew(Long userId, String shippingAddress, List<Long> bookIds, List<Integer> quantities) {
        System.out.println("=== 新事务方法开始 (REQUIRES_NEW) ===");
        System.out.println("当前事务状态: " + org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive());
        
        Order order = createOrder(userId, shippingAddress);
        List<OrderItem> orderItems = createOrderItems(order, bookIds, quantities);
        
        System.out.println("=== 新事务方法结束 (REQUIRES_NEW) ===");
        return order;
    }

    /**
     * 测试SUPPORTS传播属性
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    public Order createOrderWithSupports(Long userId, String shippingAddress, List<Long> bookIds, List<Integer> quantities) {
        System.out.println("=== 支持事务方法开始 (SUPPORTS) ===");
        System.out.println("当前事务状态: " + org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive());
        
        Order order = createOrder(userId, shippingAddress);
        List<OrderItem> orderItems = createOrderItems(order, bookIds, quantities);
        
        System.out.println("=== 支持事务方法结束 (SUPPORTS) ===");
        return order;
    }

    /**
     * 测试MANDATORY传播属性
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public Order createOrderWithMandatory(Long userId, String shippingAddress, List<Long> bookIds, List<Integer> quantities) {
        System.out.println("=== 强制事务方法开始 (MANDATORY) ===");
        System.out.println("当前事务状态: " + org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive());
        
        Order order = createOrder(userId, shippingAddress);
        List<OrderItem> orderItems = createOrderItems(order, bookIds, quantities);
        
        System.out.println("=== 强制事务方法结束 (MANDATORY) ===");
        return order;
    }

    /**
     * 测试NOT_SUPPORTED传播属性
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Order createOrderWithNotSupported(Long userId, String shippingAddress, List<Long> bookIds, List<Integer> quantities) {
        System.out.println("=== 不支持事务方法开始 (NOT_SUPPORTED) ===");
        System.out.println("当前事务状态: " + org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive());
        
        Order order = createOrder(userId, shippingAddress);
        List<OrderItem> orderItems = createOrderItems(order, bookIds, quantities);
        
        System.out.println("=== 不支持事务方法结束 (NOT_SUPPORTED) ===");
        return order;
    }

    /**
     * 测试NEVER传播属性
     */
    @Transactional(propagation = Propagation.NEVER)
    public Order createOrderWithNever(Long userId, String shippingAddress, List<Long> bookIds, List<Integer> quantities) {
        System.out.println("=== 禁止事务方法开始 (NEVER) ===");
        System.out.println("当前事务状态: " + org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive());
        
        Order order = createOrder(userId, shippingAddress);
        List<OrderItem> orderItems = createOrderItems(order, bookIds, quantities);
        
        System.out.println("=== 禁止事务方法结束 (NEVER) ===");
        return order;
    }

    /**
     * 测试NESTED传播属性
     */
    @Transactional(propagation = Propagation.NESTED)
    public Order createOrderWithNested(Long userId, String shippingAddress, List<Long> bookIds, List<Integer> quantities) {
        System.out.println("=== 嵌套事务方法开始 (NESTED) ===");
        System.out.println("当前事务状态: " + org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive());
        
        Order order = createOrder(userId, shippingAddress);
        List<OrderItem> orderItems = createOrderItems(order, bookIds, quantities);
        
        System.out.println("=== 嵌套事务方法结束 (NESTED) ===");
        return order;
    }

    /**
     * 创建Order记录
     */
    private Order createOrder(Long userId, String shippingAddress) {
        Order order = new Order();
        order.setUserId(userId);
        order.setShippingAddress(shippingAddress);
        order.setStatus("PENDING");
        order.setTotalPrice(BigDecimal.ZERO); // 将在创建OrderItem后更新
        
        return orderDao.save(order);
    }

    /**
     * 创建OrderItem记录
     */
    private List<OrderItem> createOrderItems(Order order, List<Long> bookIds, List<Integer> quantities) {
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (int i = 0; i < bookIds.size(); i++) {
            Long bookId = bookIds.get(i);
            Integer quantity = quantities.get(i);

            Book book = bookDao.findById(bookId)
                    .orElseThrow(() -> new RuntimeException("未找到书籍ID: " + bookId));

            // Check stock using inventory service
            Integer currentStock = inventoryService.getStock(bookId);
            if (currentStock < quantity) {
                throw new RuntimeException("书籍库存不足: " + book.getTitle());
            }

            OrderItem orderItem = new OrderItem(order, book, quantity, book.getPrice());
            orderItems.add(orderItem);
            totalPrice = totalPrice.add(book.getPrice().multiply(BigDecimal.valueOf(quantity)));

            // Reduce stock using inventory service
            boolean stockReduced = inventoryService.reduceStock(bookId, quantity);
            if (!stockReduced) {
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

    /**
     * 模拟异常的方法，用于测试事务回滚
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Order createOrderWithException(Long userId, String shippingAddress, List<Long> bookIds, List<Integer> quantities) {
        System.out.println("=== 异常测试方法开始 ===");
        
        Order order = createOrder(userId, shippingAddress);
        List<OrderItem> orderItems = createOrderItems(order, bookIds, quantities);
        
        // 故意抛出异常来测试事务回滚
        throw new RuntimeException("模拟异常，测试事务回滚");
    }
}
