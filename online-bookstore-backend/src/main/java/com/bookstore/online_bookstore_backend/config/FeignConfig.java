package com.bookstore.online_bookstore_backend.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign配置类
 */
@Configuration
public class FeignConfig {
    
    /**
     * Feign日志级别
     */
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;  // 记录完整的请求和响应
    }
}

