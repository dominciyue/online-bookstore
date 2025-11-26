package com.bookstore.online_bookstore_backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 标签DTO - 简单版本，不包含关系
 * 用于列表展示，避免循环引用
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagDTO {
    
    private Long id;
    private String name;
    private String nameEn;
    private Integer level;
    private String description;
}

