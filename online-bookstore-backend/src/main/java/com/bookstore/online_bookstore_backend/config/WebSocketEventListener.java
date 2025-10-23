package com.bookstore.online_bookstore_backend.config;

import com.bookstore.online_bookstore_backend.service.WebSocketNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * WebSocket事件监听器
 * 负责处理WebSocket连接和断开事件，管理用户Session
 */
@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    private WebSocketNotificationService webSocketNotificationService;

    /**
     * 处理WebSocket连接事件
     * 在此事件中注册用户Session（此时Session属性已在Interceptor中设置）
     * @param event 连接事件
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        
        // 从Session属性中获取用户ID和Session ID
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        String sessionId = headerAccessor.getSessionId();
        
        if (userId != null && sessionId != null) {
            // 注册用户Session
            webSocketNotificationService.registerUserSession(userId, sessionId);
            logger.info("WebSocket连接已建立并注册 - 用户ID: {}, Session: {}", userId, sessionId);
        } else {
            logger.warn("WebSocket连接已建立，但无法获取用户信息 - Session: {}", sessionId);
        }
    }

    /**
     * 处理WebSocket断开事件
     * @param event 断开事件
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        
        // 从Session属性中获取用户ID和Session ID
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        String sessionId = headerAccessor.getSessionId();
        
        if (userId != null && sessionId != null) {
            // 注销用户Session
            webSocketNotificationService.unregisterUserSession(userId, sessionId);
            logger.info("WebSocket连接已断开 - 用户ID: {}, Session: {}", userId, sessionId);
        } else {
            logger.warn("WebSocket连接已断开，但无法获取用户信息 - Session: {}", sessionId);
        }
    }
}
