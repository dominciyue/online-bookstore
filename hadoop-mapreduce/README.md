# E-BookStore 图书简介关键词统计 - Hadoop MapReduce

## 项目概述

本项目使用 **Hadoop MapReduce 框架**实现对 E-BookStore 网上书店系统中图书简介的关键词统计功能。

### 作业完成情况

| 作业要求 | 完成状态 | 实现方式 |
|----------|----------|----------|
| i. 按图书类型分别存储简介到多个文件 | ✅ 已完成 | `export-data.ps1` 从数据库导出 |
| ii. 编写关键词列表 | ✅ 已完成 | `keywords.txt` 包含98个关键词 |
| iii. 编写MR程序统计关键词次数 | ✅ 已完成 | `Mapper` + `Reducer` + `Driver` |

### 功能特性

1. **数据导出**：从 MySQL 数据库中读取图书信息，按分类将图书简介导出到多个文本文件
2. **关键词配置**：支持自定义中英文混合关键词列表
3. **MapReduce统计**：使用Hadoop MR框架统计所有图书简介中每个关键词的出现次数

## 项目结构

```
hadoop-mapreduce/
├── pom.xml                          # Maven 配置文件（Hadoop 3.3.6）
├── README.md                        # 本文档
├── run-hadoop.bat                   # ⭐ Hadoop MapReduce 运行脚本
├── run-simple.bat                   # 简化版运行脚本（无需Hadoop）
├── run-export-data.bat              # 数据导出脚本
├── export-data.ps1                  # PowerShell 数据导出
├── run-simple-count.ps1             # PowerShell 简化版统计
├── input/                           # 输入数据目录（按类型分类的图书简介）
│   ├── CS_Classic.txt               # 计算机经典类
│   ├── Web_Dev.txt                  # Web开发类
│   ├── Programming.txt              # 编程语言类
│   └── SciFi.txt                    # 科幻小说类
├── output/                          # 输出结果目录
│   └── part-r-00000                 # MapReduce 输出结果
└── src/main/java/com/bookstore/hadoop/
    ├── KeywordCountMapper.java      # ⭐ Mapper 类
    ├── KeywordCountReducer.java     # ⭐ Reducer 类
    ├── KeywordCountCombiner.java    # ⭐ Combiner 类
    ├── KeywordCountDriver.java      # ⭐ Driver 驱动类
    └── BookDataExporter.java        # 数据导出工具
```

---

## Hadoop 安装与配置（Windows）

### 安装步骤

#### 1. 下载并安装 Hadoop 3.3.6

```powershell
# 创建安装目录
New-Item -ItemType Directory -Path "C:\hadoop" -Force

# 下载 Hadoop 3.3.6
$hadoopVersion = "3.3.6"
$downloadUrl = "https://dlcdn.apache.org/hadoop/common/hadoop-$hadoopVersion/hadoop-$hadoopVersion.tar.gz"
Invoke-WebRequest -Uri $downloadUrl -OutFile "C:\hadoop\hadoop-$hadoopVersion.tar.gz"

# 解压
cd C:\hadoop
tar -xzf hadoop-3.3.6.tar.gz
```

#### 2. 下载 Windows 必需文件

Hadoop 需要 Windows 原生工具：

```powershell
# 下载 winutils.exe
$winutilsUrl = "https://github.com/cdarlint/winutils/raw/master/hadoop-3.3.6/bin/winutils.exe"
Invoke-WebRequest -Uri $winutilsUrl -OutFile "C:\hadoop\hadoop-3.3.6\bin\winutils.exe"

# 下载 hadoop.dll
$hadoopDllUrl = "https://github.com/cdarlint/winutils/raw/master/hadoop-3.3.6/bin/hadoop.dll"
Invoke-WebRequest -Uri $hadoopDllUrl -OutFile "C:\hadoop\hadoop-3.3.6\bin\hadoop.dll"
```

#### 3. 配置环境变量

**修改 hadoop-env.cmd：**

编辑 `C:\hadoop\hadoop-3.3.6\etc\hadoop\hadoop-env.cmd`：

```batch
@rem 将 JAVA_HOME 设置为短路径格式（避免空格问题）
set JAVA_HOME=C:\PROGRA~1\ECLIPS~1\JDK-21~1.9-H
```

**设置系统环境变量：**

```powershell
# 设置 HADOOP_HOME
[System.Environment]::SetEnvironmentVariable("HADOOP_HOME", "C:\hadoop\hadoop-3.3.6", [System.EnvironmentVariableTarget]::User)

# 添加到 PATH
$currentPath = [System.Environment]::GetEnvironmentVariable("PATH", [System.EnvironmentVariableTarget]::User)
$newPath = "C:\hadoop\hadoop-3.3.6\bin;C:\hadoop\hadoop-3.3.6\sbin;$currentPath"
[System.Environment]::SetEnvironmentVariable("PATH", $newPath, [System.EnvironmentVariableTarget]::User)
```

**重启 PowerShell 或设置当前会话：**

```powershell
$env:HADOOP_HOME = "C:\hadoop\hadoop-3.3.6"
$env:PATH = "$env:HADOOP_HOME\bin;$env:HADOOP_HOME\sbin;$env:PATH"
```

#### 4. 验证安装

```powershell
hadoop version
```

应显示：
```
Hadoop 3.3.6
Source code repository https://github.com/apache/hadoop.git
...
```

### 安装过程中遇到的问题与解决

#### 问题 1：解压时出现 .so 文件错误

**错误信息**：
```
Can't create '\\?\C:\hadoop\hadoop-3.3.6\lib\native\libhdfs.so': Invalid argument
```

**原因**：`.so` 文件是 Linux 库文件，Windows 不需要。

**解决**：忽略这些错误，解压会继续完成。

#### 问题 2：JAVA_HOME 包含空格导致错误

**错误信息**：
```
Error: JAVA_HOME is incorrectly set.
```

**原因**：Java 安装路径包含空格（如 `C:\Program Files\...`）。

**解决**：在 `hadoop-env.cmd` 中使用短路径格式：
```batch
set JAVA_HOME=C:\PROGRA~1\ECLIPS~1\JDK-21~1.9-H
```

获取短路径：
```powershell
(New-Object -ComObject Scripting.FileSystemObject).GetFolder("C:\Program Files\Eclipse Adoptium\jdk-21.0.8.9-hotspot").ShortPath
```

#### 问题 3：META-INF/license 文件创建失败

**错误信息**：
```
IOException: Mkdirs failed to create C:\Users\...\META-INF\license
```

**原因**：Windows 保留字问题，`license` 是系统保留名。

**解决**：在 `pom.xml` 的 maven-shade-plugin 中排除 LICENSE 相关文件：

```xml
<filters>
    <filter>
        <artifact>*:*</artifact>
        <excludes>
            <exclude>META-INF/*.SF</exclude>
            <exclude>META-INF/*.DSA</exclude>
            <exclude>META-INF/*.RSA</exclude>
            <exclude>META-INF/license/*</exclude>
            <exclude>META-INF/LICENSE</exclude>
            <exclude>META-INF/LICENSE.txt</exclude>
            <exclude>META-INF/NOTICE</exclude>
        </excludes>
    </filter>
</filters>
```

#### 问题 4：参数解析错误

**错误信息**：
```
Input path does not exist: file:/E:/web/hadoop-mapreduce/com.bookstore.hadoop.KeywordCountDriver
```

**原因**：命令行参数解析问题，Driver 类名被当作输入路径。

**解决**：使用显式参数标志：
```powershell
hadoop jar target\bookstore-keyword-count-1.0-SNAPSHOT.jar `
    com.bookstore.hadoop.KeywordCountDriver `
    -i file:///E:/web/hadoop-mapreduce/input `
    -o file:///E:/web/hadoop-mapreduce/output `
    -k E:\web\hadoop-mapreduce\src\main\resources\keywords.txt
```

---

## 运行方式

### 方式一：使用 Hadoop 运行（推荐用于生产环境）

**前提条件**：已安装 Hadoop 3.x 并配置环境变量（参见上方安装步骤）

```powershell
cd E:\web\hadoop-mapreduce

# 1. 导出数据库数据
.\run-export-data.bat

# 2. 运行 Hadoop MapReduce
.\run-hadoop.bat
```

**Hadoop 命令行方式**：
```bash
# 打包
mvn package -DskipTests

# 运行 MapReduce 作业
hadoop jar target/bookstore-keyword-count-1.0-SNAPSHOT.jar \
    com.bookstore.hadoop.KeywordCountDriver \
    input output src/main/resources/keywords.txt
```

### 方式二：简化版运行（无需安装 Hadoop）

```powershell
cd E:\web\hadoop-mapreduce

# 1. 导出数据
.\run-export-data.bat

# 2. 运行统计
.\run-simple.bat
```

---

## MapReduce 实现详解

### 1. Mapper (KeywordCountMapper.java)

继承 `Mapper<LongWritable, Text, Text, IntWritable>`

```java
public class KeywordCountMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
    
    @Override
    protected void map(LongWritable key, Text value, Context context) {
        String line = value.toString();
        
        // 对每个关键词进行统计
        for (String keyword : keywords) {
            int count = countKeywordOccurrences(line, keyword);
            if (count > 0) {
                context.write(new Text(keyword), new IntWritable(count));
            }
        }
    }
}
```

**核心功能**：
- 读取每行文本
- 遍历关键词列表，统计每个关键词出现次数
- 输出 `<关键词, 出现次数>` 键值对

### 2. Combiner (KeywordCountCombiner.java)

继承 `Reducer<Text, IntWritable, Text, IntWritable>`

```java
public class KeywordCountCombiner extends Reducer<Text, IntWritable, Text, IntWritable> {
    
    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Context context) {
        int sum = 0;
        for (IntWritable value : values) {
            sum += value.get();
        }
        context.write(key, new IntWritable(sum));
    }
}
```

**核心功能**：
- 在 Mapper 端进行本地聚合
- 减少网络传输量，提高效率

### 3. Reducer (KeywordCountReducer.java)

继承 `Reducer<Text, IntWritable, Text, IntWritable>`

```java
public class KeywordCountReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
    
    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Context context) {
        int sum = 0;
        
        // 累加所有计数值
        for (IntWritable value : values) {
            sum += value.get();
        }
        
        context.write(key, new IntWritable(sum));
    }
}
```

**核心功能**：
- 汇总来自所有 Mapper 的关键词计数
- 输出每个关键词的总出现次数

### 4. Driver (KeywordCountDriver.java)

继承 `Configured implements Tool`

```java
public class KeywordCountDriver extends Configured implements Tool {
    
    @Override
    public int run(String[] args) throws Exception {
        Configuration conf = getConf();
        
        // 创建 Job
        Job job = Job.getInstance(conf, "E-BookStore Keyword Count");
        job.setJarByClass(KeywordCountDriver.class);
        
        // 设置 Mapper、Combiner、Reducer
        job.setMapperClass(KeywordCountMapper.class);
        job.setCombinerClass(KeywordCountCombiner.class);
        job.setReducerClass(KeywordCountReducer.class);
        
        // 设置输出类型
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        
        // 设置输入输出路径
        FileInputFormat.addInputPath(job, new Path(inputPath));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));
        
        return job.waitForCompletion(true) ? 0 : 1;
    }
}
```

---

## MapReduce 数据流

```
┌────────────────────────────────────────────────────────────────────────────┐
│                           MapReduce 作业流程                                │
├────────────────────────────────────────────────────────────────────────────┤
│                                                                            │
│  ┌─────────────┐                                                           │
│  │ Input Files │  CS_Classic.txt, Web_Dev.txt, Programming.txt, SciFi.txt  │
│  └──────┬──────┘                                                           │
│         │                                                                  │
│         ▼                                                                  │
│  ┌─────────────────────────────────────────────────────────────────────┐  │
│  │                         MAPPER PHASE                                 │  │
│  │  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐                 │  │
│  │  │Mapper 1 │  │Mapper 2 │  │Mapper 3 │  │Mapper 4 │  (并行处理)      │  │
│  │  │CS_Classic│ │Web_Dev  │  │Program  │  │SciFi    │                 │  │
│  │  └────┬────┘  └────┬────┘  └────┬────┘  └────┬────┘                 │  │
│  │       │            │            │            │                       │  │
│  │       ▼            ▼            ▼            ▼                       │  │
│  │  <经典,2>     <前端,1>     <编程,1>     <三体,1>                      │  │
│  │  <算法,1>     <JavaScript,1> <Python,1>  <科幻,1>                     │  │
│  │  <系统,1>     <开发,1>     <入门,1>     <黑暗森林,1>                   │  │
│  └─────────────────────────────────────────────────────────────────────┘  │
│         │                                                                  │
│         ▼                                                                  │
│  ┌─────────────────────────────────────────────────────────────────────┐  │
│  │                        COMBINER PHASE                                │  │
│  │                     (本地聚合，减少网络传输)                           │  │
│  └─────────────────────────────────────────────────────────────────────┘  │
│         │                                                                  │
│         ▼                                                                  │
│  ┌─────────────────────────────────────────────────────────────────────┐  │
│  │                     SHUFFLE & SORT PHASE                             │  │
│  │                   (按 Key 分组并排序)                                  │  │
│  │  经典 → [2,1,1]    三体 → [1,1]    算法 → [1,1]                       │  │
│  └─────────────────────────────────────────────────────────────────────┘  │
│         │                                                                  │
│         ▼                                                                  │
│  ┌─────────────────────────────────────────────────────────────────────┐  │
│  │                         REDUCER PHASE                                │  │
│  │                        (汇总计算)                                     │  │
│  │  经典: 2+1+1 = 4    三体: 1+1 = 2    算法: 1+1 = 2                    │  │
│  └─────────────────────────────────────────────────────────────────────┘  │
│         │                                                                  │
│         ▼                                                                  │
│  ┌─────────────┐                                                           │
│  │ Output File │  output/part-r-00000                                      │
│  │  经典  4    │                                                           │
│  │  算法  2    │                                                           │
│  │  三体  2    │                                                           │
│  └─────────────┘                                                           │
│                                                                            │
└────────────────────────────────────────────────────────────────────────────┘
```

---

## 输入输出示例

### 输入文件 (input/CS_Classic.txt)

```
# Category: CS_Classic
# Total Books: 2
# ==========================================

--- Book ID: 2 ---
Title: 深入理解计算机系统 (第3版)
Author: Randal E. Bryant
Description: 计算机系统领域经典之作，来深入剖析计算机系统底层原理。

--- Book ID: 3 ---
Title: 算法导论 (原书第3版)
Author: Thomas H. Cormen
Description: 算法领域圣经，全面覆盖经典算法和数据结构。
```

### 关键词列表 (keywords.txt)

```
# 计算机相关
计算机
系统
算法
数据结构
经典

# 科幻相关
三体
科幻
黑暗森林
```

### 输出结果 (output/part-r-00000)

```
经典      4
系统      2
领域      2
计算机    2
算法      2
三体      1
黑暗森林  1
科幻      1
...
```

---

## 开发过程中遇到的困难与解决方法

### 困难 1：图书简介是中文，英文关键词无法匹配

**问题**：最初设计的关键词全是英文（Java, Star, Robot），但数据库中图书简介是中文。

**解决**：查询数据库后重新设计中文关键词列表。

### 困难 2：Maven 未全局安装

**问题**：运行脚本报错 "Maven未安装"。

**解决**：使用后端项目的 Maven Wrapper，并创建不依赖 Maven 的 PowerShell 脚本。

### 困难 3：Java 类型冲突

**问题**：`java.nio.file.Path` 与 `org.apache.hadoop.fs.Path` 冲突。

**解决**：使用完全限定类名 `java.nio.file.Paths.get()` 避免冲突。

### 困难 4：中文关键词匹配逻辑

**问题**：英文使用 `\bword\b` 边界匹配，中文无此概念。

**解决**：判断关键词类型，中文用 `indexOf` 子串匹配，英文用正则表达式。

### 困难 5：PowerShell 脚本编码问题

**问题**：中文脚本运行报错。

**解决**：将脚本改为英文，避免编码问题。

### 困难 6：数据库导出中文乱码

**问题**：MySQL 导出中文变成乱码。

**解决**：使用 UTF-8 BOM 编码，设置 `--default-character-set=utf8mb4`。

---

## 环境要求

### Hadoop 运行模式
- Java JDK 17+
- Maven 3.6+
- Hadoop 3.x

### 简化运行模式
- PowerShell 5.0+
- MySQL 8.0+ (用于数据导出)

---

## 实际运行结果

### 运行命令

```powershell
cd E:\web\hadoop-mapreduce

# 设置环境变量
$env:HADOOP_HOME = "C:\hadoop\hadoop-3.3.6"
$env:PATH = "$env:HADOOP_HOME\bin;$env:PATH"

# 导出数据
.\run-export-data.bat

# 运行 Hadoop MapReduce
hadoop jar target\bookstore-keyword-count-1.0-SNAPSHOT.jar `
    com.bookstore.hadoop.KeywordCountDriver `
    -i file:///E:/web/hadoop-mapreduce/input `
    -o file:///E:/web/hadoop-mapreduce/output `
    -k E:\web\hadoop-mapreduce\src\main\resources\keywords.txt
```

### 运行日志摘要

```
[配置信息]
  输入目录: file:///E:/web/hadoop-mapreduce/input
  输出目录: file:///E:/web/hadoop-mapreduce/output
  关键词文件: E:\web\hadoop-mapreduce\src\main\resources\keywords.txt

[加载关键词]
  共加载 98 个关键词

[输入文件]
  - CS_Classic.txt (469 bytes)
  - Programming.txt (277 bytes)
  - SciFi.txt (332 bytes)
  - Web_Dev.txt (279 bytes)

[执行MapReduce作业]
  作业名称: E-BookStore Keyword Count
  
Mapper初始化完成，加载了 92 个关键词
Processing split: file:/E:/web/hadoop-mapreduce/input/CS_Classic.txt:0+469
Processing split: file:/E:/web/hadoop-mapreduce/input/SciFi.txt:0+332
Processing split: file:/E:/web/hadoop-mapreduce/input/Web_Dev.txt:0+279
Processing split: file:/E:/web/hadoop-mapreduce/input/Programming.txt:0+277

Reducer初始化完成
关键词 [JavaScript] 出现次数: 1
关键词 [Python] 出现次数: 2
关键词 [经典] 出现次数: 2
关键词 [算法] 出现次数: 2
关键词 [三体] 出现次数: 1
关键词 [科幻] 出现次数: 1
... (共40个关键词)

[执行结果]
  状态: 成功 ✓
  耗时: 1717 ms

Job Counters:
  Map-Reduce Framework
    Map input records=52
    Map output records=46
    Combine input records=46
    Combine output records=40
    Reduce input records=40
    Reduce output records=40
  File Input Format Counters 
    Bytes Read=1357
  File Output Format Counters 
    Bytes Written=406
```

### 输出结果 (output/part-r-00000)

```
JavaScript	1
Python	2
三体	1
三部曲	1
入门	1
全面	1
前端	1
剖析	1
包含	1
原理	1
圣经	1
实践	1
底层	1
开发	1
引导	1
必备	1
掌握	1
数据	1
数据结构	1
核心	1
死神永生	1
深入	1
畅销	1
科幻	1
算法	2
系统	2
红宝书	1
经典	2
结构	1
编程	1
覆盖	1
计算机	2
讲解	1
详细	1
语言	1
里程碑	1
项目	1
领域	2
高级	1
黑暗森林	1
```

**统计汇总**：
- 处理文件数：4
- 输入行数：52
- 匹配关键词数：40
- 关键词总出现次数：46

**高频关键词**（出现2次以上）：
- 经典、计算机、领域：各2次
- 系统、算法：各2次
- Python：2次

---

## 数据库配置

```properties
数据库: bookstore_db
用户名: root
密码: Zy050811
```

---

## 技术总结

### 成功要点

1. ✅ **正确使用 Hadoop MapReduce 框架**：实现了标准的 Mapper、Reducer、Combiner、Driver 四个组件
2. ✅ **并行处理**：4个输入文件被分配到4个 Mapper 并行处理
3. ✅ **本地聚合**：Combiner 将46条 Map 输出聚合为40条，减少网络传输
4. ✅ **中英文混合处理**：智能识别中文和英文关键词，使用不同匹配策略
5. ✅ **完整的数据流**：数据库导出 → 分类存储 → MapReduce统计 → 结果输出

### MapReduce 执行流程

```
输入文件 (4个) → Split (4个分片)
    ↓
[Map Phase] 4个 Mapper 并行执行
    - CS_Classic.txt → Mapper 1 → 20条输出
    - SciFi.txt → Mapper 2 → 7条输出
    - Web_Dev.txt → Mapper 3 → 10条输出
    - Programming.txt → Mapper 4 → 9条输出
    ↓
[Combine Phase] 本地聚合
    46条 → 40条 (减少6条重复)
    ↓
[Shuffle & Sort Phase] 按Key分组排序
    ↓
[Reduce Phase] 1个 Reducer 汇总
    40条输入 → 40条输出 (每个关键词一行)
    ↓
输出文件 part-r-00000
```

## 许可证

本项目为 E-BookStore 网上书店系统的一部分，仅供学习和教育目的使用。
