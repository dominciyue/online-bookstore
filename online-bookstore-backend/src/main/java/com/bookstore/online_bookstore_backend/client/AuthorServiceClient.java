package com.bookstore.online_bookstore_backend.client;

import com.bookstore.online_bookstore_backend.dto.AuthorResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Author Service Feign客户端
 * 
 * 通过服务名调用author-service微服务
 * Eureka会自动解析服务名到实际的服务实例地址
 */
@FeignClient(
    name = "author-service",  // 在Eureka注册的服务名
    path = "/api/authors"      // 服务的基础路径
)
public interface AuthorServiceClient {
    
    /**
     * 根据书名精确查询作者信息
     * @param bookTitle 书名
     * @return 作者信息
     */
    @GetMapping("/by-book")
    AuthorResponseDTO getAuthorByBookTitle(@RequestParam("title") String bookTitle);
    
    /**
     * 根据书名关键词模糊查询作者信息
     * @param keyword 关键词
     * @return 作者信息列表
     */
    @GetMapping("/search")
    List<AuthorResponseDTO> searchAuthorsByKeyword(@RequestParam("keyword") String keyword);
}

