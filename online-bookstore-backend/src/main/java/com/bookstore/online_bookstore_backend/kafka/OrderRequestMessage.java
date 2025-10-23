package com.bookstore.online_bookstore_backend.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestMessage {
    private String requestId;           // 请求ID，用于追踪
    private String requestType;         // 请求类型：CART_ORDER 或 SINGLE_BOOK_ORDER
    private Long userId;                // 用户ID
    private String userName;            // 用户名
    private String shippingAddress;     // 收货地址

    // 购物车订单特有字段
    private List<CartItemInfo> cartItems; // 购物车商品列表

    // 单品订单特有字段
    private Long bookId;                // 书籍ID
    private String bookTitle;           // 书籍标题
    private Integer quantity;           // 数量
    private BigDecimal bookPrice;       // 书籍价格

    private LocalDateTime timestamp;    // 请求时间戳

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
            return String.format("OrderRequestMessage(requestId=%s, requestType=%s, userId=%s, userName=%s)",
                this.requestId, this.requestType, this.userId, this.userName);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItemInfo {
        private Long bookId;
        private String bookTitle;
        private Integer quantity;
        private BigDecimal price;           // 书籍价格
    }
}

