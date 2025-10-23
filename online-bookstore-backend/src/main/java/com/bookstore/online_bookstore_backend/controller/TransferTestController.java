package com.bookstore.online_bookstore_backend.controller;

import com.bookstore.online_bookstore_backend.service.TransferTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Transfer样例测试控制器
 * 对应课件第25页表格的9个测试场景
 */
@RestController
@RequestMapping("/api/transfer-test")
public class TransferTestController {

    private final TransferTestService transferTestService;

    @Autowired
    public TransferTestController(TransferTestService transferTestService) {
        this.transferTestService = transferTestService;
    }

    /**
     * 测试场景1: 所有操作正常
     * transfer: 正常, withdraw: 正常, deposit: 正常
     * result: 正常
     */
    @PostMapping("/scenario1")
    public ResponseEntity<?> testScenario1(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = Long.parseLong(payload.get("userId").toString());
            String shippingAddress = payload.getOrDefault("shippingAddress", "测试地址").toString();
            @SuppressWarnings("unchecked")
            List<Long> bookIds = (List<Long>) payload.get("bookIds");
            @SuppressWarnings("unchecked")
            List<Integer> quantities = (List<Integer>) payload.get("quantities");

            String result = transferTestService.testScenario1(userId, shippingAddress, bookIds, quantities);
            return ResponseEntity.ok(Map.of(
                "scenario", "场景1: 所有操作正常",
                "result", result,
                "status", "SUCCESS"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "scenario", "场景1: 所有操作正常",
                "result", "整个事务回滚",
                "error", e.getMessage(),
                "status", "ROLLBACK"
            ));
        }
    }

    /**
     * 测试场景2: Transfer中withdraw之前出错
     * transfer: result = 10/0, withdraw: 正常, deposit: 正常
     * result: 整个事务回滚
     */
    @PostMapping("/scenario2")
    public ResponseEntity<?> testScenario2(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = Long.parseLong(payload.get("userId").toString());
            String shippingAddress = payload.getOrDefault("shippingAddress", "测试地址").toString();
            @SuppressWarnings("unchecked")
            List<Long> bookIds = (List<Long>) payload.get("bookIds");
            @SuppressWarnings("unchecked")
            List<Integer> quantities = (List<Integer>) payload.get("quantities");

            String result = transferTestService.testScenario2(userId, shippingAddress, bookIds, quantities);
            return ResponseEntity.ok(Map.of(
                "scenario", "场景2: Transfer中withdraw之前出错",
                "result", result,
                "status", "SUCCESS"
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "scenario", "场景2: Transfer中withdraw之前出错",
                "result", "整个事务回滚",
                "error", e.getMessage(),
                "status", "ROLLBACK"
            ));
        }
    }

    /**
     * 测试场景3: Withdraw操作出错
     * transfer: 正常, withdraw: result = 10/0, deposit: 正常
     * result: 整个事务回滚
     */
    @PostMapping("/scenario3")
    public ResponseEntity<?> testScenario3(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = Long.parseLong(payload.get("userId").toString());
            String shippingAddress = payload.getOrDefault("shippingAddress", "测试地址").toString();
            @SuppressWarnings("unchecked")
            List<Long> bookIds = (List<Long>) payload.get("bookIds");
            @SuppressWarnings("unchecked")
            List<Integer> quantities = (List<Integer>) payload.get("quantities");

            String result = transferTestService.testScenario3(userId, shippingAddress, bookIds, quantities);
            return ResponseEntity.ok(Map.of(
                "scenario", "场景3: Withdraw操作出错",
                "result", result,
                "status", "SUCCESS"
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "scenario", "场景3: Withdraw操作出错",
                "result", "整个事务回滚",
                "error", e.getMessage(),
                "status", "ROLLBACK"
            ));
        }
    }

    /**
     * 测试场景4: Deposit操作出错（REQUIRED）
     * transfer: 正常, withdraw: 正常, deposit: result = 10/0
     * result: 整个事务回滚
     */
    @PostMapping("/scenario4")
    public ResponseEntity<?> testScenario4(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = Long.parseLong(payload.get("userId").toString());
            String shippingAddress = payload.getOrDefault("shippingAddress", "测试地址").toString();
            @SuppressWarnings("unchecked")
            List<Long> bookIds = (List<Long>) payload.get("bookIds");
            @SuppressWarnings("unchecked")
            List<Integer> quantities = (List<Integer>) payload.get("quantities");

            String result = transferTestService.testScenario4(userId, shippingAddress, bookIds, quantities);
            return ResponseEntity.ok(Map.of(
                "scenario", "场景4: Deposit操作出错（REQUIRED）",
                "result", result,
                "status", "SUCCESS"
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "scenario", "场景4: Deposit操作出错（REQUIRED）",
                "result", "整个事务回滚",
                "error", e.getMessage(),
                "status", "ROLLBACK"
            ));
        }
    }

    /**
     * 测试场景5: Deposit操作出错（REQUIRES_NEW）
     * transfer: 正常, withdraw: 正常, deposit: 正常 REQUIRES_NEW
     * result: 正常
     */
    @PostMapping("/scenario5")
    public ResponseEntity<?> testScenario5(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = Long.parseLong(payload.get("userId").toString());
            String shippingAddress = payload.getOrDefault("shippingAddress", "测试地址").toString();
            @SuppressWarnings("unchecked")
            List<Long> bookIds = (List<Long>) payload.get("bookIds");
            @SuppressWarnings("unchecked")
            List<Integer> quantities = (List<Integer>) payload.get("quantities");

            String result = transferTestService.testScenario5(userId, shippingAddress, bookIds, quantities);
            return ResponseEntity.ok(Map.of(
                "scenario", "场景5: Deposit操作出错（REQUIRES_NEW）",
                "result", result,
                "status", "SUCCESS"
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "scenario", "场景5: Deposit操作出错（REQUIRES_NEW）",
                "result", "Deposit成功，Withdraw和Transfer回滚",
                "error", e.getMessage(),
                "status", "PARTIAL_SUCCESS"
            ));
        }
    }

    /**
     * 测试场景6: Transfer中deposit之前出错
     * transfer: result = 10/0, withdraw: 正常, deposit: 正常 REQUIRES_NEW
     * result: 整个事务回滚
     */
    @PostMapping("/scenario6")
    public ResponseEntity<?> testScenario6(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = Long.parseLong(payload.get("userId").toString());
            String shippingAddress = payload.getOrDefault("shippingAddress", "测试地址").toString();
            @SuppressWarnings("unchecked")
            List<Long> bookIds = (List<Long>) payload.get("bookIds");
            @SuppressWarnings("unchecked")
            List<Integer> quantities = (List<Integer>) payload.get("quantities");

            String result = transferTestService.testScenario6(userId, shippingAddress, bookIds, quantities);
            return ResponseEntity.ok(Map.of(
                "scenario", "场景6: Transfer中deposit之前出错",
                "result", result,
                "status", "SUCCESS"
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "scenario", "场景6: Transfer中deposit之前出错",
                "result", "整个事务回滚",
                "error", e.getMessage(),
                "status", "ROLLBACK"
            ));
        }
    }

    /**
     * 测试场景7: Transfer中deposit之后出错
     * transfer: result = 10/0, withdraw: 正常, deposit: 正常 REQUIRES_NEW
     * result: deposit成功, withdraw和transfer事务回滚
     */
    @PostMapping("/scenario7")
    public ResponseEntity<?> testScenario7(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = Long.parseLong(payload.get("userId").toString());
            String shippingAddress = payload.getOrDefault("shippingAddress", "测试地址").toString();
            @SuppressWarnings("unchecked")
            List<Long> bookIds = (List<Long>) payload.get("bookIds");
            @SuppressWarnings("unchecked")
            List<Integer> quantities = (List<Integer>) payload.get("quantities");

            String result = transferTestService.testScenario7(userId, shippingAddress, bookIds, quantities);
            return ResponseEntity.ok(Map.of(
                "scenario", "场景7: Transfer中deposit之后出错",
                "result", result,
                "status", "SUCCESS"
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "scenario", "场景7: Transfer中deposit之后出错",
                "result", "Deposit成功，Withdraw和Transfer回滚",
                "error", e.getMessage(),
                "status", "PARTIAL_SUCCESS"
            ));
        }
    }

    /**
     * 测试场景8: Withdraw出错，Deposit使用REQUIRES_NEW
     * transfer: 正常, withdraw: result = 10/0, deposit: 正常 REQUIRES_NEW
     * result: 整个事务回滚
     */
    @PostMapping("/scenario8")
    public ResponseEntity<?> testScenario8(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = Long.parseLong(payload.get("userId").toString());
            String shippingAddress = payload.getOrDefault("shippingAddress", "测试地址").toString();
            @SuppressWarnings("unchecked")
            List<Long> bookIds = (List<Long>) payload.get("bookIds");
            @SuppressWarnings("unchecked")
            List<Integer> quantities = (List<Integer>) payload.get("quantities");

            String result = transferTestService.testScenario8(userId, shippingAddress, bookIds, quantities);
            return ResponseEntity.ok(Map.of(
                "scenario", "场景8: Withdraw出错，Deposit使用REQUIRES_NEW",
                "result", result,
                "status", "SUCCESS"
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "scenario", "场景8: Withdraw出错，Deposit使用REQUIRES_NEW",
                "result", "整个事务回滚",
                "error", e.getMessage(),
                "status", "ROLLBACK"
            ));
        }
    }

    /**
     * 测试场景9: Deposit出错（REQUIRES_NEW）
     * transfer: 正常, withdraw: 正常, deposit: result = 10/0 REQUIRES_NEW
     * result: withdraw和transfer成功, deposit事务回滚
     */
    @PostMapping("/scenario9")
    public ResponseEntity<?> testScenario9(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = Long.parseLong(payload.get("userId").toString());
            String shippingAddress = payload.getOrDefault("shippingAddress", "测试地址").toString();
            @SuppressWarnings("unchecked")
            List<Long> bookIds = (List<Long>) payload.get("bookIds");
            @SuppressWarnings("unchecked")
            List<Integer> quantities = (List<Integer>) payload.get("quantities");

            String result = transferTestService.testScenario9(userId, shippingAddress, bookIds, quantities);
            return ResponseEntity.ok(Map.of(
                "scenario", "场景9: Deposit出错（REQUIRES_NEW）",
                "result", result,
                "status", "SUCCESS"
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "scenario", "场景9: Deposit出错（REQUIRES_NEW）",
                "result", "Withdraw和Transfer成功，Deposit回滚",
                "error", e.getMessage(),
                "status", "PARTIAL_SUCCESS"
            ));
        }
    }

    /**
     * 运行所有测试场景
     */
    @PostMapping("/run-all")
    public ResponseEntity<?> runAllScenarios(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = Long.parseLong(payload.get("userId").toString());
            String shippingAddress = payload.getOrDefault("shippingAddress", "测试地址").toString();
            @SuppressWarnings("unchecked")
            List<Long> bookIds = (List<Long>) payload.get("bookIds");
            @SuppressWarnings("unchecked")
            List<Integer> quantities = (List<Integer>) payload.get("quantities");

            StringBuilder results = new StringBuilder();
            results.append("=== Transfer样例测试结果 ===\n\n");

            // 运行所有场景
            for (int i = 1; i <= 9; i++) {
                try {
                    String result = switch (i) {
                        case 1 -> transferTestService.testScenario1(userId, shippingAddress, bookIds, quantities);
                        case 2 -> transferTestService.testScenario2(userId, shippingAddress, bookIds, quantities);
                        case 3 -> transferTestService.testScenario3(userId, shippingAddress, bookIds, quantities);
                        case 4 -> transferTestService.testScenario4(userId, shippingAddress, bookIds, quantities);
                        case 5 -> transferTestService.testScenario5(userId, shippingAddress, bookIds, quantities);
                        case 6 -> transferTestService.testScenario6(userId, shippingAddress, bookIds, quantities);
                        case 7 -> transferTestService.testScenario7(userId, shippingAddress, bookIds, quantities);
                        case 8 -> transferTestService.testScenario8(userId, shippingAddress, bookIds, quantities);
                        case 9 -> transferTestService.testScenario9(userId, shippingAddress, bookIds, quantities);
                        default -> "未知场景";
                    };
                    results.append("场景").append(i).append(": ").append(result).append("\n");
                } catch (Exception e) {
                    results.append("场景").append(i).append(": 异常 - ").append(e.getMessage()).append("\n");
                }
            }

            return ResponseEntity.ok(Map.of(
                "message", "所有测试场景完成",
                "results", results.toString(),
                "status", "COMPLETED"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "message", "测试执行失败",
                "error", e.getMessage(),
                "status", "ERROR"
            ));
        }
    }
}
