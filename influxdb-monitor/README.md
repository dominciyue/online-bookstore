# InfluxDB 系统监控指南

## 一、快速启动

### 1. 启动服务
```powershell
cd E:\web\influxdb-monitor
docker-compose up -d
```

或双击运行 `start-monitoring.bat`

### 2. 访问 Web 界面
- **地址**: http://localhost:8086
- **用户名**: `admin`
- **密码**: `admin123456`

## 二、在 Explore 中查看监控数据

### 步骤 1: 登录 InfluxDB
1. 打开浏览器访问 http://localhost:8086
2. 输入用户名 `admin` 和密码 `admin123456`

### 步骤 2: 进入 Explore 页面
1. 点击左侧菜单栏的 **"Explore"** 图标（图表图标）
2. 确保选择了正确的 Bucket: `system_metrics`

### 步骤 3: 查询 CPU 使用率
在查询构建器中：
1. **FROM**: 选择 `cpu`
2. **Filter**: 选择 `cpu = cpu-total`
3. **Filter**: 选择 `_field = usage_percent` 或 `usage_user`
4. 点击 **Submit** 查看图表

### 步骤 4: 查询内存使用率
1. **FROM**: 选择 `mem`
2. **Filter**: 选择 `_field = used_percent`
3. 点击 **Submit**

### 步骤 5: 查询磁盘使用率
1. **FROM**: 选择 `disk`
2. **Filter**: 选择 `_field = used_percent`
3. 点击 **Submit**

### 步骤 6: 查询网络流量
1. **FROM**: 选择 `net`
2. **Filter**: 选择 `_field = bytes_recv` 或 `bytes_sent`
3. 点击 **Submit**

## 三、常用 Flux 查询语句

### CPU 使用率
```flux
from(bucket: "system_metrics")
  |> range(start: -1h)
  |> filter(fn: (r) => r._measurement == "cpu")
  |> filter(fn: (r) => r.cpu == "cpu-total")
  |> filter(fn: (r) => r._field == "usage_percent")
```

### 内存使用情况
```flux
from(bucket: "system_metrics")
  |> range(start: -1h)
  |> filter(fn: (r) => r._measurement == "mem")
  |> filter(fn: (r) => r._field == "used_percent")
```

### 磁盘使用情况
```flux
from(bucket: "system_metrics")
  |> range(start: -1h)
  |> filter(fn: (r) => r._measurement == "disk")
  |> filter(fn: (r) => r._field == "used_percent")
```

### 网络流量
```flux
from(bucket: "system_metrics")
  |> range(start: -1h)
  |> filter(fn: (r) => r._measurement == "net")
  |> filter(fn: (r) => r._field == "bytes_recv" or r._field == "bytes_sent")
```

## 四、截图建议

在作业中需要截图以下内容：

1. **CPU 使用率图表** - 展示 CPU 负载变化
2. **内存使用率图表** - 展示内存占用情况
3. **磁盘使用率图表** - 展示各磁盘分区的使用情况
4. **网络流量图表** - 展示网络收发数据量

## 五、运行状态分析示例

根据截图，可以这样描述笔记本电脑的运行状态：

> **CPU 使用情况**：CPU 使用率平均在 XX%，偶尔有峰值达到 XX%，整体负载较轻/中等/较重。
>
> **内存使用情况**：内存使用率约为 XX%，剩余可用内存 XX GB，内存资源充足/紧张。
>
> **磁盘使用情况**：系统盘 (C:) 使用率为 XX%，数据盘 (D:) 使用率为 XX%，磁盘空间充足/需要清理。
>
> **网络流量**：网络接收速率约为 XX KB/s，发送速率约为 XX KB/s，网络活动正常。

## 六、停止服务

```powershell
cd E:\web\influxdb-monitor
docker-compose down
```

或双击运行 `stop-monitoring.bat`

## 七、故障排除

### 如果看不到数据
1. 等待 1-2 分钟让 Telegraf 采集数据
2. 检查 Telegraf 日志：`docker logs telegraf`
3. 确认时间范围选择正确（默认 Last 1h）

### 如果服务无法启动
1. 确保 Docker Desktop 正在运行
2. 确保端口 8086 未被占用
3. 运行 `docker-compose logs` 查看错误信息

