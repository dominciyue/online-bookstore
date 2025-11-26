package com.bookstore.online_bookstore_backend.config;

import org.neo4j.driver.Driver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.neo4j.core.DatabaseSelectionProvider;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.Neo4jOperations;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext;
import org.springframework.data.neo4j.core.transaction.Neo4jTransactionManager;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;

/**
 * Neo4j配置类
 * 在多数据源环境下，需要明确配置各个事务管理器并指定主事务管理器
 */
@Configuration
@EnableNeo4jRepositories(basePackages = "com.bookstore.online_bookstore_backend.repository")
@EnableTransactionManagement
public class Neo4jConfig {

    /**
     * JPA事务管理器 - 设置为主事务管理器
     * 这样JPA相关的操作会默认使用这个事务管理器
     */
    @Primary
    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    /**
     * Neo4j事务管理器 - 用于Neo4j Repository
     * 不设置为Primary，避免冲突
     */
    @Bean(name = "neo4jTransactionManager")
    public PlatformTransactionManager neo4jTransactionManager(
            Driver driver,
            DatabaseSelectionProvider databaseSelectionProvider) {
        return new Neo4jTransactionManager(driver, databaseSelectionProvider);
    }

    /**
     * Neo4j Template - 手动配置以使用neo4jTransactionManager
     */
    @Bean
    public Neo4jOperations neo4jTemplate(
            Neo4jClient neo4jClient,
            Neo4jMappingContext neo4jMappingContext,
            @Qualifier("neo4jTransactionManager") PlatformTransactionManager neo4jTransactionManager) {
        return new Neo4jTemplate(neo4jClient, neo4jMappingContext, neo4jTransactionManager);
    }
}
