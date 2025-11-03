package com.bookstore.online_bookstore_backend.controller;

import com.bookstore.online_bookstore_backend.client.AuthorServiceClient;
import com.bookstore.online_bookstore_backend.dto.AuthorResponseDTO;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 作者查询控制器
 * 通过Feign调用author-service微服务
 */
@RestController
@RequestMapping("/api/authors")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"}, allowCredentials = "true")
public class AuthorController {

    private static final Logger logger = LoggerFactory.getLogger(AuthorController.class);

    @Autowired
    private AuthorServiceClient authorServiceClient;

    /**
     * 根据书名精确查询作者
     * GET /api/authors/by-book?title=xxx
     */
    @GetMapping("/by-book")
    public ResponseEntity<?> getAuthorByBookTitle(@RequestParam("title") String bookTitle) {
        logger.info("📖 [主服务] 收到作者查询请求: bookTitle={}", bookTitle);
        
        if (bookTitle == null || bookTitle.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "书名不能为空"));
        }
        
        try {
            // 通过Feign调用author-service
            AuthorResponseDTO response = authorServiceClient.getAuthorByBookTitle(bookTitle.trim());
            logger.info("✅ [主服务] 成功从author-service获取数据: author={}", response.getAuthor());
            return ResponseEntity.ok(response);
        } catch (FeignException.NotFound e) {
            logger.warn("❌ [主服务] 未找到书籍: {}", bookTitle);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "未找到书籍: " + bookTitle));
        } catch (FeignException e) {
            logger.error("❌ [主服务] 调用author-service失败: status={}, message={}", 
                        e.status(), e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("message", "作者服务暂时不可用，请稍后重试"));
        } catch (Exception e) {
            logger.error("❌ [主服务] 处理请求时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "服务器内部错误"));
        }
    }

    /**
     * 根据书名关键词模糊查询作者
     * GET /api/authors/search?keyword=xxx
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchAuthorsByKeyword(@RequestParam("keyword") String keyword) {
        logger.info("🔍 [主服务] 收到模糊查询请求: keyword={}", keyword);
        
        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "关键词不能为空"));
        }
        
        try {
            // 通过Feign调用author-service
            List<AuthorResponseDTO> responses = authorServiceClient.searchAuthorsByKeyword(keyword.trim());
            logger.info("✅ [主服务] 成功从author-service获取数据: count={}", responses.size());
            
            if (responses.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "未找到相关书籍: " + keyword));
            }
            
            return ResponseEntity.ok(responses);
        } catch (FeignException.NotFound e) {
            logger.warn("❌ [主服务] 未找到相关书籍: {}", keyword);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "未找到相关书籍: " + keyword));
        } catch (FeignException e) {
            logger.error("❌ [主服务] 调用author-service失败: status={}, message={}", 
                        e.status(), e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("message", "作者服务暂时不可用，请稍后重试"));
        } catch (Exception e) {
            logger.error("❌ [主服务] 处理请求时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "服务器内部错误"));
        }
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "bookstore-backend-author-controller",
                "timestamp", System.currentTimeMillis()
        ));
    }
}

