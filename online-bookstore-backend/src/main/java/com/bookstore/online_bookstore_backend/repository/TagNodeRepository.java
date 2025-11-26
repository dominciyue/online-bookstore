package com.bookstore.online_bookstore_backend.repository;

import com.bookstore.online_bookstore_backend.entity.neo4j.TagNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Neo4j标签节点Repository
 * 用于操作图书分类标签图
 */
@Repository
public interface TagNodeRepository extends Neo4jRepository<TagNode, Long> {
    
    // 根据标签名称查找
    Optional<TagNode> findByName(String name);
    
    // 根据标签英文名称查找
    Optional<TagNode> findByNameEn(String nameEn);
    
    // 查找指定层级的所有标签
    List<TagNode> findByLevel(Integer level);
    
    /**
     * 查找与指定标签通过2次边连接可以关联到的所有标签
     * 这是核心查询：从指定标签出发，沿着任意方向的HAS_SUBCATEGORY关系，
     * 最多经过2次跳转，找到所有相关标签
     */
    @Query("MATCH (start:Tag {name: $tagName})-[:HAS_SUBCATEGORY*0..2]-(related:Tag) " +
           "RETURN DISTINCT related")
    List<TagNode> findRelatedTagsWithin2Hops(@Param("tagName") String tagName);
    
    /**
     * 查找与指定标签通过2次边连接可以关联到的所有标签（不考虑方向）
     * 符合作业要求：2次边连接扩展搜索范围
     * 包括：自己、父节点、子节点、兄弟节点、爷爷节点、叔叔节点、孙子节点等
     */
    @Query("MATCH (start:Tag {name: $tagName})-[:HAS_SUBCATEGORY|RELATED_TO*0..2]-(related:Tag) " +
           "RETURN DISTINCT related")
    List<TagNode> findRelatedTagsWithin2HopsUndirected(@Param("tagName") String tagName);
    
    /**
     * 批量查找多个标签的相关标签（2次边连接内）
     * 符合作业要求：2次边连接扩展搜索范围
     */
    @Query("MATCH (start:Tag) WHERE start.name IN $tagNames " +
           "MATCH (start)-[:HAS_SUBCATEGORY|RELATED_TO*0..2]-(related:Tag) " +
           "RETURN DISTINCT related")
    List<TagNode> findRelatedTagsForMultipleTags(@Param("tagNames") List<String> tagNames);
    
    /**
     * 查找指定标签的所有子标签（包括多级）
     */
    @Query("MATCH (parent:Tag {name: $tagName})-[:HAS_SUBCATEGORY*1..]->(child:Tag) " +
           "RETURN DISTINCT child")
    List<TagNode> findAllDescendants(@Param("tagName") String tagName);
    
    /**
     * 查找指定标签的所有父标签（包括多级）
     */
    @Query("MATCH (child:Tag {name: $tagName})<-[:HAS_SUBCATEGORY*1..]-(parent:Tag) " +
           "RETURN DISTINCT parent")
    List<TagNode> findAllAncestors(@Param("tagName") String tagName);
    
    /**
     * 获取完整的标签树（所有根节点及其子节点）
     */
    @Query("MATCH (root:Tag {level: 0}) " +
           "OPTIONAL MATCH (root)-[:HAS_SUBCATEGORY*1..]->(child:Tag) " +
           "RETURN root, collect(DISTINCT child)")
    List<TagNode> findTagTree();
    
    /**
     * 获取所有标签节点
     */
    @Query("MATCH (n:Tag) RETURN n")
    List<TagNode> findAllTags();
    
    /**
     * 查找与指定标签直接相关的标签（只有1次边连接）
     */
    @Query("MATCH (start:Tag {name: $tagName})-[:HAS_SUBCATEGORY|RELATED_TO]-(related:Tag) " +
           "RETURN DISTINCT related")
    List<TagNode> findDirectlyRelatedTags(@Param("tagName") String tagName);
}

