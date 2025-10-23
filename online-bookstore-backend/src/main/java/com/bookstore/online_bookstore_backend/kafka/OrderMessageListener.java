package com.bookstore.online_bookstore_backend.kafka;

import com.bookstore.online_bookstore_backend.entity.Order;
import com.bookstore.online_bookstore_backend.kafka.OrderRequestMessage;
import com.bookstore.online_bookstore_backend.kafka.OrderResponseMessage;
import com.bookstore.online_bookstore_backend.service.OrderService;
import com.bookstore.online_bookstore_backend.service.WebSocketNotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class OrderMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(OrderMessageListener.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private KafkaTemplate<String, OrderResponseMessage> orderResponseKafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebSocketNotificationService webSocketNotificationService;

    @KafkaListener(topics = "order-requests", groupId = "order-processor-group")
    public void handleOrderRequest(@Payload String message,
                                   Acknowledgment acknowledgment) {
        logger.info("=== 收到订单请求消息 ===");
        logger.debug("消息内容: {}", message);

        try {
            // 解析消息
            OrderRequestMessage requestMessage = objectMapper.readValue(message, OrderRequestMessage.class);
            logger.info("订单请求已解析 - RequestID: {}, UserID: {}, Type: {}", 
                    requestMessage.getRequestId(), requestMessage.getUserId(), requestMessage.getRequestType());

            // 处理订单
            OrderResponseMessage responseMessage = processOrder(requestMessage);

            // 发送响应消息到另一个Topic
            orderResponseKafkaTemplate.send("order-responses", requestMessage.getRequestId(), responseMessage);
            logger.info("=== 订单响应已发送 ===");
            logger.debug("响应内容: {}", responseMessage.toString());

            // 手动确认消息
            acknowledgment.acknowledge();
            logger.debug("Kafka消息已确认 - RequestID: {}", requestMessage.getRequestId());

        } catch (Exception e) {
            logger.error("处理订单消息时发生错误: {}", e.getMessage(), e);

            // 发送错误响应
            try {
                OrderRequestMessage requestMessage = objectMapper.readValue(message, OrderRequestMessage.class);
                OrderResponseMessage errorResponse = OrderResponseMessage.error(
                    requestMessage.getRequestId(),
                    requestMessage.getUserId(),
                    requestMessage.getUserName(),
                    "处理订单请求时发生错误: " + e.getMessage(),
                    e.getMessage()
                );

                orderResponseKafkaTemplate.send("order-responses", requestMessage.getRequestId(), errorResponse);
                logger.info("错误响应已发送 - RequestID: {}", requestMessage.getRequestId());
            } catch (Exception parseException) {
                logger.error("解析原始消息以生成错误响应时失败: {}", parseException.getMessage(), parseException);
            }

            // 确认消息以避免重复处理
            acknowledgment.acknowledge();
        }
    }

    private OrderResponseMessage processOrder(OrderRequestMessage requestMessage) {
        try {
            Order order = null;

            if ("CART_ORDER".equals(requestMessage.getRequestType())) {
                // 处理购物车订单
                logger.info("处理购物车订单 - UserID: {}, RequestID: {}", 
                        requestMessage.getUserId(), requestMessage.getRequestId());
                order = orderService.createOrderFromCart(requestMessage.getUserId(), requestMessage.getShippingAddress());
            } else if ("SINGLE_BOOK_ORDER".equals(requestMessage.getRequestType())) {
                // 处理单品订单
                logger.info("处理单品订单 - UserID: {}, BookID: {}, Quantity: {}, RequestID: {}", 
                        requestMessage.getUserId(), requestMessage.getBookId(), 
                        requestMessage.getQuantity(), requestMessage.getRequestId());
                order = orderService.createOrderForSingleBook(
                    requestMessage.getUserId(),
                    requestMessage.getBookId(),
                    requestMessage.getQuantity(),
                    requestMessage.getShippingAddress()
                );
            } else {
                throw new IllegalArgumentException("未知的订单类型: " + requestMessage.getRequestType());
            }

            // 更新订单状态为COMPLETED
            order.setStatus("COMPLETED");
            orderService.updateOrderStatus(order);
            logger.info("订单处理成功 - OrderID: {}, UserID: {}, TotalPrice: {}", 
                    order.getId(), requestMessage.getUserId(), order.getTotalPrice());

            // 构建成功响应
            OrderResponseMessage response = OrderResponseMessage.success(
                requestMessage.getRequestId(),
                order.getId().toString(),
                requestMessage.getUserId(),
                requestMessage.getUserName(),
                order.getStatus(),
                order.getTotalPrice(),
                requestMessage.getShippingAddress(),
                "订单处理成功"
            );

            // 通过WebSocket推送订单完成通知
            webSocketNotificationService.notifyOrderCompleted(
                requestMessage.getUserId(),
                order.getId(),
                order.getTotalPrice(),
                requestMessage.getRequestId()
            );

            return response;

        } catch (Exception e) {
            logger.error("订单处理失败 - UserID: {}, RequestID: {}, Error: {}", 
                    requestMessage.getUserId(), requestMessage.getRequestId(), e.getMessage(), e);

            // 通过WebSocket推送订单失败通知
            webSocketNotificationService.notifyOrderFailed(
                requestMessage.getUserId(),
                null, // orderId为null表示订单创建失败
                e.getMessage(),
                requestMessage.getRequestId()
            );

            // 构建错误响应
            return OrderResponseMessage.error(
                requestMessage.getRequestId(),
                requestMessage.getUserId(),
                requestMessage.getUserName(),
                "订单处理失败: " + e.getMessage(),
                e.getMessage()
            );
        }
    }
}