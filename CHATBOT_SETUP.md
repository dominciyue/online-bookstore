# E-BookStore 聊天机器人配置指南

## 系统架构

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   React前端     │────▶│    n8n Agent    │────▶│ DeepSeek API    │
│   (ChatBot)     │◀────│   (工作流)       │◀────│   (大模型)       │
└─────────────────┘     └────────┬────────┘     └─────────────────┘
                                 │
                                 ▼
                        ┌─────────────────┐
                        │  MCP HTTP服务   │
                        │  (书籍工具API)   │
                        └─────────────────┘
                                 │
                                 ▼
                        ┌─────────────────┐
                        │    MySQL DB     │
                        │  (bookstore_db) │
                        └─────────────────┘
```

## 快速启动（一键启动）

在PowerShell中依次运行以下命令：

```powershell
# 1. 启动MCP HTTP服务（新终端）
cd E:\bookstore-mcp-server; npm run http

# 2. 启动n8n（新终端）
npx n8n start

# 3. 启动React前端（新终端）
cd E:\web; npm start
```

## 详细启动步骤

### 1. 启动MCP HTTP服务 (端口 3001)

```powershell
cd E:\bookstore-mcp-server
npm run http
```

验证服务启动：
- 访问 http://localhost:3001/health 应返回 `{"status":"ok"}`
- 访问 http://localhost:3001/tools 查看可用工具列表

### 2. 启动n8n (端口 5678)

```powershell
npx n8n start
```

或者禁用认证启动：
```powershell
$env:N8N_BASIC_AUTH_ACTIVE="false"
npx n8n start
```

### 3. 配置n8n工作流

#### 3.1 首次使用n8n

1. 打开浏览器访问 http://localhost:5678
2. 首次使用需要创建账号（邮箱/密码）
3. 登录后进入工作台

#### 3.2 创建DeepSeek API凭据

1. 点击左下角 **Settings** (齿轮图标)
2. 选择 **Credentials**
3. 点击 **Add Credential**
4. 搜索并选择 **Header Auth**
5. 填写配置:
   - **Credential Name**: `DeepSeek API`
   - **Name**: `Authorization`
   - **Value**: `Bearer sk-9b1cffdbc9394018b05947f6ad716dc2`
6. 点击 **Save** 保存

#### 3.3 创建聊天工作流（手动步骤）

**步骤1: 创建新工作流**
- 点击 **New Workflow**
- 命名为 "BookStore Chatbot"

**步骤2: 添加Webhook触发器**
1. 点击 + 添加第一个节点
2. 搜索 **Webhook**
3. 配置:
   - HTTP Method: `POST`
   - Path: `chat`
   - Response: 选择 `Using 'Respond to Webhook' node`
4. 保存节点

**步骤3: 添加HTTP Request节点（调用DeepSeek）**
1. 点击 + 添加节点
2. 搜索 **HTTP Request**
3. 配置:
   - Method: `POST`
   - URL: `https://api.deepseek.com/chat/completions`
   - Authentication: `Generic Credential Type` → `Header Auth` → 选择 `DeepSeek API`
   - Body Content Type: `JSON`
   - Body 设置为 "Define below" 并输入:

```json
{
  "model": "deepseek-chat",
  "messages": [
    {
      "role": "system",
      "content": "你是在线书店E-BookStore的智能助手。你可以帮助用户搜索书籍、查询书籍信息、了解书籍分类和价格。请用友好的中文回答用户问题。"
    },
    {
      "role": "user",
      "content": "{{ $json.body.message }}"
    }
  ],
  "temperature": 0.7,
  "max_tokens": 500
}
```

4. 连接到 Webhook 节点

**步骤4: 添加Code节点（解析响应）**
1. 添加 **Code** 节点
2. 语言选择 **JavaScript**
3. 输入代码:

```javascript
const response = $input.first().json;
let reply = '抱歉，我暂时无法回答您的问题。';

try {
  if (response.choices && response.choices[0]) {
    reply = response.choices[0].message.content;
  }
} catch (e) {
  console.log('Error:', e);
}

return {
  json: {
    reply: reply,
    success: true
  }
};
```

4. 连接到 HTTP Request 节点

**步骤5: 添加Respond to Webhook节点**
1. 添加 **Respond to Webhook** 节点
2. 配置:
   - Respond With: `JSON`
   - Response Body: `{{ JSON.stringify($json) }}`
   - 在 Options 中添加 Response Headers:
     - Name: `Access-Control-Allow-Origin`
     - Value: `*`
3. 连接到 Code 节点

**步骤6: 激活工作流**
1. 检查所有节点已正确连接：Webhook → HTTP Request → Code → Respond to Webhook
2. 点击右上角的开关，激活工作流
3. 复制Production URL（通常是 `http://localhost:5678/webhook/chat`）

### 4. 启动React前端 (端口 3000)

```powershell
cd E:\web
npm start
```

## 测试聊天机器人

### 方法1: 在网页上测试

1. 打开 http://localhost:3000
2. 点击右下角的蓝色聊天图标
3. 输入问题，如 "有什么书籍推荐？"

### 方法2: 使用PowerShell测试n8n工作流

```powershell
# 测试n8n Webhook
Invoke-RestMethod -Uri "http://localhost:5678/webhook/chat" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"message": "你好，有什么书籍推荐？"}'
```

### 方法3: 测试MCP HTTP服务

```powershell
# 搜索书籍
Invoke-RestMethod -Uri "http://localhost:3001/api/search_books" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"query": "Python", "limit": 5}'

# 获取所有分类
Invoke-RestMethod -Uri "http://localhost:3001/api/get_all_categories"
```

## 备用方案（不使用n8n）

如果n8n配置遇到问题，前端ChatBot有内置备用方案：
- 会自动尝试直接调用MCP HTTP服务
- 支持基本的书籍搜索和分类查询
- 只需确保MCP HTTP服务运行即可

## 文件说明

| 文件 | 说明 |
|------|------|
| `E:\bookstore-mcp-server\http-server.js` | MCP HTTP代理服务 |
| `E:\web\src\components\ChatBot.js` | 聊天机器人前端组件 |
| `E:\web\src\services\chatService.js` | 聊天服务API |
| `E:\web\n8n-simple-workflow.json` | 简化版n8n工作流配置 |
| `E:\web\n8n-workflow.json` | 完整版n8n工作流配置（含工具调用） |

## 常见问题

### 1. n8n Webhook 404错误

确保工作流已激活（右上角开关打开）

### 2. CORS错误

MCP HTTP服务已配置CORS。如果n8n返回CORS错误，在Respond to Webhook节点添加响应头：
- `Access-Control-Allow-Origin`: `*`

### 3. n8n认证问题

首次使用需要创建账号。或者设置环境变量禁用认证：
```powershell
$env:N8N_BASIC_AUTH_ACTIVE="false"
npx n8n start
```

### 4. DeepSeek API错误

- 检查API Key是否正确
- 检查凭据配置中Authorization值格式：`Bearer sk-xxx`
- API可能有请求频率限制

### 5. 数据库连接失败

确保MySQL服务运行且配置正确：
- Host: localhost
- Port: 3306
- Database: bookstore_db
- User: root
- Password: Zy050811

## 端口说明

| 服务 | 端口 | 说明 |
|------|------|------|
| React前端 | 3000 | 书店前端界面 |
| Spring Boot后端 | 8080 | 书店API服务 |
| MCP HTTP服务 | 3001 | 书籍工具API |
| n8n | 5678 | 工作流引擎 |

## API Key

DeepSeek API Key: `sk-9b1cffdbc9394018b05947f6ad716dc2`

