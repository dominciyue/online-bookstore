package com.bookstore.online_bookstore_backend.config; // 确保是正确的包名

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
// import org.springframework.web.servlet.config.annotation.CorsRegistry; // Commented out or removed
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:./uploads/default}") // Get the same upload directory path
    private String uploadDir;

    /*
        // Original CORS Mappings - now handled by WebSecurityConfig's CorsConfigurationSource
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/api/**") // 应用于所有 /api/ 开头的路径
                    .allowedOrigins("http://localhost:3000") // 允许的前端源
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的HTTP方法
                    .allowedHeaders("*") // 允许所有请求头
                    .allowCredentials(true); // 是否允许发送Cookie
        }
    */

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Example: Serving files from the ./uploads/avatars directory via /uploads/avatars/** URL path
        // Path to the directory where avatars are stored (relative to project root or absolute)
        // Make sure this matches or is consistent with FileStorageServiceImpl's rootLocation
        String resolvedUploadPath = Paths.get(uploadDir).toAbsolutePath().normalize().toString();
        if (!resolvedUploadPath.endsWith("/")) {
            resolvedUploadPath += "/";
        }

        // Serves files from the location specified by resolvedUploadPath (e.g., file:///E:/web/online-bookstore-backend/uploads/avatars/)
        // when a request comes to /uploads/avatars/** (or whatever you put in addResourceHandler)
        // So, if avatarUrl in DB is /uploads/avatars/foo.jpg, and uploadDir is ./uploads/avatars,
        // this will map http://localhost:8080/uploads/avatars/foo.jpg to the file system path.
        // The pathPattern should match the prefix of avatarUrl stored in the DB.
        registry.addResourceHandler("/uploads/avatars/**")
                .addResourceLocations("file:" + resolvedUploadPath);
        
        // If you have other static content directories, add them here too.
        // For example, to serve general static content like CSS/JS from src/main/resources/static/
        // registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
    }
}