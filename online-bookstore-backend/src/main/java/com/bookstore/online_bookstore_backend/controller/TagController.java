package com.bookstore.online_bookstore_backend.controller;

import com.bookstore.online_bookstore_backend.dto.TagDTO;
import com.bookstore.online_bookstore_backend.dto.TagTreeDTO;
import com.bookstore.online_bookstore_backend.entity.neo4j.TagNode;
import com.bookstore.online_bookstore_backend.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 标签控制器
 * 提供标签查询和搜索相关的API接口
 */
@RestController
@RequestMapping("/api/tags")
@CrossOrigin(origins = "http://localhost:3000")
public class TagController {
    
    @Autowired
    private TagService tagService;
    
    /**
     * 获取所有标签
     * GET /api/tags
     */
    @GetMapping
    public ResponseEntity<List<TagDTO>> getAllTags() {
        List<TagDTO> tags = tagService.getAllTags();
        return ResponseEntity.ok(tags);
    }
    
    /**
     * 获取标签树结构（用于前端展示分类树）
     * GET /api/tags/tree
     */
    @GetMapping("/tree")
    public ResponseEntity<List<TagTreeDTO>> getTagTree() {
        List<TagTreeDTO> tagTree = tagService.getTagTree();
        return ResponseEntity.ok(tagTree);
    }
    
    /**
     * 获取根标签（顶级分类）
     * GET /api/tags/roots
     */
    @GetMapping("/roots")
    public ResponseEntity<List<TagDTO>> getRootTags() {
        List<TagDTO> rootTags = tagService.getRootTags();
        return ResponseEntity.ok(rootTags);
    }
    
    /**
     * 根据标签名获取相关标签（2次边连接内）
     * GET /api/tags/related?tag=技术类
     * 
     * 这是实现标签搜索功能的核心接口
     */
    @GetMapping("/related")
    public ResponseEntity<Set<String>> getRelatedTags(@RequestParam String tag) {
        Set<String> relatedTags = tagService.findRelatedTagNames(tag);
        return ResponseEntity.ok(relatedTags);
    }
    
    /**
     * 批量获取多个标签的相关标签（2次边连接内）
     * POST /api/tags/related-batch
     * Body: ["技术类", "编程语言"]
     * 
     * 用于用户同时选择多个标签进行搜索的场景
     */
    @PostMapping("/related-batch")
    public ResponseEntity<Set<String>> getRelatedTagsBatch(@RequestBody List<String> tags) {
        Set<String> relatedTags = tagService.findRelatedTagNames(tags);
        return ResponseEntity.ok(relatedTags);
    }
    
    /**
     * 获取指定标签的子标签
     * GET /api/tags/{tagName}/descendants
     */
    @GetMapping("/{tagName}/descendants")
    public ResponseEntity<List<TagDTO>> getDescendants(@PathVariable String tagName) {
        List<TagDTO> descendants = tagService.getDescendants(tagName);
        return ResponseEntity.ok(descendants);
    }
    
    /**
     * 获取指定标签的父标签
     * GET /api/tags/{tagName}/ancestors
     */
    @GetMapping("/{tagName}/ancestors")
    public ResponseEntity<List<TagDTO>> getAncestors(@PathVariable String tagName) {
        List<TagDTO> ancestors = tagService.getAncestors(tagName);
        return ResponseEntity.ok(ancestors);
    }
    
    /**
     * 获取与指定标签直接相关的标签（1次边连接）
     * GET /api/tags/{tagName}/directly-related
     */
    @GetMapping("/{tagName}/directly-related")
    public ResponseEntity<List<TagDTO>> getDirectlyRelatedTags(@PathVariable String tagName) {
        List<TagDTO> directlyRelated = tagService.getDirectlyRelatedTags(tagName);
        return ResponseEntity.ok(directlyRelated);
    }
}

