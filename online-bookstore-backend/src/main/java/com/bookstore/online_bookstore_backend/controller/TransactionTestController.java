package com.bookstore.online_bookstore_backend.controller;

import com.bookstore.online_bookstore_backend.entity.Order;
import com.bookstore.online_bookstore_backend.service.TransactionTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 事务传播属性测试控制器
 * 用于测试不同@Transactional传播属性的行为
 */
@RestController
@RequestMapping("/api/transaction-test")
public class TransactionTestController {

    private final TransactionTestService transactionTestService;

    @Autowired
    public TransactionTestController(TransactionTestService transactionTestService) {
        this.transactionTestService = transactionTestService;
    }

    /**
     * 测试REQUIRED传播属性
     */
    @PostMapping("/test-required")
    public ResponseEntity<?> testRequired(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = Long.parseLong(payload.get("userId").toString());
            String shippingAddress = payload.getOrDefault("shippingAddress", "测试地址").toString();
            @SuppressWarnings("unchecked")
            List<Long> bookIds = (List<Long>) payload.get("bookIds");
            @SuppressWarnings("unchecked")
            List<Integer> quantities = (List<Integer>) payload.get("quantities");

            Order order = transactionTestService.createOrderWithRequired(userId, shippingAddress, bookIds, quantities);
            return ResponseEntity.ok(Map.of(
                "message", "REQUIRED传播属性测试成功",
                "orderId", order.getId(),
                "status", "SUCCESS"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "message", "REQUIRED传播属性测试失败: " + e.getMessage(),
                "status", "ERROR"
            ));
        }
    }

    /**
     * 测试REQUIRES_NEW传播属性
     */
    @PostMapping("/test-requires-new")
    public ResponseEntity<?> testRequiresNew(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = Long.parseLong(payload.get("userId").toString());
            String shippingAddress = payload.getOrDefault("shippingAddress", "测试地址").toString();
            @SuppressWarnings("unchecked")
            List<Long> bookIds = (List<Long>) payload.get("bookIds");
            @SuppressWarnings("unchecked")
            List<Integer> quantities = (List<Integer>) payload.get("quantities");

            Order order = transactionTestService.createOrderWithRequiresNew(userId, shippingAddress, bookIds, quantities);
            return ResponseEntity.ok(Map.of(
                "message", "REQUIRES_NEW传播属性测试成功",
                "orderId", order.getId(),
                "status", "SUCCESS"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "message", "REQUIRES_NEW传播属性测试失败: " + e.getMessage(),
                "status", "ERROR"
            ));
        }
    }

    /**
     * 测试SUPPORTS传播属性
     */
    @PostMapping("/test-supports")
    public ResponseEntity<?> testSupports(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = Long.parseLong(payload.get("userId").toString());
            String shippingAddress = payload.getOrDefault("shippingAddress", "测试地址").toString();
            @SuppressWarnings("unchecked")
            List<Long> bookIds = (List<Long>) payload.get("bookIds");
            @SuppressWarnings("unchecked")
            List<Integer> quantities = (List<Integer>) payload.get("quantities");

            Order order = transactionTestService.createOrderWithSupports(userId, shippingAddress, bookIds, quantities);
            return ResponseEntity.ok(Map.of(
                "message", "SUPPORTS传播属性测试成功",
                "orderId", order.getId(),
                "status", "SUCCESS"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "message", "SUPPORTS传播属性测试失败: " + e.getMessage(),
                "status", "ERROR"
            ));
        }
    }

    /**
     * 测试MANDATORY传播属性
     */
    @PostMapping("/test-mandatory")
    public ResponseEntity<?> testMandatory(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = Long.parseLong(payload.get("userId").toString());
            String shippingAddress = payload.getOrDefault("shippingAddress", "测试地址").toString();
            @SuppressWarnings("unchecked")
            List<Long> bookIds = (List<Long>) payload.get("bookIds");
            @SuppressWarnings("unchecked")
            List<Integer> quantities = (List<Integer>) payload.get("quantities");

            Order order = transactionTestService.createOrderWithMandatory(userId, shippingAddress, bookIds, quantities);
            return ResponseEntity.ok(Map.of(
                "message", "MANDATORY传播属性测试成功",
                "orderId", order.getId(),
                "status", "SUCCESS"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "message", "MANDATORY传播属性测试失败: " + e.getMessage(),
                "status", "ERROR"
            ));
        }
    }

    /**
     * 测试NOT_SUPPORTED传播属性
     */
    @PostMapping("/test-not-supported")
    public ResponseEntity<?> testNotSupported(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = Long.parseLong(payload.get("userId").toString());
            String shippingAddress = payload.getOrDefault("shippingAddress", "测试地址").toString();
            @SuppressWarnings("unchecked")
            List<Long> bookIds = (List<Long>) payload.get("bookIds");
            @SuppressWarnings("unchecked")
            List<Integer> quantities = (List<Integer>) payload.get("quantities");

            Order order = transactionTestService.createOrderWithNotSupported(userId, shippingAddress, bookIds, quantities);
            return ResponseEntity.ok(Map.of(
                "message", "NOT_SUPPORTED传播属性测试成功",
                "orderId", order.getId(),
                "status", "SUCCESS"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "message", "NOT_SUPPORTED传播属性测试失败: " + e.getMessage(),
                "status", "ERROR"
            ));
        }
    }

    /**
     * 测试NEVER传播属性
     */
    @PostMapping("/test-never")
    public ResponseEntity<?> testNever(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = Long.parseLong(payload.get("userId").toString());
            String shippingAddress = payload.getOrDefault("shippingAddress", "测试地址").toString();
            @SuppressWarnings("unchecked")
            List<Long> bookIds = (List<Long>) payload.get("bookIds");
            @SuppressWarnings("unchecked")
            List<Integer> quantities = (List<Integer>) payload.get("quantities");

            Order order = transactionTestService.createOrderWithNever(userId, shippingAddress, bookIds, quantities);
            return ResponseEntity.ok(Map.of(
                "message", "NEVER传播属性测试成功",
                "orderId", order.getId(),
                "status", "SUCCESS"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "message", "NEVER传播属性测试失败: " + e.getMessage(),
                "status", "ERROR"
            ));
        }
    }

    /**
     * 测试NESTED传播属性
     */
    @PostMapping("/test-nested")
    public ResponseEntity<?> testNested(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = Long.parseLong(payload.get("userId").toString());
            String shippingAddress = payload.getOrDefault("shippingAddress", "测试地址").toString();
            @SuppressWarnings("unchecked")
            List<Long> bookIds = (List<Long>) payload.get("bookIds");
            @SuppressWarnings("unchecked")
            List<Integer> quantities = (List<Integer>) payload.get("quantities");

            Order order = transactionTestService.createOrderWithNested(userId, shippingAddress, bookIds, quantities);
            return ResponseEntity.ok(Map.of(
                "message", "NESTED传播属性测试成功",
                "orderId", order.getId(),
                "status", "SUCCESS"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "message", "NESTED传播属性测试失败: " + e.getMessage(),
                "status", "ERROR"
            ));
        }
    }

    /**
     * 测试异常回滚
     */
    @PostMapping("/test-exception")
    public ResponseEntity<?> testException(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = Long.parseLong(payload.get("userId").toString());
            String shippingAddress = payload.getOrDefault("shippingAddress", "测试地址").toString();
            @SuppressWarnings("unchecked")
            List<Long> bookIds = (List<Long>) payload.get("bookIds");
            @SuppressWarnings("unchecked")
            List<Integer> quantities = (List<Integer>) payload.get("quantities");

            Order order = transactionTestService.createOrderWithException(userId, shippingAddress, bookIds, quantities);
            return ResponseEntity.ok(Map.of(
                "message", "异常测试成功（不应该到达这里）",
                "orderId", order.getId(),
                "status", "SUCCESS"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "message", "异常测试成功，事务已回滚: " + e.getMessage(),
                "status", "EXPECTED_ERROR"
            ));
        }
    }
}
