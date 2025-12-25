package com.bookstore.hadoop;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.conf.Configuration;

import java.io.*;
import java.util.*;
import java.util.regex.*;

/**
 * KeywordCountMapper - MapReduce Mapper类
 * 
 * 功能：读取图书简介文本，统计每个关键词在当前行中的出现次数
 * 
 * 输入：
 * - Key: 行偏移量 (LongWritable)
 * - Value: 文本行内容 (Text)
 * 
 * 输出：
 * - Key: 关键词 (Text)
 * - Value: 出现次数 (IntWritable)
 */
public class KeywordCountMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
    
    // 关键词集合（转换为小写以便不区分大小写匹配）
    private Set<String> keywords;
    
    // 关键词到原始形式的映射（保持输出时的大小写）
    private Map<String, String> keywordOriginalMap;
    
    // 输出的Key
    private Text outputKey = new Text();
    
    // 输出的Value
    private final IntWritable outputValue = new IntWritable(1);
    
    /**
     * setup方法 - 在Mapper任务开始前执行
     * 用于加载关键词列表
     */
    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);
        
        keywords = new HashSet<>();
        keywordOriginalMap = new HashMap<>();
        
        Configuration conf = context.getConfiguration();
        
        // 从配置中获取关键词列表
        String keywordsString = conf.get("keywords.list");
        if (keywordsString != null && !keywordsString.isEmpty()) {
            String[] keywordArray = keywordsString.split(",");
            for (String keyword : keywordArray) {
                keyword = keyword.trim();
                if (!keyword.isEmpty() && !keyword.startsWith("#")) {
                    // 对于中文关键词，保持原样；对于英文关键词，转换为小写
                    if (containsChinese(keyword)) {
                        keywords.add(keyword);
                        keywordOriginalMap.put(keyword, keyword);
                    } else {
                        String lowerKeyword = keyword.toLowerCase();
                        keywords.add(lowerKeyword);
                        keywordOriginalMap.put(lowerKeyword, keyword);
                    }
                }
            }
        }
        
        System.out.println("Mapper初始化完成，加载了 " + keywords.size() + " 个关键词");
    }
    
    /**
     * map方法 - 处理每一行输入
     */
    @Override
    protected void map(LongWritable key, Text value, Context context) 
            throws IOException, InterruptedException {
        
        String line = value.toString();
        
        // 跳过注释行和元数据行
        if (line.trim().startsWith("#") || line.trim().startsWith("---") || 
            line.trim().startsWith("Title:") || line.trim().startsWith("Author:") ||
            line.trim().isEmpty()) {
            return;
        }
        
        // 移除 "Description: " 前缀（如果存在）
        if (line.startsWith("Description:")) {
            line = line.substring("Description:".length());
        }
        
        // 原始文本用于中文匹配，小写文本用于英文匹配
        String originalLine = line;
        String lowerLine = line.toLowerCase();
        
        // 对每个关键词进行统计
        for (String keyword : keywords) {
            // 根据关键词类型选择要匹配的文本
            String textToMatch = containsChinese(keyword) ? originalLine : lowerLine;
            int count = countKeywordOccurrences(textToMatch, keyword);
            if (count > 0) {
                // 使用原始大小写形式输出
                String originalKeyword = keywordOriginalMap.get(keyword);
                outputKey.set(originalKeyword);
                
                // 输出每次出现
                for (int i = 0; i < count; i++) {
                    context.write(outputKey, outputValue);
                }
            }
        }
    }
    
    /**
     * 统计关键词在文本中的出现次数
     * 对于中文关键词：直接进行子串匹配
     * 对于英文关键词：使用单词边界匹配，避免部分匹配
     */
    private int countKeywordOccurrences(String text, String keyword) {
        int count = 0;
        
        // 判断关键词是否包含中文字符
        boolean isChinese = containsChinese(keyword);
        
        if (isChinese) {
            // 中文关键词：直接进行子串匹配
            int index = 0;
            while ((index = text.indexOf(keyword, index)) != -1) {
                count++;
                index += keyword.length();
            }
        } else {
            // 英文关键词：使用单词边界匹配
            // \\b 表示单词边界
            String regex = "\\b" + Pattern.quote(keyword) + "\\b";
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            
            while (matcher.find()) {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * 判断字符串是否包含中文字符
     */
    private boolean containsChinese(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        for (char c : str.toCharArray()) {
            if (Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * cleanup方法 - 在Mapper任务结束后执行
     */
    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        super.cleanup(context);
        System.out.println("Mapper任务完成");
    }
}

