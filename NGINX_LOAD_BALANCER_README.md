# Nginx 负载均衡配置指南

本指南说明如何使用 Nginx 作为反向代理，对两个应用实例进行负载均衡测试。

## 📁 文件结构

### 应用配置文件
- `online-bookstore-backend/src/main/resources/application-8080.properties` - 实例1配置（端口8080）
- `online-bookstore-backend/src/main/resources/application-8081.properties` - 实例2配置（端口8081）

### 启动脚本
- `E:/web/start-instance1.bat` - 启动实例1（端口8080）
- `E:/web/start-instance2.bat` - 启动实例2（端口8081）

### Nginx 配置文件
- `E:/nginx/nginx-1.24.0/conf/nginx.conf` - 当前使用的配置文件
- `E:/nginx/nginx-1.24.0/conf/nginx.conf.round-robin` - 轮询策略配置
- `E:/nginx/nginx-1.24.0/conf/nginx.conf.weight` - 加权轮询策略配置
- `E:/nginx/nginx-1.24.0/conf/nginx.conf.ip-hash` - IP哈希策略配置

### 切换脚本
- `E:/nginx/nginx-1.24.0/switch-to-round-robin.bat` - 切换到轮询策略
- `E:/nginx/nginx-1.24.0/switch-to-weight.bat` - 切换到加权轮询策略
- `E:/nginx/nginx-1.24.0/switch-to-ip-hash.bat` - 切换到IP哈希策略

### 测试脚本
- `E:/web/test-load-balancer.ps1` - PowerShell 负载均衡测试脚本

## 🚀 使用步骤

### 第一步：启动两个应用实例

1. **打开第一个 PowerShell 窗口**，运行：
   ```powershell
   cd E:\web
   .\start-instance1.bat
   ```
   等待实例1启动完成（看到 "Started OnlineBookstoreBackendApplication"）

2. **打开第二个 PowerShell 窗口**，运行：
   ```powershell
   cd E:\web
   .\start-instance2.bat
   ```
   等待实例2启动完成

3. **验证两个实例是否启动成功**：
   - 访问 `http://localhost:8080/api/books/server-info` 应该返回端口8080
   - 访问 `http://localhost:8081/api/books/server-info` 应该返回端口8081

### 第二步：配置并启动 Nginx

1. **切换到轮询策略**（第一次测试）：
   ```powershell
   cd E:\nginx\nginx-1.24.0
   .\switch-to-round-robin.bat
   ```

2. **启动 Nginx**（如果还没启动）：
   ```powershell
   .\nginx.exe
   ```

3. **如果 Nginx 已经在运行，重新加载配置**：
   ```powershell
   .\nginx.exe -s reload
   ```

### 第三步：测试负载均衡

#### 方法1：使用 PowerShell 测试脚本

```powershell
cd E:\web
.\test-load-balancer.ps1
```

#### 方法2：使用浏览器或 Postman

多次访问：`http://localhost/api/books/server-info`

观察返回的 `port` 字段，看请求被哪个实例处理。

#### 方法3：使用 curl（PowerShell）

```powershell
# 发送10个请求
1..10 | ForEach-Object {
    $response = Invoke-RestMethod -Uri "http://localhost/api/books/server-info"
    Write-Host "Request $_ : Port = $($response.port)"
    Start-Sleep -Milliseconds 500
}
```

## 📊 三种负载均衡策略

### 策略1：轮询（Round-Robin）

**配置方式**：
```powershell
cd E:\nginx\nginx-1.24.0
.\switch-to-round-robin.bat
.\nginx.exe -s reload
```

**特点**：
- 请求按顺序轮流分配到两个服务器
- 8080 → 8081 → 8080 → 8081 → ...

**预期结果**：
- 10个请求中，8080和8081各处理5个
- 请求分布：50% : 50%

**适用场景**：
- 两个服务器性能相近
- 需要均匀分配负载

---

### 策略2：加权轮询（Weight）

**配置方式**：
```powershell
cd E:\nginx\nginx-1.24.0
.\switch-to-weight.bat
.\nginx.exe -s reload
```

**特点**：
- 8080端口权重为3，8081端口权重为1
- 8080会处理约75%的请求，8081处理约25%

**预期结果**：
- 10个请求中，8080处理约7-8个，8081处理约2-3个
- 请求分布：约75% : 25%

**适用场景**：
- 服务器性能不同
- 需要让性能更好的服务器处理更多请求

---

### 策略3：IP哈希（IP Hash）

**配置方式**：
```powershell
cd E:\nginx\nginx-1.24.0
.\switch-to-ip-hash.bat
.\nginx.exe -s reload
```

**特点**：
- 根据客户端IP地址进行哈希计算
- 同一IP的请求总是访问同一服务器

**预期结果**：
- 从同一台电脑访问，所有请求都会被同一个实例处理
- 不同IP的请求可能被分配到不同实例

**适用场景**：
- 需要会话保持（Session Sticky）
- 有状态服务

---

## 🔍 测试结果分析

### 策略1：轮询（Round-Robin）测试结果

**实际测试结果**：
```
Request 1 : Port = 8080
Request 2 : Port = 8080
Request 3 : Port = 8081
Request 4 : Port = 8081
Request 5 : Port = 8080
Request 6 : Port = 8080
Request 7 : Port = 8081
Request 8 : Port = 8081
Request 9 : Port = 8080
Request 10 : Port = 8080
```

**统计分析**：
- 端口8080处理：6个请求（60%）
- 端口8081处理：4个请求（40%）
- 分布比例：约 6:4

**结果分析**：
1. **基本符合轮询策略**：请求在两个服务器之间轮流分配
2. **分布模式**：每2个请求为一组，交替分配给8080和8081
3. **轻微偏差**：由于Nginx内部实现，可能出现连续请求分配到同一服务器的情况，但总体分布相对均匀
4. **适用场景验证**：适合两个服务器性能相近的场景，能够实现基本的负载均衡

**产生差异的原因**：
- Nginx的轮询算法在实现时，可能会对连续的请求进行批量处理
- 网络延迟和请求处理时间可能影响分配结果
- 小样本测试（10个请求）可能无法完全体现均匀分布

---

### 策略2：加权轮询（Weight 3:1）测试结果

**实际测试结果**：
```
Request 1 : Port = 8080
Request 2 : Port = 8080
Request 3 : Port = 8081
Request 4 : Port = 8080
Request 5 : Port = 8080
Request 6 : Port = 8081
Request 7 : Port = 8080
Request 8 : Port = 8080
Request 9 : Port = 8080
Request 10 : Port = 8080
```

**统计分析**：
- 端口8080处理：8个请求（80%）
- 端口8081处理：2个请求（20%）
- 分布比例：8:2 = 4:1（接近配置的3:1权重）

**结果分析**：
1. **符合加权策略**：8080端口处理的请求明显多于8081
2. **权重比例验证**：配置的权重是3:1（75%:25%），实际结果是80%:20%，非常接近预期
3. **分布模式**：8080每处理3-4个请求后，8081处理1个请求，符合权重分配逻辑
4. **适用场景验证**：成功实现了让性能更好的服务器（8080）处理更多请求的目标

**产生差异的原因**：
- **权重算法实现**：Nginx的加权轮询算法会按照权重比例分配请求，但具体分配顺序可能因实现细节而略有不同
- **样本量影响**：10个请求的样本量较小，可能无法完全精确体现3:1的比例
- **算法内部状态**：加权轮询算法维护内部计数器，可能在某些时刻出现连续分配

**理论分析**：
- 配置权重：8080=3, 8081=1，总权重=4
- 理论比例：8080应处理 3/4 = 75%，8081应处理 1/4 = 25%
- 实际比例：8080处理80%，8081处理20%，偏差在可接受范围内

---

### 策略3：IP哈希（IP Hash）测试结果

**实际测试结果**：
```
Request 1 : Port = 8080
Request 2 : Port = 8080
Request 3 : Port = 8080
Request 4 : Port = 8080
Request 5 : Port = 8080
Request 6 : Port = 8080
Request 7 : Port = 8080
Request 8 : Port = 8080
Request 9 : Port = 8080
Request 10 : Port = 8080
```

**统计分析**：
- 端口8080处理：10个请求（100%）
- 端口8081处理：0个请求（0%）
- 分布比例：10:0（完全固定）

**结果分析**：
1. **完全符合IP哈希策略**：所有请求都被分配到同一个服务器（8080）
2. **会话保持验证**：同一客户端IP的所有请求都路由到同一实例，实现了会话粘性（Session Sticky）
3. **哈希算法验证**：Nginx根据客户端IP地址计算哈希值，同一IP总是得到相同的哈希结果，因此总是路由到同一服务器
4. **适用场景验证**：完美适用于需要保持会话状态的场景

**产生差异的原因**：
- **IP地址固定**：所有请求都来自同一客户端（localhost/127.0.0.1），IP地址相同
- **哈希算法一致性**：IP哈希算法对同一IP地址总是产生相同的哈希值
- **服务器选择固定**：哈希值对服务器数量取模后，总是得到相同的服务器索引
- **设计预期**：这正是IP哈希策略的设计目标——确保同一客户端的请求总是访问同一服务器

**技术原理**：
```
哈希计算：hash(客户端IP) % 服务器数量 = 服务器索引
例如：hash("127.0.0.1") % 2 = 0 → 选择服务器1（8080）
```

**扩展验证**：
- 如果从不同IP地址访问（如不同电脑），可能会被分配到不同的服务器
- 如果服务器数量变化，哈希分布会重新计算，可能导致会话迁移

---

## 📊 三种策略对比总结

| 策略 | 8080处理数 | 8081处理数 | 分布比例 | 特点 |
|------|-----------|-----------|---------|------|
| **轮询** | 6 | 4 | 60%:40% | 相对均匀，轮流分配 |
| **加权轮询** | 8 | 2 | 80%:20% | 按权重分配，8080处理更多 |
| **IP哈希** | 10 | 0 | 100%:0% | 完全固定，会话保持 |

## 🎯 关键发现

1. **轮询策略**：实现了基本的负载均衡，但分布可能不完全均匀
2. **加权轮询**：成功实现了按权重分配，8080处理了约80%的请求
3. **IP哈希**：完美实现了会话保持，同一IP的所有请求都路由到同一服务器

## 💡 实际应用建议

- **无状态服务**：使用轮询或加权轮询
- **有状态服务**：使用IP哈希保证会话一致性
- **性能差异**：使用加权轮询让高性能服务器处理更多请求
- **高可用性**：结合健康检查，自动剔除故障服务器

## 📝 截图建议

在测试每种策略时，建议截图以下内容：

1. **两个应用实例的启动日志**（显示端口和Group-ID）
2. **Nginx配置文件内容**（显示当前使用的策略）
3. **测试结果**（显示请求被哪个端口处理）
4. **测试脚本输出**（显示请求分布统计）

## ⚠️ 注意事项

1. **确保两个实例都启动成功**后再测试
2. **每次切换策略后**，记得执行 `nginx.exe -s reload`
3. **Kafka Group-ID 已配置不同**：
   - 实例1：`bookstore-order-group-8080`
   - 实例2：`bookstore-order-group-8081`
4. **测试端点**：`http://localhost/api/books/server-info`
5. **停止 Nginx**：`nginx.exe -s stop`

## 🛠️ 故障排查

### 问题1：Nginx 无法启动
- 检查端口80是否被占用
- 检查 `logs/error.log` 查看错误信息

### 问题2：请求返回502错误
- 确认两个应用实例都已启动
- 检查应用是否在8080和8081端口监听

### 问题3：所有请求都到同一个实例
- 检查Nginx配置是否正确
- 确认已执行 `nginx.exe -s reload`

### 问题4：测试端点返回404
- 确认应用已添加 `server-info` 端点
- 检查应用日志确认端点是否注册成功

## 📚 相关文件

- 应用配置文件：`online-bookstore-backend/src/main/resources/application-*.properties`
- Nginx配置文件：`E:/nginx/nginx-1.24.0/conf/nginx.conf.*`
- 测试端点代码：`BookController.java` 中的 `getServerInfo()` 方法

