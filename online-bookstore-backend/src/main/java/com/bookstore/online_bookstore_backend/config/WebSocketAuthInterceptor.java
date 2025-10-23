package com.bookstore.online_bookstore_backend.config;

import com.bookstore.online_bookstore_backend.entity.User;
import com.bookstore.online_bookstore_backend.security.jwt.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

/**
 * WebSocket认证拦截器
 * 用于处理WebSocket连接时的JWT token认证
 */
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 从WebSocket连接头中获取token
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                
                try {
                    if (jwtUtils.validateJwtToken(token)) {
                        String username = jwtUtils.getUserNameFromJwtToken(token);
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        
                        // 从UserDetails中获取真实的用户ID
                        String userId = null;
                        if (userDetails instanceof User) {
                            User user = (User) userDetails;
                            userId = user.getId().toString();
                            logger.info("WebSocket认证成功 - 用户名: {}, 用户ID: {}", username, userId);
                        } else {
                            logger.warn("UserDetails不是User类型，无法获取用户ID");
                        }
                        
                        UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        
                        // 设置用户信息到WebSocket会话中
                        accessor.setUser(authentication);
                        
                        // 将用户ID存储到Session属性中，供SessionConnectedEvent使用
                        if (userId != null) {
                            accessor.getSessionAttributes().put("userId", userId);
                            logger.debug("用户ID已保存到Session属性 - 用户ID: {}, Session: {}", userId, accessor.getSessionId());
                        } else {
                            logger.error("无法获取用户ID");
                        }
                    }
                } catch (Exception e) {
                    logger.error("WebSocket认证失败: {}", e.getMessage(), e);
                }
            } else {
                logger.warn("WebSocket连接缺少Authorization头或格式不正确");
            }
        }
        
        return message;
    }
}
