# Redis 学习笔记

## 一、Redis 简介与特性

### 1.1 SQL 与 NoSQL 对比

| 对比项 | SQL | NoSQL |
|---|---|---|
| 数据结构 | 结构化 | 非结构化 |
| 数据关联 | 关联的 | 无关联的 |
| 查询方式 | SQL 查询 | 非 SQL |
| 事务特性 | ACID | BASE |
| 存储介质 | 磁盘 | 内存 |
| 扩展方式 | 垂直 | 水平 |
| 适用场景 | 数据结构固定，相关业务对数据安全性、一致性要求较高 | 数据结构不固定，对一致性、安全性要求不高，对性能有要求 |

### 1.2 Redis 特性

- 单线程、每个命令具备原子性
- 低延迟、速度快（基于内存、IO 多路复用、良好的编码）
- 支持数据持久化
- 支持主从集群、分片集群
- 支持多语言客户端

### 1.3 常见问题：Redis 是单线程为什么速度这么快？

最重要最核心的原因就是 Redis 基于内存，相较于基于磁盘的 mysql，从内存读取速度快很多。

---

## 二、安装 Redis

### 2.1 推荐方式：Ubuntu 包管理器

```bash
sudo apt update
sudo apt install -y redis-server
```

装完自动带 systemd 服务，比源码编译省事得多。

### 2.2 源码编译安装（官方下载）

下载页上只有**源码包**，Linux 官方不提供预编译二进制，所以下载 `.tar.gz` 源码包自行编译：

```bash
# 固定链接，永远指向最新稳定版
wget https://download.redis.io/redis-stable.tar.gz
wget https://download.redis.io/redis-stable.tar.gz.sha256   # 校验文件，可选

tar -xzf redis-stable.tar.gz
cd redis-stable
make -j4
make install        # 可选，装到 /usr/local/bin
```

要点：

- `redis-stable.tar.gz` 是固定地址，不用自己找版本号。
- 想指定版本可下载 `https://download.redis.io/releases/redis-8.8.0.tar.gz`。
- 下载页上的 **Windows 版是社区维护**的，Linux 环境不要下错。

### 2.3 CentOS 7 的注意事项

1. **最小化安装没有 wget**，会提示 `command not found`：

    ```bash
    yum install -y wget
    ```

2. 不想装 wget 可以用自带的 curl 替代：

    ```bash
    curl -O https://download.redis.io/redis-stable.tar.gz
    ```

3. **CentOS 7 已停止维护**（2024-06 EOL），软件源需要切到 vault 归档地址才能继续装软件：

    ```bash
    sed -i 's/mirrorlist=/#mirrorlist=/g; s|#baseurl=http://mirror.centos.org|baseurl=http://vault.centos.org|g' /etc/yum.repos.d/CentOS-*.repo
    yum clean all
    ```

### 2.4 Ubuntu vs CentOS 7 怎么选

| 对比项 | Ubuntu（推荐） | CentOS 7 |
|---|---|---|
| 维护状态 | LTS 支持 5 年，正常更新 | 已 EOL，停止维护 |
| 安装 Redis | `apt install redis-server` 一条命令 | 包较旧，需要切 vault 源 |
| 资料数量 | 教程最多 | 越来越少 |
| 结论 | 新环境首选 Ubuntu 24.04 LTS | 除非公司强制，否则不推荐 |


---

## 三、启动与验证

```bash
sudo systemctl enable --now redis-server   # 启动并设置开机自启
sudo systemctl status redis-server         # 查看状态

redis-cli ping                              # 返回 PONG 即正常
redis-cli info server | head -5             # 查看版本等信息
```

---

## 四、安全配置（生产必做）

编辑 `/etc/redis/redis.conf`：

```conf
# 只监听本机（默认值，没特殊需求不要改 0.0.0.0）
bind 127.0.0.1

# 设置密码（强烈建议）
requirepass 你的强密码

# 禁用危险命令（可选）
rename-command FLUSHALL ""
rename-command FLUSHDB ""
```

改完重启：

```bash
sudo systemctl restart redis-server
redis-cli -a 你的强密码 ping
```

防火墙（若开启 ufw）：

```bash
sudo ufw allow 6379/tcp
```

⚠️ 注意：Redis 自身没有传输加密，**不要把未设密码的 Redis 直接暴露到公网**。

---

## 五、vim / nano 编辑器

Ubuntu 最小化安装默认没有 vim，报 `vim: command not found` 时：

```bash
sudo apt install -y vim
# 或者装 nano（对新手更友好）
sudo apt install -y nano
```

不想装编辑器，可以用 sed 直接改配置：

```bash
sudo sed -i 's/^# *requirepass foobared/requirepass 你的密码/' /etc/redis/redis.conf
sudo sed -i 's/^bind 127.0.0.1.*/bind 127.0.0.1 你的服务器IP/' /etc/redis/redis.conf
sudo systemctl restart redis-server
```

### nano 常用快捷键

| 操作 | 按键 |
|---|---|
| 保存（不退出） | `Ctrl + O`，回车确认 |
| 退出 | `Ctrl + X` |
| 保存并退出 | `Ctrl + X` → `Y` → 回车 |
| 跳到指定行 | `Ctrl + _`，输入行号 |
| 搜索 | `Ctrl + W`；`Alt + W` 找下一个 |
| 替换 | `Ctrl + \` |
| 剪切整行 | `Ctrl + K` |
| 粘贴 | `Ctrl + U` |
| 复制整行 | `Alt + ^`，然后 `Ctrl + U` |
| 撤销 / 重做 | `Alt + U` / `Alt + E` |
| 显示行号 | `Alt + #` |
| 帮助 | `Ctrl + G` |

底部两行会实时提示可用快捷键：`^O` 表示 `Ctrl+O`，`M-U` 表示 `Alt+U`。

---

## 六、常见问题：java.net.ConnectException: Connection refused

含义：Java 程序连接某个 IP:端口时**被拒绝**，多数是"服务没监听在该地址/端口"或"网络不通"。

按顺序排查：

| 步骤  | 检查内容                 | 命令                                          |
| --- | -------------------- | ------------------------------------------- |
| 1   | Redis 服务是否在运行        | `sudo systemctl status redis-server`        |
| 2   | 监听在哪个地址和端口           | `ss -tlnp \| grep 6379`                     |
| 3   | 客户端连的 host/port 是否正确 | Java 代码或配置                                  |
| 4   | 网络是否可达               | `nc -vz IP 6379` / `telnet IP 6379`         |
| 5   | 防火墙是否放行              | `sudo ufw status`、`sudo ufw allow 6379/tcp` |

关键判断：

- 监听显示 `127.0.0.1:6379` → 只能本机连接，外部访问会拒绝。
- 端口根本没监听 → 服务没起来，先看日志。
- 密码错误报 `NOAUTH` / `WRONGPASS`，**不是** Connection refused，别往密码方向排查。

---

## 七、Windows 连接虚拟机中的 Redis

### 7.1 虚拟机网络模式

| 模式 | 能否从 Windows 直接访问 | 说明 |
|---|---|---|
| 桥接 Bridged | ✅ | 虚拟机有局域网 IP，最省事 |
| NAT | ❌ | 需端口转发或 SSH 隧道 |
| 仅主机 Host-Only | ✅ | 仅 Windows 与虚拟机互通 |

### 7.2 修改 Redis 配置

```conf
bind 0.0.0.0        # 监听所有网卡
requirepass 你的强密码
```

```bash
sudo systemctl restart redis-server
sudo ufw allow 6379/tcp
```

### 7.3 获取 IP 并测试

虚拟机里：

```bash
hostname -I         # 例如 192.168.1.100
```

Windows PowerShell 里：

```powershell
Test-NetConnection 192.168.1.100 -Port 6379
# TcpTestSucceeded : True 即连通
```

### 7.4 使用客户端连接

- **图形工具**：
- **命令行**：

    ```bash
    redis-cli -h 192.168.1.100 -p 6379 -a 你的密码 ping
    ```

- **Java**：

    ```java
    new Jedis("192.168.1.100", 6379);
    ```

### 7.5 更安全的方式：SSH 隧道

不想把 Redis 暴露到局域网时，Windows 里开一条隧道：

```powershell
ssh -L 6379:localhost:6379 用户名@192.168.1.100
```

之后所有程序连接 `localhost:6379` 即可，数据走 SSH 加密，Redis 也不用改 bind。NAT 模式下同样适用。
