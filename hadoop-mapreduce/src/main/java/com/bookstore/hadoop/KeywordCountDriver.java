package com.bookstore.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * KeywordCountDriver - MapReduce作业驱动类
 * 
 * 功能：配置并启动MapReduce作业，统计图书简介中关键词出现次数
 * 
 * 使用方法：
 * hadoop jar bookstore-keyword-count.jar com.bookstore.hadoop.KeywordCountDriver [input] [output] [keywords_file]
 * 
 * 参数：
 * - input: 输入目录（包含按分类分组的图书简介文件）
 * - output: 输出目录（存放统计结果）
 * - keywords_file: 关键词列表文件（可选，默认使用内置关键词）
 */
public class KeywordCountDriver extends Configured implements Tool {
    
    // 默认关键词列表（中英文混合）
    private static final String[] DEFAULT_KEYWORDS = {
        // 编程语言（中英文）
        "Java", "JavaScript", "Python", "C++", "编程", "程序", "代码", "语言",
        // 计算机概念
        "计算机", "系统", "算法", "数据", "结构", "数据结构", "内存", "底层", "原理",
        "处理器", "操作系统", "网络", "架构",
        // 开发相关
        "开发", "前端", "后端", "Web", "设计", "模式", "框架", "实践", "项目", 
        "技术", "核心", "高级",
        // 经典/入门
        "经典", "入门", "畅销", "红宝书", "圣经", "必备", "掌握", "学习", "教程", "指南",
        // 科幻相关
        "三体", "黑暗森林", "死神永生", "科幻", "里程碑", "三部曲",
        "宇宙", "星际", "太空", "外星", "文明", "时间", "未来", "机器人", "人工智能",
        // 文学通用
        "故事", "小说", "全集", "作品", "世界", "人类", "生命", "思想", "哲学",
        // 描述性词汇
        "深入", "详细", "全面", "覆盖", "剖析", "理解", "讲解", "引导", "包含", "领域"
    };
    
    /**
     * 主函数入口
     */
    public static void main(String[] args) throws Exception {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║       E-BookStore 图书简介关键词统计 MapReduce 程序           ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  功能: 统计图书简介中各关键词出现的次数                       ║");
        System.out.println("║  作者: E-BookStore Team                                      ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
        
        // 使用ToolRunner运行作业
        int exitCode = ToolRunner.run(new Configuration(), new KeywordCountDriver(), args);
        System.exit(exitCode);
    }
    
    /**
     * 运行MapReduce作业
     */
    @Override
    public int run(String[] args) throws Exception {
        // 解析参数
        String inputPath = "input";
        String outputPath = "output";
        String keywordsFile = null;
        
        // 解析命令行参数
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-i":
                case "--input":
                    if (i + 1 < args.length) {
                        inputPath = args[++i];
                    }
                    break;
                case "-o":
                case "--output":
                    if (i + 1 < args.length) {
                        outputPath = args[++i];
                    }
                    break;
                case "-k":
                case "--keywords":
                    if (i + 1 < args.length) {
                        keywordsFile = args[++i];
                    }
                    break;
                case "--help":
                    printHelp();
                    return 0;
                default:
                    // 兼容位置参数
                    if (!args[i].startsWith("-")) {
                        if (inputPath.equals("input")) {
                            inputPath = args[i];
                        } else if (outputPath.equals("output")) {
                            outputPath = args[i];
                        } else if (keywordsFile == null) {
                            keywordsFile = args[i];
                        }
                    }
            }
        }
        
        System.out.println("[配置信息]");
        System.out.println("  输入目录: " + inputPath);
        System.out.println("  输出目录: " + outputPath);
        System.out.println("  关键词文件: " + (keywordsFile != null ? keywordsFile : "使用默认关键词"));
        System.out.println();
        
        // 获取配置
        Configuration conf = getConf();
        
        // 加载关键词
        String keywordsList = loadKeywords(keywordsFile);
        conf.set("keywords.list", keywordsList);
        
        System.out.println("[加载关键词]");
        String[] keywords = keywordsList.split(",");
        System.out.println("  共加载 " + keywords.length + " 个关键词");
        System.out.println("  关键词列表: " + Arrays.toString(Arrays.copyOf(keywords, Math.min(10, keywords.length))) + 
                          (keywords.length > 10 ? " ..." : ""));
        System.out.println();
        
        // 创建作业
        Job job = Job.getInstance(conf, "E-BookStore Keyword Count");
        job.setJarByClass(KeywordCountDriver.class);
        
        // 设置Mapper
        job.setMapperClass(KeywordCountMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);
        
        // 设置Combiner（本地聚合）
        job.setCombinerClass(KeywordCountCombiner.class);
        
        // 设置Reducer
        job.setReducerClass(KeywordCountReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        
        // 设置输入输出格式
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        
        // 设置输入输出路径
        Path inputDir = new Path(inputPath);
        Path outputDir = new Path(outputPath);
        
        FileInputFormat.addInputPath(job, inputDir);
        FileOutputFormat.setOutputPath(job, outputDir);
        
        // 如果输出目录已存在，删除它
        FileSystem fs = FileSystem.get(conf);
        if (fs.exists(outputDir)) {
            System.out.println("[清理] 删除已存在的输出目录: " + outputPath);
            fs.delete(outputDir, true);
        }
        
        // 显示输入文件列表
        System.out.println("[输入文件]");
        if (fs.exists(inputDir)) {
            org.apache.hadoop.fs.FileStatus[] files = fs.listStatus(inputDir);
            for (org.apache.hadoop.fs.FileStatus file : files) {
                if (!file.isDirectory() && file.getPath().getName().endsWith(".txt")) {
                    System.out.println("  - " + file.getPath().getName() + " (" + file.getLen() + " bytes)");
                }
            }
        } else {
            System.err.println("  警告: 输入目录不存在: " + inputPath);
        }
        System.out.println();
        
        // 提交作业并等待完成
        System.out.println("[执行MapReduce作业]");
        System.out.println("  作业名称: " + job.getJobName());
        System.out.println("  开始时间: " + new java.util.Date());
        System.out.println();
        
        long startTime = System.currentTimeMillis();
        boolean success = job.waitForCompletion(true);
        long endTime = System.currentTimeMillis();
        
        System.out.println();
        System.out.println("[执行结果]");
        System.out.println("  状态: " + (success ? "成功 ✓" : "失败 ✗"));
        System.out.println("  耗时: " + (endTime - startTime) + " ms");
        System.out.println("  结束时间: " + new java.util.Date());
        
        if (success) {
            // 显示输出结果
            System.out.println();
            System.out.println("[统计结果]");
            displayResults(outputPath);
        }
        
        return success ? 0 : 1;
    }
    
    /**
     * 打印帮助信息
     */
    private void printHelp() {
        System.out.println("使用方法:");
        System.out.println("  hadoop jar bookstore-keyword-count.jar [options] [input] [output] [keywords]");
        System.out.println();
        System.out.println("选项:");
        System.out.println("  -i, --input <path>      输入目录路径 (默认: input)");
        System.out.println("  -o, --output <path>     输出目录路径 (默认: output)");
        System.out.println("  -k, --keywords <file>   关键词列表文件 (默认: 使用内置关键词)");
        System.out.println("  --help                  显示帮助信息");
        System.out.println();
        System.out.println("示例:");
        System.out.println("  hadoop jar bookstore-keyword-count.jar input output");
        System.out.println("  hadoop jar bookstore-keyword-count.jar -i hdfs:///input -o hdfs:///output");
        System.out.println("  hadoop jar bookstore-keyword-count.jar input output keywords.txt");
    }
    
    /**
     * 加载关键词列表
     */
    private String loadKeywords(String keywordsFile) {
        List<String> keywords = new ArrayList<>();
        
        if (keywordsFile != null) {
            // 从文件加载关键词
            try {
                java.nio.file.Path path = java.nio.file.Paths.get(keywordsFile);
                List<String> lines = java.nio.file.Files.readAllLines(path, StandardCharsets.UTF_8);
                for (String line : lines) {
                    line = line.trim();
                    // 跳过空行和注释
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        keywords.add(line);
                    }
                }
                System.out.println("从文件加载关键词: " + keywordsFile);
            } catch (IOException e) {
                System.err.println("无法读取关键词文件: " + keywordsFile);
                System.err.println("将使用默认关键词列表");
                keywords.addAll(Arrays.asList(DEFAULT_KEYWORDS));
            }
        } else {
            // 尝试从classpath加载
            try (InputStream is = getClass().getClassLoader().getResourceAsStream("keywords.txt")) {
                if (is != null) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (!line.isEmpty() && !line.startsWith("#")) {
                            keywords.add(line);
                        }
                    }
                    System.out.println("从classpath加载关键词");
                } else {
                    keywords.addAll(Arrays.asList(DEFAULT_KEYWORDS));
                    System.out.println("使用默认关键词列表");
                }
            } catch (IOException e) {
                keywords.addAll(Arrays.asList(DEFAULT_KEYWORDS));
                System.out.println("使用默认关键词列表");
            }
        }
        
        return String.join(",", keywords);
    }
    
    /**
     * 显示统计结果
     */
    private void displayResults(String outputPath) {
        try {
            java.nio.file.Path resultPath = java.nio.file.Paths.get(outputPath, "part-r-00000");
            if (java.nio.file.Files.exists(resultPath)) {
                System.out.println("  输出文件: " + resultPath);
                System.out.println();
                System.out.println("  ┌─────────────────────┬───────────┐");
                System.out.println("  │      关键词          │  出现次数  │");
                System.out.println("  ├─────────────────────┼───────────┤");
                
                List<String> lines = java.nio.file.Files.readAllLines(resultPath, StandardCharsets.UTF_8);
                
                // 按出现次数排序
                List<String[]> results = new ArrayList<>();
                for (String line : lines) {
                    String[] parts = line.split("\t");
                    if (parts.length == 2) {
                        results.add(parts);
                    }
                }
                results.sort((a, b) -> Integer.parseInt(b[1]) - Integer.parseInt(a[1]));
                
                int totalCount = 0;
                for (String[] parts : results) {
                    String keyword = parts[0];
                    int count = Integer.parseInt(parts[1]);
                    totalCount += count;
                    System.out.printf("  │ %-19s │ %9d │%n", keyword, count);
                }
                
                System.out.println("  └─────────────────────┴───────────┘");
                System.out.println();
                System.out.println("  统计汇总:");
                System.out.println("    - 出现的关键词数: " + results.size());
                System.out.println("    - 关键词总出现次数: " + totalCount);
            }
        } catch (IOException e) {
            System.err.println("  无法读取结果文件: " + e.getMessage());
        }
    }
}

