package com.bookstore.online_bookstore_backend.service;

import com.bookstore.online_bookstore_backend.dto.OrderStatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.Set;

/**
 * WebSocket消息推送服务
 * 负责向客户端推送订单状态更新
 * 使用线程安全的集合来管理客户端Session
 */
@Service
public class WebSocketNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketNotificationService.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    // 使用线程安全的ConcurrentHashMap存储用户ID到Session ID的映射
    // Key: 用户ID (String), Value: 该用户的所有Session ID集合
    private final ConcurrentHashMap<String, Set<String>> userSessions = new ConcurrentHashMap<>();
    
    // 使用线程安全的CopyOnWriteArraySet存储所有活跃的Session ID
    private final Set<String> activeSessions = new CopyOnWriteArraySet<>();

    /**
     * 注册用户Session
     * @param userId 用户ID
     * @param sessionId Session ID
     */
    public void registerUserSession(String userId, String sessionId) {
        try {
            userSessions.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(sessionId);
            activeSessions.add(sessionId);
            logger.info("用户Session已注册 - 用户ID: {}, Session: {}, 当前活跃Session数: {}", 
                    userId, sessionId, activeSessions.size());
        } catch (Exception e) {
            logger.error("注册用户Session失败 - 用户ID: {}, Session: {}", userId, sessionId, e);
        }
    }
    
    /**
     * 注销用户Session
     * @param userId 用户ID
     * @param sessionId Session ID
     */
    public void unregisterUserSession(String userId, String sessionId) {
        try {
            Set<String> sessions = userSessions.get(userId);
            if (sessions != null) {
                sessions.remove(sessionId);
                if (sessions.isEmpty()) {
                    userSessions.remove(userId);
                }
            }
            activeSessions.remove(sessionId);
            logger.info("用户Session已注销 - 用户ID: {}, Session: {}, 当前活跃Session数: {}", 
                    userId, sessionId, activeSessions.size());
        } catch (Exception e) {
            logger.error("注销用户Session失败 - 用户ID: {}, Session: {}", userId, sessionId, e);
        }
    }
    
    /**
     * 检查用户是否有活跃的Session
     * @param userId 用户ID
     * @return 是否有活跃Session
     */
    public boolean hasActiveSession(String userId) {
        Set<String> sessions = userSessions.get(userId);
        return sessions != null && !sessions.isEmpty();
    }
    
    /**
     * 获取用户的所有活跃Session
     * @param userId 用户ID
     * @return Session ID集合
     */
    public Set<String> getUserSessions(String userId) {
        return userSessions.getOrDefault(userId, new CopyOnWriteArraySet<>());
    }

    /**
     * 获取所有活跃Session数量（用于监控）
     * @return 活跃Session数量
     */
    public int getActiveSessionCount() {
        return activeSessions.size();
    }

    /**
     * 获取所有活跃用户数量（用于监控）
     * @return 活跃用户数量
     */
    public int getActiveUserCount() {
        return userSessions.size();
    }

    /**
     * 推送订单状态更新给特定用户
     * @param userId 用户ID
     * @param orderId 订单ID
     * @param status 订单状态
     * @param totalPrice 总价
     * @param message 消息内容
     * @param requestId 请求ID
     */
    public void notifyOrderStatusUpdate(Long userId, Long orderId, String status, 
                                       BigDecimal totalPrice, String message, String requestId) {
        try {
            OrderStatusMessage statusMessage = new OrderStatusMessage(
                    orderId, userId, status, totalPrice, LocalDateTime.now(), message, requestId
            );
            
            String userIdStr = userId.toString();
            
            // 检查用户是否有活跃的Session
            if (hasActiveSession(userIdStr)) {
                try {
                    // 发送给特定用户的私有队列
                    messagingTemplate.convertAndSendToUser(
                            userIdStr, 
                            "/queue/order-updates", 
                            statusMessage
                    );
                    logger.info("WebSocket消息已推送 - 用户ID: {}, 订单ID: {}, 状态: {}, RequestID: {}", 
                            userIdStr, orderId, status, requestId);
                } catch (Exception e) {
                    logger.error("推送WebSocket消息失败 - 用户ID: {}, 订单ID: {}", userIdStr, orderId, e);
                }
            } else {
                logger.warn("用户没有活跃WebSocket连接，跳过推送 - 用户ID: {}, 订单ID: {}", userIdStr, orderId);
            }
            
            // 同时发送到公共主题（用于管理员监控）
            try {
                messagingTemplate.convertAndSend("/topic/order-updates", statusMessage);
            } catch (Exception e) {
                logger.error("推送公共主题消息失败 - 订单ID: {}", orderId, e);
            }
        } catch (Exception e) {
            logger.error("处理订单状态更新通知失败 - 用户ID: {}, 订单ID: {}", userId, orderId, e);
        }
    }

    /**
     * 推送订单创建成功消息
     */
    public void notifyOrderCreated(Long userId, Long orderId, BigDecimal totalPrice, String requestId) {
        notifyOrderStatusUpdate(userId, orderId, "PROCESSING", totalPrice, 
                "订单创建成功，正在处理中...", requestId);
    }

    /**
     * 推送订单处理完成消息
     */
    public void notifyOrderCompleted(Long userId, Long orderId, BigDecimal totalPrice, String requestId) {
        // 修复：订单完成应该是COMPLETED状态，而不是PENDING
        notifyOrderStatusUpdate(userId, orderId, "COMPLETED", totalPrice, 
                "订单处理完成！", requestId);
    }

    /**
     * 推送订单处理失败消息
     */
    public void notifyOrderFailed(Long userId, Long orderId, String errorMessage, String requestId) {
        notifyOrderStatusUpdate(userId, orderId, "FAILED", BigDecimal.ZERO, 
                "订单处理失败: " + errorMessage, requestId);
    }
}
