
# Day13 - Tlias智能学习辅助系统（前后端联调与日志）

## 一、前后端分离开发

### 1.1 前后端分离概念

- 前端和后端各自独立开发，通过接口文档约定数据格式
- 一个功能对应一个接口，接口文档是前后端协作的契约

### 1.2 开发流程

1. 需求分析 → 定义接口
2. 前后端各自开发
3. 前后端联调
4. 测试上线

---

## 二、RESTful 规范与 Apifox

### 2.1 REST 规范

REST（Representational State Transfer，表述性状态转换）：

| 规则 | 说明 |
|------|------|
| URL 定位资源 | `/depts` 表示部门资源，常用复数形式 |
| HTTP 动词描述操作 | GET 查询、POST 新增、PUT 修改、DELETE 删除 |
| 无状态通信 | 每次请求包含所有必要信息 |

**标准 RESTful 风格对照：**

| 操作 | 请求方式 | URL 示例 |
|------|----------|----------|
| 查询全部 | `GET` | `/depts` |
| 查询单个 | `GET` | `/depts/{id}` |
| 新增 | `POST` | `/depts` |
| 修改 | `PUT` | `/depts` |
| 删除 | `DELETE` | `/depts` |

### 2.2 SpringBoot 简化注解

| 注解 | 等价于 | 说明 |
|------|--------|------|
| `@GetMapping("/depts")` | `@RequestMapping(value="/depts", method=RequestMethod.GET)` | 查询 |
| `@PostMapping("/depts")` | `@RequestMapping(value="/depts", method=RequestMethod.POST)` | 新增 |
| `@PutMapping("/depts")` | `@RequestMapping(value="/depts", method=RequestMethod.PUT)` | 修改 |
| `@DeleteMapping("/depts")` | `@RequestMapping(value="/depts", method=RequestMethod.DELETE)` | 删除 |

> 可以将增删改查的公共路径抽取到类顶部的 `@RequestMapping` 上，各方法只写差异化部分。

### 2.3 Apifox 工具

用于接口调试和文档管理的工具，支持导入接口、自动生成文档和模拟请求。

---

## 三、项目搭建与配置

### 3.1 项目起步

1. 创建 SpringBoot 工程，引入依赖（Spring Web、MyBatis、MySQL Driver、Lombok）
2. 创建数据库表，配置 `application.yml`

### 3.2 application.yml 配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/tlias
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: 123456

mybatis:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
    map-underscore-to-camel-case: true
```

### 3.3 驼峰命名自动映射

数据库字段使用下划线命名（如 `create_time`），实体类使用驼峰命名（如 `createTime`）。

开启 `map-underscore-to-camel-case: true` 后，MyBatis 会自动完成映射，这是最优方案。

**三种解决字段不匹配的方法：**

| 方式 | 说明 | 推荐度 |
|------|------|--------|
| 手动结果映射 | `@Results({@Result(column="create_time", property="createTime")})` | 不推荐 |
| SQL 起别名 | `SELECT create_time createTime FROM dept` | 一般 |
| 开启驼峰映射 | `map-underscore-to-camel-case: true` | **最优** |

---

## 四、三层架构与 CRUD 实现

### 4.1 项目结构

```
controller/DeptController.java   —— 接收请求、返回响应
service/DeptService.java          —— 业务接口
service/impl/DeptServiceImpl.java —— 业务实现（补全数据、调用 Mapper）
mapper/DeptMapper.java            —— 数据库操作
pojo/Dept.java                    —— 实体类
pojo/Result.java                  —— 统一响应结果
```

### 4.2 数据封装：Result 统一响应

```java
@Data
public class Result {
    private Integer code;  // 1 成功，0 失败
    private String msg;    // 提示信息
    private Object data;   // 返回数据

    public static Result success() { ... }      // 无数据成功
    public static Result success(Object obj) { ... }  // 带数据成功
    public static Result error(String msg) { ... }     // 失败
}
```

所有接口统一返回 `Result` 对象，前端根据 `code` 判断请求是否成功。

### 4.3 CRUD 完整示例

**Controller 层：**

```java
@Slf4j
@RestController
public class DeptController {
    @Autowired
    private DeptService deptService;

    @GetMapping("/depts")
    public Result list() {
        log.info("查询所有部门");
        List<Dept> deptList = deptService.findAll();
        return Result.success(deptList);
    }

    @DeleteMapping("/depts")
    public Result delete(Integer id) {
        log.info("删除部门{}", id);
        deptService.deleteById(id);
        return Result.success();
    }

    @PostMapping("/depts")
    public Result add(@RequestBody Dept dept) {
        log.info("添加部门{}", dept);
        deptService.add(dept);
        return Result.success();
    }

    @GetMapping("/depts/{id}")
    public Result getInfo(@PathVariable("id") Integer deptId) {
        log.info("查询部门{}", deptId);
        Dept dept = deptService.getById(deptId);
        return Result.success(dept);
    }

    @PutMapping("/depts")
    public Result update(@RequestBody Dept dept) {
        log.info("修改部门{}", dept);
        deptService.update(dept);
        return Result.success();
    }
}
```

**Service 层（补全数据 + 调用 Mapper）：**

```java
@Service
public class DeptServiceImpl implements DeptService {
    @Autowired
    private DeptMapper deptMapper;

    @Override
    public void add(Dept dept) {
        dept.setCreateTime(LocalDateTime.now());  // 补全创建时间
        dept.setUpdateTime(LocalDateTime.now());  // 补全更新时间
        deptMapper.add(dept);
    }

    @Override
    public void update(Dept dept) {
        dept.setUpdateTime(LocalDateTime.now());  // 更新修改时间
        deptMapper.update(dept);
    }
}
```

**Mapper 层：**

```java
@Mapper
public interface DeptMapper {
    @Select("SELECT id,name,create_time,update_time FROM dept ORDER BY update_time DESC")
    List<Dept> findAll();

    @Delete("DELETE FROM dept WHERE id=#{id}")
    void deleteById(Integer id);

    @Insert("INSERT INTO dept(name,create_time,update_time) VALUES (#{name},#{createTime},#{updateTime})")
    void add(Dept dept);

    @Select("SELECT id,name,create_time,update_time FROM dept WHERE id=#{deptId}")
    Dept getById(Integer deptId);

    @Update("UPDATE dept SET name=#{name},update_time=#{updateTime} WHERE id=#{id}")
    void update(Dept dept);
}
```

### 4.4 修改操作分为两步

1. **查询回显**：先调用 `GET /depts/{id}` 查出原始数据回填到表单
2. **执行修改**：修改完毕后调用 `PUT /depts` 提交更新

---

## 五、Controller 接收参数

### 5.1 三种接收请求参数的方式

| 方式 | 语法 | 适用场景 |
|------|------|----------|
| HttpServletRequest | `request.getParameter("id")` | 原始方式，不推荐 |
| @RequestParam | `@RequestParam("id") Integer id` | 参数名与形参名不同时使用，`required` 默认 `true` |
| 直接映射 | 形参名与参数名一致时直接写 | **最推荐** |

```java
// 方式三（推荐）：参数名一致时直接接收
public Result delete(Integer id) { ... }
```

### 5.2 接收 JSON 请求参数

使用 `@RequestBody` 注解 + 实体类接收：

```java
@PostMapping("/depts")
public Result add(@RequestBody Dept dept) { ... }
```

### 5.3 接收路径参数

URL 中用 `{...}` 占位，方法形参用 `@PathVariable` 获取：

```java
@GetMapping("/depts/{id}")
public Result getInfo(@PathVariable("id") Integer deptId) { ... }
```

> 形参名与路径名一致时，可省略 `@PathVariable` 的 value 属性。多个路径参数需各自加 `@PathVariable`。

---

## 六、Nginx 反向代理

### 6.1 前后端联调架构

```
前端(Nginx:90端口) → 反向代理 → 后端(SpringBoot:8080端口)
```

### 6.2 反向代理优势

| 优势 | 说明 |
|------|------|
| 安全性 | 后端端口不直接暴露给前端 |
| 灵活性 | 后端服务器变更不影响前端请求 |
| 负载均衡 | 代理服务器可将请求分摊到多台后端 |

### 6.3 Nginx 配置解析

```nginx
server {
    listen 90;                    # 监听端口
    location /api/ {              # 匹配规则
        rewrite ^/api/(.*)$ /$1 break;  # 重写路径（去掉 /api 前缀）
        proxy_pass http://localhost:8080; # 代理转发
    }
}
```

---

## 七、日志技术（Logback + SLF4J）

### 7.1 日志的作用

| 用途 | 说明 |
|------|------|
| 数据追踪 | 记录请求处理过程 |
| 性能优化 | 分析慢操作 |
| 问题排查 | 定位错误原因 |
| 系统监控 | 监控运行状态 |

### 7.2 SLF4J 与 Logback

- **SLF4J**：简单日志门面（Simple Logging Facade for Java），日志规范/接口
- **Logback**：SLF4J 的实现，SpringBoot 默认集成

### 7.3 日志级别（由低到高）

| 级别 | 用途 |
|------|------|
| `TRACE` | 追踪，最详细 |
| `DEBUG` | 调试信息 |
| `INFO` | 一般信息（默认级别） |
| `WARN` | 警告 |
| `ERROR` | 错误 |

> 只会输出 **当前级别及以上** 的日志。如设为 `INFO`，则 `DEBUG` 和 `TRACE` 不输出。

### 7.4 使用方式

**方式一：传统方式**

```java
private static final Logger log = LoggerFactory.getLogger(类名.class);
```

**方式二：Lombok 注解（推荐）**

```java
@Slf4j  // 在类上加此注解，自动生成 log 对象
public class DeptController {
    // 直接使用
    log.info("查询所有部门");
    log.debug("调试信息");
    log.error("错误信息", exception);
}
```

### 7.5 logback.xml 配置

```xml
<configuration>
    <!-- 控制台输出 -->
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50}-%msg%n</pattern>
        </encoder>
    </appender>

    <!-- 文件输出（按大小和时间滚动） -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <FileNamePattern>D:/tlias-%d{yyyy-MM-dd}-%i.log</FileNamePattern>
            <MaxHistory>30</MaxHistory>
            <maxFileSize>10MB</maxFileSize>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50}-%msg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="STDOUT"/>
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```

| 配置项 | 说明 |
|--------|------|
| `%d` | 日期时间 |
| `%thread` | 线程名 |
| `%-5level` | 日志级别，左对齐 5 字符宽度 |
| `%logger{50}` | Logger 名称，最多 50 字符 |
| `%msg` | 日志消息 |
| `%n` | 换行 |
| `MaxHistory` | 最多保留 30 天历史日志 |
| `maxFileSize` | 单文件超过 10MB 触发滚动 |

---

## 今日总结

| 模块 | 核心内容 | 掌握程度 |
|------|----------|----------|
| 前后端分离 | 接口文档、RESTful 规范 | 理解 |
| 简化注解 | @GetMapping / @PostMapping / @PutMapping / @DeleteMapping | 熟练 |
| 项目结构 | Controller → Service → Mapper 三层 | 熟练 |
| 统一响应 | Result 类封装 code + msg + data | 熟练 |
| 驼峰映射 | map-underscore-to-camel-case | 掌握 |
| 参数接收 | @RequestParam、@RequestBody、@PathVariable | 掌握 |
| Nginx | 反向代理、路径重写、proxy_pass | 理解 |
| 日志 | SLF4J + Logback、@Slf4j、日志级别 | 掌握 |

