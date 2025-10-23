package com.bookstore.online_bookstore_backend.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseMessage {
    private String requestId;           // 对应的请求ID
    private String responseType;        // 响应类型：SUCCESS 或 ERROR
    private String orderId;             // 订单ID（成功时）
    private Long userId;                // 用户ID
    private String userName;            // 用户名
    private String status;              // 订单状态
    private String totalAmount;         // 总金额（使用String以匹配BigDecimal的toString()）
    private String shippingAddress;     // 收货地址
    private String message;             // 响应消息
    private String errorDetails;        // 错误详情（如果有）
    private LocalDateTime timestamp;    // 响应时间戳

    // 添加toString方法用于JSON序列化
    @Override
    public String toString() {
        try {
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            // 配置ObjectMapper以处理null值
            objectMapper.setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL);
            return objectMapper.writeValueAsString(this);
        } catch (Exception e) {
            // 如果JSON序列化失败，返回基本信息而不是完整的toString()
            return String.format("OrderResponseMessage(requestId=%s, responseType=%s, orderId=%s, userId=%s, message=%s)",
                this.requestId, this.responseType, this.orderId, this.userId, this.message);
        }
    }

    // 成功响应
    public static OrderResponseMessage success(String requestId, String orderId, Long userId,
                                             String userName, String status, BigDecimal totalAmount,
                                             String shippingAddress, String message) {
        return new OrderResponseMessage(
            requestId,
            "SUCCESS",
            orderId,
            userId,
            userName,
            status,
            totalAmount.toString(),
            shippingAddress,
            message,
            null,
            LocalDateTime.now()
        );
    }

    // 错误响应
    public static OrderResponseMessage error(String requestId, Long userId, String userName,
                                           String message, String errorDetails) {
        return new OrderResponseMessage(
            requestId,
            "ERROR",
            null,
            userId,
            userName,
            null,
            null,
            null,
            message,
            errorDetails,
            LocalDateTime.now()
        );
    }
}

