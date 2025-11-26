package com.bookstore.online_bookstore_backend.entity.neo4j;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Set;

/**
 * Neo4j标签节点实体类
 * 表示图书分类标签及其层级关系
 */
@Node("Tag")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"subcategories", "parentCategories", "relatedTags"})
@ToString(exclude = {"subcategories", "parentCategories", "relatedTags"})
public class TagNode {
    
    @Id
    @GeneratedValue
    private Long id;
    
    // 标签中文名称
    private String name;
    
    // 标签英文名称（用于匹配MySQL中的category字段）
    private String nameEn;
    
    // 标签层级（0=根分类，1=二级分类，2=三级分类）
    private Integer level;
    
    // 标签描述
    private String description;
    
    // 子分类（出向关系）
    @Relationship(type = "HAS_SUBCATEGORY", direction = Relationship.Direction.OUTGOING)
    @JsonIgnore  // 防止JSON序列化时循环引用
    private Set<TagNode> subcategories = new HashSet<>();
    
    // 父分类（入向关系）
    @Relationship(type = "HAS_SUBCATEGORY", direction = Relationship.Direction.INCOMING)
    @JsonIgnore  // 防止JSON序列化时循环引用
    private Set<TagNode> parentCategories = new HashSet<>();
    
    // 相关标签（无向关系）
    @Relationship(type = "RELATED_TO", direction = Relationship.Direction.OUTGOING)
    @JsonIgnore  // 防止JSON序列化时循环引用
    private Set<TagNode> relatedTags = new HashSet<>();
}

