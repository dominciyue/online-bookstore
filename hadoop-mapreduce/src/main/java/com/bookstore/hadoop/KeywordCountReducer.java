package com.bookstore.hadoop;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/**
 * KeywordCountReducer - MapReduce Reducer类
 * 
 * 功能：汇总来自所有Mapper的关键词计数，计算每个关键词的总出现次数
 * 
 * 输入：
 * - Key: 关键词 (Text)
 * - Values: 来自各Mapper的出现次数列表 (Iterable<IntWritable>)
 * 
 * 输出：
 * - Key: 关键词 (Text)
 * - Value: 总出现次数 (IntWritable)
 */
public class KeywordCountReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
    
    // 输出的Value
    private IntWritable result = new IntWritable();
    
    /**
     * setup方法 - 在Reducer任务开始前执行
     */
    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);
        System.out.println("Reducer初始化完成");
    }
    
    /**
     * reduce方法 - 处理每个关键词的所有计数值
     */
    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Context context)
            throws IOException, InterruptedException {
        
        int sum = 0;
        
        // 累加所有计数值
        for (IntWritable value : values) {
            sum += value.get();
        }
        
        // 只输出出现次数大于0的关键词
        if (sum > 0) {
            result.set(sum);
            context.write(key, result);
            
            System.out.println("关键词 [" + key.toString() + "] 出现次数: " + sum);
        }
    }
    
    /**
     * cleanup方法 - 在Reducer任务结束后执行
     */
    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        super.cleanup(context);
        System.out.println("Reducer任务完成");
    }
}

