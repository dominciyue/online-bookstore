package com.bookstore.online_bookstore_backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket配置类
 * 用于支持实时订单状态推送
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单消息代理，处理以"/topic"和"/queue"开头的消息
        config.enableSimpleBroker("/topic", "/queue");
        // 设置应用程序目标前缀
        config.setApplicationDestinationPrefixes("/app");
        // 设置用户目标前缀，用于点对点消息
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 启用认证拦截器
        registration.interceptors(webSocketAuthInterceptor);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册WebSocket端点，允许跨域
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // 允许所有来源，生产环境应改为具体域名
                .withSockJS();
    }
}
