package com.bookstore.online_bookstore_backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 标签树DTO
 * 用于前端显示标签层级结构，避免循环引用问题
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagTreeDTO {
    
    private Long id;
    private String name;
    private String nameEn;
    private Integer level;
    private String description;
    
    // 只包含子分类，不包含父分类，避免循环引用
    private List<TagTreeDTO> subcategories = new ArrayList<>();
}

