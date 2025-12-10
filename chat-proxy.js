/**
 * 聊天代理服务
 * 解决 n8n webhook 的 CORS 问题
 */

const express = require('express');
const cors = require('cors');

const app = express();
const PORT = 3002;

// n8n webhook URL
const N8N_WEBHOOK_URL = 'http://localhost:5678/webhook/chat';

// 启用 CORS
app.use(cors({
    origin: 'http://localhost:3000',
    credentials: true
}));

app.use(express.json());

// 代理聊天请求到 n8n
app.post('/chat', async (req, res) => {
    try {
        console.log('收到聊天请求:', req.body);
        
        const response = await fetch(N8N_WEBHOOK_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(req.body),
        });

        console.log('n8n 响应状态:', response.status);
        
        // 获取响应文本
        const text = await response.text();
        console.log('n8n 响应内容:', text);
        
        // 尝试解析 JSON
        if (!text || text.trim() === '') {
            console.log('n8n 返回空响应，使用默认回复');
            return res.json({
                reply: '抱歉，我暂时无法处理您的请求，请稍后再试。',
                success: false
            });
        }
        
        try {
            const data = JSON.parse(text);
            console.log('n8n 解析结果:', data);
            res.json(data);
        } catch (parseError) {
            console.error('JSON 解析失败:', parseError.message);
            // 如果响应不是 JSON，可能是纯文本回复
            res.json({
                reply: text,
                success: true
            });
        }
    } catch (error) {
        console.error('代理错误:', error.message);
        res.status(500).json({
            reply: '抱歉，聊天服务暂时不可用。请确保 n8n 服务正在运行。',
            success: false,
            error: error.message
        });
    }
});

// 健康检查
app.get('/health', (req, res) => {
    res.json({ status: 'ok', service: 'chat-proxy' });
});

app.listen(PORT, () => {
    console.log(`✓ 聊天代理服务运行在 http://localhost:${PORT}`);
    console.log(`  - 聊天接口: POST http://localhost:${PORT}/chat`);
});
