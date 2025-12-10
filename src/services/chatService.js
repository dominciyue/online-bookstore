/**
 * 聊天机器人服务
 * 与n8n工作流通信
 */

// n8n webhook URL
const N8N_WEBHOOK_URL = 'http://localhost:5678/webhook/chat';

// 备用：直接调用MCP HTTP服务
const MCP_HTTP_URL = 'http://localhost:3001';

/**
 * 发送消息到n8n聊天机器人
 * @param {string} message - 用户消息
 * @returns {Promise<{reply: string, success: boolean}>}
 */
export const sendMessage = async (message) => {
    try {
        const response = await fetch(N8N_WEBHOOK_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ message }),
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        return {
            reply: data.reply || '抱歉，我没有理解您的问题。',
            success: true,
        };
    } catch (error) {
        console.error('Chat service error:', error);
        
        // 如果n8n不可用，尝试使用备用服务
        try {
            return await sendMessageFallback(message);
        } catch (fallbackError) {
            return {
                reply: '抱歉，聊天服务暂时不可用。请确保n8n服务已启动。',
                success: false,
            };
        }
    }
};

/**
 * 备用方案：直接调用MCP服务进行简单查询
 * @param {string} message - 用户消息
 */
const sendMessageFallback = async (message) => {
    // 简单的关键词匹配
    const lowerMessage = message.toLowerCase();
    
    let result;
    
    if (lowerMessage.includes('分类') || lowerMessage.includes('类别')) {
        // 获取所有分类
        const response = await fetch(`${MCP_HTTP_URL}/api/get_all_categories`);
        const data = await response.json();
        if (data.success) {
            result = `我们书店有以下分类：\n${data.data.join('、')}`;
        }
    } else if (lowerMessage.includes('搜索') || lowerMessage.includes('找') || lowerMessage.includes('查')) {
        // 提取搜索关键词
        const keywords = message.replace(/搜索|找|查|书籍|书|有|吗|？|\?/g, '').trim();
        if (keywords) {
            const response = await fetch(`${MCP_HTTP_URL}/api/search_books`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ query: keywords, limit: 5 }),
            });
            const data = await response.json();
            if (data.success && data.data.length > 0) {
                result = `为您找到以下书籍：\n${data.data.map(book => 
                    `📚 《${book.title}》- ${book.author} - ¥${book.price}`
                ).join('\n')}`;
            } else {
                result = `抱歉，没有找到与"${keywords}"相关的书籍。`;
            }
        }
    } else {
        // 默认搜索
        const response = await fetch(`${MCP_HTTP_URL}/api/search_books`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ query: message, limit: 5 }),
        });
        const data = await response.json();
        if (data.success && data.data.length > 0) {
            result = `为您找到以下相关书籍：\n${data.data.map(book => 
                `📚 《${book.title}》- ${book.author} - ¥${book.price}`
            ).join('\n')}`;
        } else {
            result = '您好！我是在线书店的智能助手。您可以问我：\n- 有什么书籍推荐？\n- 搜索某本书\n- 查看书籍分类\n- 按价格筛选书籍';
        }
    }
    
    return {
        reply: result || '您好！有什么可以帮助您的？',
        success: true,
    };
};

/**
 * 直接调用MCP工具
 * @param {string} toolName - 工具名称
 * @param {object} params - 参数
 */
export const callMcpTool = async (toolName, params = {}) => {
    try {
        const response = await fetch(`${MCP_HTTP_URL}/api/call_tool`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                tool_name: toolName,
                parameters: params,
            }),
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        return await response.json();
    } catch (error) {
        console.error('MCP tool call error:', error);
        throw error;
    }
};

/**
 * 检查聊天服务是否可用
 */
export const checkServiceHealth = async () => {
    try {
        // 检查MCP HTTP服务
        const mcpResponse = await fetch(`${MCP_HTTP_URL}/health`, {
            method: 'GET',
        });
        const mcpOk = mcpResponse.ok;

        // 简单检查n8n是否响应（可能需要认证）
        let n8nOk = false;
        try {
            const n8nResponse = await fetch('http://localhost:5678/healthz', {
                method: 'GET',
            });
            n8nOk = n8nResponse.ok;
        } catch {
            // n8n可能没有healthz端点
            n8nOk = false;
        }

        return {
            mcp: mcpOk,
            n8n: n8nOk,
            overall: mcpOk, // 只要MCP可用，备用方案就可以工作
        };
    } catch (error) {
        return {
            mcp: false,
            n8n: false,
            overall: false,
        };
    }
};

export default {
    sendMessage,
    callMcpTool,
    checkServiceHealth,
};


