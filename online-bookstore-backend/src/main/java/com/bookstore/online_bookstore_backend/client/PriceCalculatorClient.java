package com.bookstore.online_bookstore_backend.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.List;

/**
 * Price Calculator Service Feign客户端
 * 调用函数式价格计算服务
 */
@FeignClient(
    name = "price-calculator-service",
    path = "/api/calculator"
)
public interface PriceCalculatorClient {
    
    /**
     * 计算单个商品项总价
     */
    @PostMapping("/item")
    CalculationResponseDTO calculateItem(@RequestBody CalculationRequestDTO request);
    
    /**
     * 批量计算订单总价
     */
    @PostMapping("/batch")
    BatchCalculationResponseDTO calculateBatch(@RequestBody BatchCalculationRequestDTO request);
    
    /**
     * 计算请求DTO
     */
    record CalculationRequestDTO(BigDecimal price, Integer quantity) {}
    
    /**
     * 计算响应DTO
     */
    record CalculationResponseDTO(
        BigDecimal price,
        Integer quantity,
        BigDecimal totalPrice,
        Long timestamp,
        String instanceId
    ) {}
    
    /**
     * 批量计算请求DTO
     */
    record BatchCalculationRequestDTO(Long orderId, List<CalculationRequestDTO> items) {}
    
    /**
     * 批量计算响应DTO
     */
    record BatchCalculationResponseDTO(
        Long orderId,
        List<CalculationResponseDTO> items,
        BigDecimal orderTotal,
        Long processingTimeMs,
        String instanceId
    ) {}
}

