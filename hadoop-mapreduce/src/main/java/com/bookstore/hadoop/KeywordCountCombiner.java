package com.bookstore.hadoop;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/**
 * KeywordCountCombiner - MapReduce Combiner类
 * 
 * 功能：在Mapper端进行本地聚合，减少网络传输量
 * Combiner的逻辑与Reducer相同，但在Mapper端执行
 * 
 * 输入：
 * - Key: 关键词 (Text)
 * - Values: 本地Mapper的出现次数列表 (Iterable<IntWritable>)
 * 
 * 输出：
 * - Key: 关键词 (Text)
 * - Value: 本地汇总的出现次数 (IntWritable)
 */
public class KeywordCountCombiner extends Reducer<Text, IntWritable, Text, IntWritable> {
    
    // 输出的Value
    private IntWritable result = new IntWritable();
    
    /**
     * reduce方法 - 在Mapper端进行本地聚合
     */
    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Context context)
            throws IOException, InterruptedException {
        
        int sum = 0;
        
        // 累加本地的所有计数值
        for (IntWritable value : values) {
            sum += value.get();
        }
        
        result.set(sum);
        context.write(key, result);
    }
}

