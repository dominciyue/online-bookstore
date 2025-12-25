package com.bookstore.hadoop;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

/**
 * 图书数据导出工具
 * 从数据库中读取图书信息，按分类将简介导出到不同的文本文件
 * 
 * 功能：
 * 1. 连接MySQL数据库
 * 2. 查询所有未删除的图书
 * 3. 按分类(category)分组
 * 4. 将每个分类的图书简介写入对应的文本文件
 */
public class BookDataExporter {
    
    // 数据库连接配置
    private static final String DB_URL = "jdbc:mysql://localhost:3306/bookstore_db?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Zy050811";
    
    // 输出目录
    private static final String OUTPUT_DIR = "input";
    
    // 分类名称到文件名的映射
    private static final Map<String, String> CATEGORY_FILE_MAP = new HashMap<>();
    
    static {
        // 初始化分类映射 - 根据数据库中的实际分类
        // 数据库分类: cs-classic, web-dev, programming-lang, sci-fi
        
        // 计算机经典
        CATEGORY_FILE_MAP.put("cs-classic", "CS_Classic");
        CATEGORY_FILE_MAP.put("计算机", "CS_Classic");
        CATEGORY_FILE_MAP.put("Computer Science", "CS_Classic");
        
        // Web开发
        CATEGORY_FILE_MAP.put("web-dev", "Web_Dev");
        CATEGORY_FILE_MAP.put("前端", "Web_Dev");
        CATEGORY_FILE_MAP.put("后端", "Web_Dev");
        CATEGORY_FILE_MAP.put("Web", "Web_Dev");
        
        // 编程语言
        CATEGORY_FILE_MAP.put("programming-lang", "Programming");
        CATEGORY_FILE_MAP.put("编程", "Programming");
        CATEGORY_FILE_MAP.put("Programming", "Programming");
        
        // 科幻小说
        CATEGORY_FILE_MAP.put("sci-fi", "SciFi");
        CATEGORY_FILE_MAP.put("科幻", "SciFi");
        CATEGORY_FILE_MAP.put("Science Fiction", "SciFi");
        CATEGORY_FILE_MAP.put("Sci-Fi", "SciFi");
        
        // 其他可能的分类
        CATEGORY_FILE_MAP.put("小说", "Novel");
        CATEGORY_FILE_MAP.put("Fiction", "Novel");
        CATEGORY_FILE_MAP.put("文学", "Literature");
        CATEGORY_FILE_MAP.put("Literature", "Literature");
        CATEGORY_FILE_MAP.put("商业", "Business");
        CATEGORY_FILE_MAP.put("Business", "Business");
        CATEGORY_FILE_MAP.put("历史", "History");
        CATEGORY_FILE_MAP.put("History", "History");
    }
    
    /**
     * 主函数 - 执行数据导出
     */
    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("  E-BookStore 图书简介数据导出工具");
        System.out.println("==========================================");
        
        // 解析命令行参数
        String outputDir = OUTPUT_DIR;
        String dbUrl = DB_URL;
        String dbUser = DB_USER;
        String dbPassword = DB_PASSWORD;
        
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-o":
                case "--output":
                    if (i + 1 < args.length) {
                        outputDir = args[++i];
                    }
                    break;
                case "-h":
                case "--host":
                    if (i + 1 < args.length) {
                        String host = args[++i];
                        dbUrl = "jdbc:mysql://" + host + "/online_bookstore?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8";
                    }
                    break;
                case "-u":
                case "--user":
                    if (i + 1 < args.length) {
                        dbUser = args[++i];
                    }
                    break;
                case "-p":
                case "--password":
                    if (i + 1 < args.length) {
                        dbPassword = args[++i];
                    }
                    break;
                case "--help":
                    printHelp();
                    return;
            }
        }
        
        BookDataExporter exporter = new BookDataExporter();
        exporter.export(outputDir, dbUrl, dbUser, dbPassword);
    }
    
    /**
     * 打印帮助信息
     */
    private static void printHelp() {
        System.out.println("Usage: java -cp bookstore-keyword-count.jar com.bookstore.hadoop.BookDataExporter [options]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  -o, --output <dir>     输出目录 (默认: input)");
        System.out.println("  -h, --host <host:port> 数据库主机 (默认: localhost:3306)");
        System.out.println("  -u, --user <user>      数据库用户名 (默认: root)");
        System.out.println("  -p, --password <pwd>   数据库密码 (默认: 123456)");
        System.out.println("  --help                 显示帮助信息");
    }
    
    /**
     * 执行导出操作
     */
    public void export(String outputDir, String dbUrl, String dbUser, String dbPassword) {
        System.out.println("\n[1/4] 创建输出目录: " + outputDir);
        
        // 创建输出目录
        Path outputPath = Paths.get(outputDir);
        try {
            Files.createDirectories(outputPath);
            System.out.println("      ✓ 输出目录已创建");
        } catch (IOException e) {
            System.err.println("      ✗ 创建输出目录失败: " + e.getMessage());
            return;
        }
        
        System.out.println("\n[2/4] 连接数据库...");
        
        // 存储按分类分组的图书简介
        Map<String, List<BookDescription>> categoryBooks = new HashMap<>();
        int totalBooks = 0;
        
        try {
            // 加载MySQL驱动
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("      ✓ MySQL驱动加载成功");
        } catch (ClassNotFoundException e) {
            System.err.println("      ✗ MySQL驱动加载失败: " + e.getMessage());
            return;
        }
        
        // 连接数据库并查询
        String sql = "SELECT id, title, author, category, description FROM books WHERE deleted = false AND description IS NOT NULL";
        
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            System.out.println("      ✓ 数据库连接成功");
            System.out.println("\n[3/4] 读取图书数据...");
            
            while (rs.next()) {
                long id = rs.getLong("id");
                String title = rs.getString("title");
                String author = rs.getString("author");
                String category = rs.getString("category");
                String description = rs.getString("description");
                
                if (category == null || category.trim().isEmpty()) {
                    category = "Other";
                }
                
                // 规范化分类名称
                String normalizedCategory = normalizeCategory(category);
                
                // 创建图书描述对象
                BookDescription bookDesc = new BookDescription(id, title, author, category, description);
                
                // 添加到对应分类的列表中
                categoryBooks.computeIfAbsent(normalizedCategory, k -> new ArrayList<>()).add(bookDesc);
                totalBooks++;
            }
            
            System.out.println("      ✓ 共读取 " + totalBooks + " 本图书");
            System.out.println("      ✓ 共有 " + categoryBooks.size() + " 个分类");
            
        } catch (SQLException e) {
            System.err.println("      ✗ 数据库操作失败: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        
        // 写入文件
        System.out.println("\n[4/4] 导出数据到文件...");
        
        int filesCreated = 0;
        for (Map.Entry<String, List<BookDescription>> entry : categoryBooks.entrySet()) {
            String category = entry.getKey();
            List<BookDescription> books = entry.getValue();
            
            String fileName = category + ".txt";
            Path filePath = outputPath.resolve(fileName);
            
            try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                // 写入文件头
                writer.write("# Category: " + category);
                writer.newLine();
                writer.write("# Total Books: " + books.size());
                writer.newLine();
                writer.write("# ==========================================");
                writer.newLine();
                writer.newLine();
                
                // 写入每本书的简介
                for (BookDescription book : books) {
                    writer.write("--- Book ID: " + book.id + " ---");
                    writer.newLine();
                    writer.write("Title: " + book.title);
                    writer.newLine();
                    writer.write("Author: " + (book.author != null ? book.author : "Unknown"));
                    writer.newLine();
                    writer.write("Description: " + cleanDescription(book.description));
                    writer.newLine();
                    writer.newLine();
                }
                
                filesCreated++;
                System.out.println("      ✓ " + fileName + " (" + books.size() + " 本书)");
                
            } catch (IOException e) {
                System.err.println("      ✗ 写入文件失败 " + fileName + ": " + e.getMessage());
            }
        }
        
        // 输出汇总
        System.out.println("\n==========================================");
        System.out.println("  导出完成!");
        System.out.println("  - 图书总数: " + totalBooks);
        System.out.println("  - 文件数量: " + filesCreated);
        System.out.println("  - 输出目录: " + outputPath.toAbsolutePath());
        System.out.println("==========================================");
        
        // 列出生成的文件
        System.out.println("\n生成的文件列表:");
        try {
            Files.list(outputPath)
                 .filter(p -> p.toString().endsWith(".txt"))
                 .forEach(p -> {
                     try {
                         long size = Files.size(p);
                         System.out.println("  - " + p.getFileName() + " (" + size + " bytes)");
                     } catch (IOException e) {
                         System.out.println("  - " + p.getFileName());
                     }
                 });
        } catch (IOException e) {
            // 忽略
        }
    }
    
    /**
     * 规范化分类名称，映射到对应的文件名
     */
    private String normalizeCategory(String category) {
        // 首先尝试精确匹配
        if (CATEGORY_FILE_MAP.containsKey(category)) {
            return CATEGORY_FILE_MAP.get(category);
        }
        
        // 尝试不区分大小写匹配
        for (Map.Entry<String, String> entry : CATEGORY_FILE_MAP.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(category)) {
                return entry.getValue();
            }
        }
        
        // 尝试部分匹配
        String lowerCategory = category.toLowerCase();
        for (Map.Entry<String, String> entry : CATEGORY_FILE_MAP.entrySet()) {
            if (lowerCategory.contains(entry.getKey().toLowerCase())) {
                return entry.getValue();
            }
        }
        
        // 如果没有匹配，使用原分类名（去除特殊字符）
        return category.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "_");
    }
    
    /**
     * 清理描述文本，移除特殊字符和多余空白
     */
    private String cleanDescription(String description) {
        if (description == null) {
            return "";
        }
        // 移除HTML标签
        description = description.replaceAll("<[^>]+>", " ");
        // 规范化空白字符
        description = description.replaceAll("\\s+", " ");
        // 去除首尾空白
        return description.trim();
    }
    
    /**
     * 图书描述内部类
     */
    private static class BookDescription {
        long id;
        String title;
        String author;
        String category;
        String description;
        
        BookDescription(long id, String title, String author, String category, String description) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.category = category;
            this.description = description;
        }
    }
}

