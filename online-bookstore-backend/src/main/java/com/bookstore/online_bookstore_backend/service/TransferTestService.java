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
 * Transfer样例测试服务类
 * 模拟课件第25页表格中的transfer、withdraw、deposit场景
 * 用于测试不同@Transactional传播属性的行为
 */
@Service
public class TransferTestService {

    private final OrderDao orderDao;
    private final OrderItemDao orderItemDao;
    private final BookDao bookDao;

    @Autowired
    public TransferTestService(OrderDao orderDao, OrderItemDao orderItemDao, BookDao bookDao) {
        this.orderDao = orderDao;
        this.orderItemDao = orderItemDao;
        this.bookDao = bookDao;
    }

    /**
     * Transfer方法 - 模拟转账操作
     * 包含withdraw和deposit两个子操作
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public String transfer(Long userId, String shippingAddress, List<Long> bookIds, List<Integer> quantities, boolean simulateError) {
        System.out.println("=== TRANSFER方法开始 ===");
        System.out.println("当前事务状态: " + org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive());
        
        try {
            // 1. Withdraw操作 - 创建Order记录
            Order order = withdraw(userId, shippingAddress);
            System.out.println("Withdraw完成，Order ID: " + order.getId());
            
            // 2. Deposit操作 - 创建OrderItem记录
            List<OrderItem> orderItems = deposit(order, bookIds, quantities);
            System.out.println("Deposit完成，OrderItem数量: " + orderItems.size());
            
            // 模拟错误（根据参数决定）
            if (simulateError) {
                System.out.println("模拟错误：10/0");
                int result = 10 / 0; // 故意抛出除零异常
            }
            
            System.out.println("=== TRANSFER方法成功完成 ===");
            return "Transfer成功: Order ID=" + order.getId() + ", OrderItems=" + orderItems.size();
            
        } catch (Exception e) {
            System.out.println("=== TRANSFER方法异常: " + e.getMessage() + " ===");
            throw e;
        }
    }

    /**
     * Withdraw方法 - 模拟取款操作（创建Order）
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Order withdraw(Long userId, String shippingAddress) {
        System.out.println("--- Withdraw方法开始 ---");
        System.out.println("当前事务状态: " + org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive());
        
        Order order = createOrder(userId, shippingAddress);
        System.out.println("--- Withdraw方法完成 ---");
        return order;
    }

    /**
     * Deposit方法 - 模拟存款操作（创建OrderItem）
     * 使用REQUIRES_NEW传播属性
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<OrderItem> deposit(Order order, List<Long> bookIds, List<Integer> quantities) {
        System.out.println("--- Deposit方法开始 (REQUIRES_NEW) ---");
        System.out.println("当前事务状态: " + org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive());
        
        List<OrderItem> orderItems = createOrderItems(order, bookIds, quantities);
        System.out.println("--- Deposit方法完成 ---");
        return orderItems;
    }

    /**
     * Deposit方法 - 使用REQUIRED传播属性
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public List<OrderItem> depositRequired(Order order, List<Long> bookIds, List<Integer> quantities) {
        System.out.println("--- Deposit方法开始 (REQUIRED) ---");
        System.out.println("当前事务状态: " + org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive());
        
        List<OrderItem> orderItems = createOrderItems(order, bookIds, quantities);
        System.out.println("--- Deposit方法完成 ---");
        return orderItems;
    }

    /**
     * 测试场景1: 所有操作正常
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public String testScenario1(Long userId, String shippingAddress, List<Long> bookIds, List<Integer> quantities) {
        System.out.println("\n=== 测试场景1: 所有操作正常 ===");
        return transfer(userId, shippingAddress, bookIds, quantities, false);
    }

    /**
     * 测试场景2: Transfer中withdraw之前出错
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public String testScenario2(Long userId, String shippingAddress, List<Long> bookIds, List<Integer> quantities) {
        System.out.println("\n=== 测试场景2: Transfer中withdraw之前出错 ===");
        System.out.println("模拟错误：10/0");
        int result = 10 / 0; // 在withdraw之前就出错
        return "不应该到达这里";
    }

    /**
     * 测试场景3: Withdraw操作出错
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public String testScenario3(Long userId, String shippingAddress, List<Long> bookIds, List<Integer> quantities) {
        System.out.println("\n=== 测试场景3: Withdraw操作出错 ===");
        System.out.println("模拟错误：10/0");
        int result = 10 / 0; // 在withdraw中出错
        return "不应该到达这里";
    }

    /**
     * 测试场景4: Deposit操作出错（REQUIRED）
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public String testScenario4(Long userId, String shippingAddress, List<Long> bookIds, List<Integer> quantities) {
        System.out.println("\n=== 测试场景4: Deposit操作出错（REQUIRED） ===");
        try {
            Order order = withdraw(userId, shippingAddress);
            List<OrderItem> orderItems = depositRequired(order, bookIds, quantities);
            System.out.println("模拟错误：10/0");
            int result = 10 / 0; // 在deposit中出错
            return "不应该到达这里";
        } catch (Exception e) {
            System.out.println("整个事务回滚: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 测试场景5: Deposit操作出错（REQUIRES_NEW）
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public String testScenario5(Long userId, String shippingAddress, List<Long> bookIds, List<Integer> quantities) {
        System.out.println("\n=== 测试场景5: Deposit操作出错（REQUIRES_NEW） ===");
        try {
            Order order = withdraw(userId, shippingAddress);
            List<OrderItem> orderItems = deposit(order, bookIds, quantities);
            System.out.println("模拟错误：10/0");
            int result = 10 / 0; // 在deposit之后出错
            return "不应该到达这里";
        } catch (Exception e) {
            System.out.println("Withdraw和Transfer回滚，Deposit成功: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 测试场景6: Transfer中deposit之前出错
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public String testScenario6(Long userId, String shippingAddress, List<Long> bookIds, List<Integer> quantities) {
        System.out.println("\n=== 测试场景6: Transfer中deposit之前出错 ===");
        try {
            Order order = withdraw(userId, shippingAddress);
            System.out.println("模拟错误：10/0");
            int result = 10 / 0; // 在deposit之前出错
            List<OrderItem> orderItems = deposit(order, bookIds, quantities);
            return "不应该到达这里";
        } catch (Exception e) {
            System.out.println("整个事务回滚: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 测试场景7: Transfer中deposit之后出错
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public String testScenario7(Long userId, String shippingAddress, List<Long> bookIds, List<Integer> quantities) {
        System.out.println("\n=== 测试场景7: Transfer中deposit之后出错 ===");
        try {
            Order order = withdraw(userId, shippingAddress);
            List<OrderItem> orderItems = deposit(order, bookIds, quantities);
            System.out.println("模拟错误：10/0");
            int result = 10 / 0; // 在deposit之后出错
            return "不应该到达这里";
        } catch (Exception e) {
            System.out.println("Deposit成功，Withdraw和Transfer回滚: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 测试场景8: Withdraw出错，Deposit使用REQUIRES_NEW
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public String testScenario8(Long userId, String shippingAddress, List<Long> bookIds, List<Integer> quantities) {
        System.out.println("\n=== 测试场景8: Withdraw出错，Deposit使用REQUIRES_NEW ===");
        try {
            System.out.println("模拟错误：10/0");
            int result = 10 / 0; // 在withdraw中出错
            Order order = withdraw(userId, shippingAddress);
            List<OrderItem> orderItems = deposit(order, bookIds, quantities);
            return "不应该到达这里";
        } catch (Exception e) {
            System.out.println("整个事务回滚: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 测试场景9: Deposit出错（REQUIRES_NEW）
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public String testScenario9(Long userId, String shippingAddress, List<Long> bookIds, List<Integer> quantities) {
        System.out.println("\n=== 测试场景9: Deposit出错（REQUIRES_NEW） ===");
        try {
            Order order = withdraw(userId, shippingAddress);
            List<OrderItem> orderItems = depositWithError(order, bookIds, quantities);
            return "不应该到达这里";
        } catch (Exception e) {
            System.out.println("Withdraw和Transfer成功，Deposit回滚: " + e.getMessage());
            return "Withdraw和Transfer成功，Deposit回滚";
        }
    }

    /**
     * Deposit方法 - 带错误的版本
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<OrderItem> depositWithError(Order order, List<Long> bookIds, List<Integer> quantities) {
        System.out.println("--- Deposit方法开始 (REQUIRES_NEW) - 带错误 ---");
        System.out.println("当前事务状态: " + org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive());
        
        List<OrderItem> orderItems = createOrderItems(order, bookIds, quantities);
        System.out.println("模拟错误：10/0");
        int result = 10 / 0; // 在deposit中出错
        System.out.println("--- Deposit方法完成 ---");
        return orderItems;
    }

    /**
     * 创建Order记录
     */
    private Order createOrder(Long userId, String shippingAddress) {
        Order order = new Order();
        order.setUserId(userId);
        order.setShippingAddress(shippingAddress);
        order.setStatus("PENDING");
        order.setTotalPrice(BigDecimal.ZERO);
        
        return orderDao.save(order);
    }

    /**
     * 创建OrderItem记录
     */
    private List<OrderItem> createOrderItems(Order order, List<Long> bookIds, List<Integer> quantities) {
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (int i = 0; i < bookIds.size(); i++) {
            // 安全地转换类型 - 处理String和Long类型
            Object bookIdObj = bookIds.get(i);
            Long bookId;
            if (bookIdObj instanceof Long) {
                bookId = (Long) bookIdObj;
            } else if (bookIdObj instanceof String) {
                bookId = Long.valueOf((String) bookIdObj);
            } else {
                bookId = Long.valueOf(bookIdObj.toString());
            }
            
            Integer quantity = quantities.get(i);

            Book book = bookDao.findById(bookId)
                    .orElseThrow(() -> new RuntimeException("未找到书籍ID: " + bookId));

            if (book.getStock() < quantity) {
                throw new RuntimeException("书籍库存不足: " + book.getTitle());
            }

            OrderItem orderItem = new OrderItem(order, book, quantity, book.getPrice());
            orderItems.add(orderItem);
            totalPrice = totalPrice.add(book.getPrice().multiply(BigDecimal.valueOf(quantity)));

            // 更新库存
            book.setStock(book.getStock() - quantity);
            bookDao.save(book);
        }

        // 保存OrderItem记录
        orderItemDao.saveAll(orderItems);

        // 更新Order的总价
        order.setTotalPrice(totalPrice);
        orderDao.save(order);

        return orderItems;
    }
}
