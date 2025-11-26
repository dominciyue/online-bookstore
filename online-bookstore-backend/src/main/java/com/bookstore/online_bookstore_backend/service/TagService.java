package com.bookstore.online_bookstore_backend.service;

import com.bookstore.online_bookstore_backend.dto.TagDTO;
import com.bookstore.online_bookstore_backend.dto.TagTreeDTO;
import com.bookstore.online_bookstore_backend.entity.neo4j.TagNode;
import com.bookstore.online_bookstore_backend.repository.TagNodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 标签服务类
 * 处理标签图的查询和操作
 */
@Service
public class TagService {
    
    @Autowired
    private TagNodeRepository tagNodeRepository;
    
    /**
     * 获取所有标签（转换为DTO）
     */
    @Transactional(readOnly = true)
    public List<TagDTO> getAllTags() {
        return tagNodeRepository.findAllTags().stream()
                .map(this::convertToSimpleDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取根标签（顶级分类）
     */
    @Transactional(readOnly = true)
    public List<TagDTO> getRootTags() {
        return tagNodeRepository.findByLevel(0).stream()
                .map(this::convertToSimpleDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 将TagNode转换为简单DTO（不包含关系）
     */
    private TagDTO convertToSimpleDTO(TagNode node) {
        TagDTO dto = new TagDTO();
        dto.setId(node.getId());
        dto.setName(node.getName());
        dto.setNameEn(node.getNameEn());
        dto.setLevel(node.getLevel());
        dto.setDescription(node.getDescription());
        return dto;
    }
    
    /**
     * 获取标签树结构（用于前端显示）
     * 转换为DTO避免循环引用
     */
    @Transactional(readOnly = true)
    public List<TagTreeDTO> getTagTree() {
        List<TagNode> rootNodes = tagNodeRepository.findByLevel(0);
        return rootNodes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 将TagNode转换为TagTreeDTO，递归构建子树
     */
    private TagTreeDTO convertToDTO(TagNode node) {
        TagTreeDTO dto = new TagTreeDTO();
        dto.setId(node.getId());
        dto.setName(node.getName());
        dto.setNameEn(node.getNameEn());
        dto.setLevel(node.getLevel());
        dto.setDescription(node.getDescription());
        
        // 递归转换子分类
        if (node.getSubcategories() != null && !node.getSubcategories().isEmpty()) {
            List<TagTreeDTO> subDTOs = node.getSubcategories().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            dto.setSubcategories(subDTOs);
        }
        
        return dto;
    }
    
    /**
     * 根据标签名查找标签节点
     */
    @Transactional(readOnly = true)
    public TagNode getTagByName(String name) {
        return tagNodeRepository.findByName(name).orElse(null);
    }
    
    /**
     * 核心方法：查找与指定标签通过2次边连接相关的所有标签名称
     * 这是实现标签搜索功能的关键
     * 
     * @param tagName 用户选择的标签名称
     * @return 所有相关标签的名称集合（用于MySQL搜索）
     */
    @Transactional(readOnly = true)
    public Set<String> findRelatedTagNames(String tagName) {
        // 使用无向关系查询，找到2次边连接内的所有相关标签
        List<TagNode> relatedTags = tagNodeRepository.findRelatedTagsWithin2HopsUndirected(tagName);
        
        // 提取所有相关标签的名称
        return relatedTags.stream()
                .map(TagNode::getName)
                .collect(Collectors.toSet());
    }
    
    /**
     * 批量查找多个标签的相关标签名称
     * 用于用户同时选择多个标签进行搜索的场景
     * 
     * @param tagNames 用户选择的多个标签名称
     * @return 所有相关标签的名称集合（并集）
     */
    @Transactional(readOnly = true)
    public Set<String> findRelatedTagNames(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return Set.of();
        }
        
        // 查找所有相关标签
        List<TagNode> relatedTags = tagNodeRepository.findRelatedTagsForMultipleTags(tagNames);
        
        // 提取所有相关标签的名称
        return relatedTags.stream()
                .map(TagNode::getName)
                .collect(Collectors.toSet());
    }
    
    /**
     * 查找指定标签的所有子标签
     */
    @Transactional(readOnly = true)
    public List<TagDTO> getDescendants(String tagName) {
        return tagNodeRepository.findAllDescendants(tagName).stream()
                .map(this::convertToSimpleDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 查找指定标签的所有父标签
     */
    @Transactional(readOnly = true)
    public List<TagDTO> getAncestors(String tagName) {
        return tagNodeRepository.findAllAncestors(tagName).stream()
                .map(this::convertToSimpleDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 查找与指定标签直接相关的标签（1次边连接）
     */
    @Transactional(readOnly = true)
    public List<TagDTO> getDirectlyRelatedTags(String tagName) {
        return tagNodeRepository.findDirectlyRelatedTags(tagName).stream()
                .map(this::convertToSimpleDTO)
                .collect(Collectors.toList());
    }
}

