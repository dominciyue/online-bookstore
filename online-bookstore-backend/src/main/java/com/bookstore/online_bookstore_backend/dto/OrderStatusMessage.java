package com.bookstore.online_bookstore_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单状态消息DTO
 * 用于WebSocket推送订单状态更新
 */
public class OrderStatusMessage {
    private Long orderId;
    private Long userId;
    private String status;
    private BigDecimal totalPrice;
    private LocalDateTime updateTime;
    private String message;
    private String requestId;

    // 构造函数
    public OrderStatusMessage() {}

    public OrderStatusMessage(Long orderId, Long userId, String status, BigDecimal totalPrice, 
                             LocalDateTime updateTime, String message, String requestId) {
        this.orderId = orderId;
        this.userId = userId;
        this.status = status;
        this.totalPrice = totalPrice;
        this.updateTime = updateTime;
        this.message = message;
        this.requestId = requestId;
    }

    // Getters and Setters
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @Override
    public String toString() {
        return "OrderStatusMessage{" +
                "orderId=" + orderId +
                ", userId=" + userId +
                ", status='" + status + '\'' +
                ", totalPrice=" + totalPrice +
                ", updateTime=" + updateTime +
                ", message='" + message + '\'' +
                ", requestId='" + requestId + '\'' +
                '}';
    }
}
