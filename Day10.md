
# Day10 - HTTP响应、三层架构、IOC与DI、Spring注解

---

## 一、HTTP 响应数据格式

### 1.1 响应结构概览

HTTP 响应由三部分组成：

| 组成部分 | 格式 | 说明 |
|----------|------|------|
| 响应行 | `协议版本 状态码 状态描述` | 例如 `HTTP/1.1 200 OK` |
| 响应头 | `Key: Value` | 描述响应元信息 |
| 响应体 | 实际数据 | 返回给客户端的正文内容 |

### 1.2 响应行

```
HTTP/1.1 200 OK
```

| 字段 | 说明 |
|------|------|
| 协议版本 | HTTP/1.1、HTTP/2 等 |
| 状态码 | 三位数字，表示请求处理结果 |
| 状态描述 | 对状态码的简短文本说明 |

### 1.3 响应头

以 `Key: Value` 格式传递响应的元信息。

| 常见响应头 | 说明 |
|-----------|------|
| `Content-Type` | 响应体的 MIME 类型（如 `text/html; charset=UTF-8`） |
| `Content-Length` | 响应体的字节长度 |
| `Content-Encoding` | 响应体的压缩编码方式（如 `gzip`） |
| `Connection` | 连接管理方式（`keep-alive` 保持连接 / `close` 关闭） |

### 1.4 响应体

服务器返回的实际数据，可以是 HTML、JSON、XML、图片等。

### 1.5 状态码分类

| 状态码范围 | 类别 | 说明 | 常见示例 |
|-----------|------|------|----------|
| **1xx** | 临时响应 | 请求已接收，继续处理 | `101` 切换协议（WebSocket 升级） |
| **2xx** | 成功 | 请求已被成功处理 | `200 OK` |
| **3xx** | 重定向 | 需要进一步操作才能完成请求 | 请求百度用 `http://` 时自动重定向到 `https://` |
| **4xx** | 客户端错误 | 请求包含错误语法或无法完成 | `404` 请求的资源不存在 |
| **5xx** | 服务端错误 | 服务器处理请求失败 | `500` 服务器内部异常（代码逻辑报错） |

### 1.6 响应设置方式

**方式一：HttpServletResponse 封装**

Servlet 提供了 `HttpServletResponse` 对象，调用其方法即可设置响应的各项属性。

```java
response.setContentType("application/json");
response.setCharacterEncoding("UTF-8");
response.setStatus(200);
```

**方式二：Spring 的 ResponseEntity**

Spring 提供 `ResponseEntity<泛型>`，可以直接创建一个对象作为返回值，包含状态码、响应头和响应体。

```java
return ResponseEntity.ok(data);
return ResponseEntity.status(404).body("资源不存在");
```

---

## 二、Web 三层架构

### 2.1 架构概览

```
请求 → Controller 控制层 → Service 业务逻辑层 → DAO 数据访问层 → 数据库
                                                      ↑
响应 ← Controller 控制层 ← Service 业务逻辑层 ← DAO 数据访问层 ← ┘
```

### 2.2 各层职责

| 层级 | 英文 | 职责 | 典型示例 |
|------|------|------|----------|
| **Controller** | 控制层 | 接收前端请求，调用 Service 处理，返回响应数据 | `@RestController` 注解的类 |
| **Service** | 业务逻辑层 | 处理具体的业务逻辑，协调多个 DAO | `@Service` 注解的类 |
| **DAO** | 数据访问层 | 负责数据访问操作（增删改查） | `@Repository` 注解的类 |

### 2.3 设计原则

- **单一职责原则**：每个层只负责自己的职责，边界清晰
- **高内聚**：同一层内部功能紧密关联
- **低耦合**：层与层之间通过接口交互，降低依赖
- **便于复用和维护**：修改某一层不影响其他层

---

## 三、IOC 与 DI

### 3.1 耦合与内聚

| 概念 | 定义 | 目标 |
|------|------|------|
| **耦合** | 各个层之间依赖关联的程度 | 越低越好 |
| **内聚** | 软件中各个功能模块内部的功能联系 | 越高越好 |

> **核心目标**：高内聚，低耦合 —— 模块内部紧密，模块之间松散。

### 3.2 IOC 控制反转

**IOC**（Inversion of Control）将对象的**创建控制权**由程序自身转移到外部容器（Spring 容器）。

| 对比 | 传统方式 | IOC 方式 |
|------|----------|----------|
| 对象创建 | 程序内部 `new` | 由 Spring 容器创建 |
| 控制权 | 程序控制 | 容器控制 |
| 耦合度 | 高 | 低 |

### 3.3 DI 依赖注入

**DI**（Dependency Injection）是 IOC 的具体实现方式：容器在运行时为应用程序**提供**其所依赖的资源。

```
IOC 是思想：控制权反转
DI  是手段：依赖的注入方式
```

### 3.4 Bean 对象

IOC 容器中**创建和管理的对象**都称为 **Bean**。

| 要点 | 说明 |
|------|------|
| 定义 | IOC 容器管理的对象实例 |
| 生命周期 | 由 Spring 容器统一管理（创建 → 初始化 → 使用 → 销毁） |
| 获取方式 | 通过 `@Autowired` 注入或从容器中手动获取 |

---

## 四、Spring 注解

### 4.1 @Component

将当前类交给 IOC 容器管理（加在**实现类**上）。

```java
@Component
public class UserServiceImpl implements UserService {
    // 该类会被 Spring 扫描并创建 Bean
}
```

### 4.2 衍生注解

以下三个注解都是 `@Component` 的衍生注解，语义更明确：

| 注解 | 添加位置 | 说明 |
|------|----------|------|
| `@Repository` | DAO 实现类 | 标记数据访问层 Bean |
| `@Service` | Service 实现类 | 标记业务逻辑层 Bean |
| `@RestController` | Controller 实现类 | 标记控制层 Bean（相当于 `@Controller + @ResponseBody`） |

```java
// DAO 层
@Repository
public class UserDaoImpl implements UserDao { }

// Service 层
@Service
public class UserServiceImpl implements UserService { }

// Controller 层
@RestController
public class UserController { }
```

### 4.3 @ComponentScan

声明 Bean 的四大注解要生效，必须被 `@ComponentScan` 扫描。

| 要点 | 说明 |
|------|------|
| 作用 | 指定 Spring 扫描哪些包下的 Bean |
| 默认配置 | 启动类已包含该注解 |
| 扫描范围 | 仅扫描**本包及其子包**下的 Bean |

```java
@SpringBootApplication  // 内部已包含 @ComponentScan
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 4.4 @Autowired

应用程序运行时，Spring 会**自动查询**该类型的 Bean 对象，进行依赖注入。

```java
@RestController
public class UserController {

    @Autowired
    private UserService userService;  // Spring 自动注入 UserService 的 Bean
}
```

---

## 五、依赖注入方式对比

### 5.1 属性注入（字段注入）

在字段上直接加 `@Autowired`。

```java
@RestController
public class UserController {
    @Autowired
    private UserService userService;
}
```

| 优点 | 缺点 |
|------|------|
| 代码简洁，方便快速开发 | 隐藏了类之间的依赖关系，可能破坏类的封装性 |

### 5.2 构造函数注入

通过构造函数传入依赖。

```java
@RestController
public class UserController {

    private final UserService userService;

    // 只有一个构造函数时，@Autowired 可以省略
    public UserController(UserService userService) {
        this.userService = userService;
    }
}
```

| 优点 | 缺点 |
|------|------|
| 清晰展示类的依赖关系 | 代码较繁琐 |
| 提高代码安全性（依赖不可变） | 依赖过多时构造函数臃肿 |
| 便于单元测试 | — |

> **注意**：当类只有一个构造函数时，`@Autowired` 可以省略。

### 5.3 Setter 注入

通过 Setter 方法注入依赖。

```java
@RestController
public class UserController {

    private UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
```

| 优点 | 缺点 |
|------|------|
| 保持了类的封装性 | 需要额外编写 setter 方法 |
| 依赖关系清晰 | 增加代码量 |
| 依赖可选（可不注入） | — |

### 5.4 对比总结

| 对比维度 | 属性注入 | 构造函数注入 | Setter 注入 |
|----------|----------|-------------|------------|
| 代码量 | 最少 | 较多 | 中等 |
| 封装性 | 弱 | 强（依赖 immutable） | 中 |
| 依赖可见性 | 隐藏 | 清晰 | 清晰 |
| 依赖必要性 | 必填 | 必填 | 可选 |
| 推荐程度 | 一般 | **推荐** | 可选依赖时使用 |

---

## 六、多 Bean 冲突处理

当容器中存在**多个同类型**的 Bean 时，Spring 无法确定注入哪个，需要手动指定。

### 6.1 @Primary

在某个 Bean 上标注 `@Primary`，使其成为**优先注入**的 Bean。

```java
@Service
@Primary
public class UserServiceImplA implements UserService { }
```

### 6.2 @Qualifier

配合 `@Autowired` 使用，通过 **Bean 名称**指定注入哪个 Bean。

```java
@RestController
public class UserController {

    @Autowired
    @Qualifier("userServiceImplB")  // 指定 bean 名
    private UserService userService;
}
```

### 6.3 @Resource

通过 `name` 属性指定 Bean 名称进行注入（JDK 注解，非 Spring 特有）。

```java
@RestController
public class UserController {

    @Resource(name = "userServiceImplB")
    private UserService userService;
}
```

### 6.4 对比总结

| 注解 | 来源 | 匹配方式 | 配合使用 |
|------|------|----------|----------|
| `@Primary` | Spring | 标记优先 Bean | 单独使用 |
| `@Qualifier("bean名")` | Spring | 按名称指定 | 配合 `@Autowired` |
| `@Resource(name="类名")` | JDK（javax.annotation） | 按名称指定 | 单独使用 |

---

## 今日总结

| 模块 | 核心要点 |
|------|----------|
| HTTP 响应格式 | 响应行（协议+状态码+描述）/ 响应头 / 响应体；1xx~5xx 五类状态码 |
| 响应设置 | HttpServletResponse 封装 / Spring 的 ResponseEntity |
| Web 三层架构 | Controller（请求响应）/ Service（业务逻辑）/ DAO（数据访问），高内聚低耦合 |
| IOC 与 DI | 控制反转（创建权转移给容器）/ 依赖注入（容器提供资源）/ Bean（容器管理对象） |
| Spring 注解 | @Component 及 @Repository/@Service/@RestController 衍生注解；@ComponentScan 扫描范围；@Autowired 自动注入 |
| 依赖注入方式 | 属性注入（简洁）/ 构造函数注入（推荐，清晰安全）/ Setter 注入（封装性好） |
| 多 Bean 冲突 | @Primary（优先）/ @Qualifier（按名指定）/ @Resource（JDK 按名注入） |

