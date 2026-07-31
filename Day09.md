
# Day09 - JUnit单元测试 & SpringBoot Web入门 & HTTP协议

---

## 一、JUnit 单元测试

### 1.1 传统 main 方法测试的缺点

| 缺点 | 说明 |
|------|------|
| 测试代码与源码未分离 | 测试逻辑与业务代码混在一起，难以维护和清理 |
| 一个方法失败影响后续 | main 方法中顺序执行，某个测试异常会中断后续所有测试 |
| 无法自动化测试 | 每次需要手动运行 main，结果要靠肉眼对比，无法集成到 CI/CD 流水线 |

### 1.2 JUnit 简介

JUnit 是 Java 最主流的单元测试框架（当前主流版本为 JUnit 5），提供注解驱动、断言丰富的测试方式。

**基本规范**：

```java
// 测试方法命名习惯：被测试方法名 + Test
public void 方法名Test() { ... }
```

### 1.3 常用断言方法

断言全部来自 `org.junit.jupiter.api.Assertions` 类，用于判断实际结果是否符合预期。

| 方法 | 签名 | 说明 |
|------|------|------|
| `assertEquals` | `assertEquals(expected, actual, message)` | 判断两个值是否相等 |
| `assertNotEquals` | `assertNotEquals(unexpected, actual, message)` | 判断两个值是否不等 |
| `assertNull` | `assertNull(actual, message)` | 判断对象为 null |
| `assertNotNull` | `assertNotNull(actual, message)` | 判断对象不为 null |
| `assertTrue` | `assertTrue(condition, message)` | 判断条件为 true |
| `assertFalse` | `assertFalse(condition, message)` | 判断条件为 false |
| `assertThrows` | `assertThrows(异常类型.class, () -> { ... }, message)` | 判断执行代码是否抛出指定异常 |

**代码示例**：

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CalculatorTest {

    @Test
    void addTest() {
        Calculator calc = new Calculator();
        int result = calc.add(2, 3);
        assertEquals(5, result, "2 + 3 应该等于 5");
    }

    @Test
    void divideTest() {
        Calculator calc = new Calculator();
        // 验证除以 0 抛出 ArithmeticException
        assertThrows(ArithmeticException.class,
                () -> calc.divide(10, 0),
                "除以 0 应该抛出 ArithmeticException");
    }

    @Test
    void userNullTest() {
        User user = null;
        assertNull(user, "用户对象应该为 null");
    }
}
```

### 1.4 常用注解

| 注解 | 作用 | 说明 |
|------|------|------|
| `@Test` | 标记为测试方法 | JUnit 通过此注解识别并执行测试 |
| `@BeforeAll` | 所有测试前执行一次 | 必须修饰**静态方法**，用于初始化全局资源（如数据库连接池） |
| `@AfterAll` | 所有测试后执行一次 | 必须修饰**静态方法**，用于释放全局资源 |
| `@BeforeEach` | 每个测试方法前执行 | 用于每个测试前的数据准备，确保测试独立性 |
| `@AfterEach` | 每个测试方法后执行 | 用于每个测试后的清理工作 |
| `@ParameterizedTest` | 参数化测试 | 替换 `@Test`，配合 `@ValueSource` 使用，同一方法用多组参数执行 |
| `@ValueSource` | 提供参数来源 | 配合 `@ParameterizedTest`，传入 ints / strings / doubles 等 |
| `@DisplayName` | 自定义测试显示名 | 让测试报告更可读 |

**完整示例**：

```java
import org.junit.jupiter.api.*;

@DisplayName("用户服务测试")
class UserServiceTest {

    @BeforeAll
    static void setUpAll() {
        System.out.println("【全局初始化】在所有测试前执行一次");
    }

    @AfterAll
    static void tearDownAll() {
        System.out.println("【全局清理】在所有测试后执行一次");
    }

    @BeforeEach
    void setUp() {
        System.out.println("  每个测试前执行...");
    }

    @AfterEach
    void tearDown() {
        System.out.println("  每个测试后执行...");
    }

    @Test
    @DisplayName("测试用户注册成功")
    void registerSuccessTest() {
        assertEquals(1, 1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"admin", "user", "guest"})
    @DisplayName("测试不同角色登录")
    void loginWithRolesTest(String role) {
        System.out.println("  当前测试角色: " + role);
        assertNotNull(role);
    }
}
```

**执行顺序**：

```
@BeforeAll（一次）
  └─ @BeforeEach → @Test → @AfterEach（重复 N 次）
  └─ @BeforeEach → @Test → @AfterEach
  ...
@AfterAll（一次）
```

### 1.5 企业规范

1. **测试覆盖**：单元测试应尽可能覆盖业务方法的所有可能情况，尤其是：
   - 正常路径（happy path）
   - **边界值**：如空字符串、0、负数、最大/最小值
   - 异常路径：非法参数、空指针等
2. **单元测试覆盖率**：通常要求行覆盖率 ≥ 80% 或分支覆盖率 ≥ 70%，具体以团队约定为准。
3. 测试应保持**独立性和可重复性**，不依赖执行顺序。

### 1.6 Maven 依赖范围（scope）

| scope | 说明 | 典型场景 |
|-------|------|----------|
| `compile` | 默认值，编译 / 测试 / 运行三个阶段都可用 | 业务核心依赖（如 Spring 核心包） |
| `test` | **仅在测试阶段可用**，主代码中无法引用 | JUnit、Mockito 等测试框架 |

```xml
<!-- pom.xml 示例 -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.10.0</version>
    <scope>test</scope>
</dependency>
```

### 1.7 实用技巧 — 删除 Maven 下载失败的文件

当 Maven 下载依赖失败时会留下 `.lastUpdated` 文件，导致不再重试下载。可在仓库根目录执行：

```powershell
del /s *.lastUpdated
```

`/s` 表示递归删除当前目录及所有子目录下的 `.lastUpdated` 文件。

---

## 二、Web 基础概念

### 2.1 静态资源 vs 动态资源

| 对比维度 | 静态资源 | 动态资源 |
|----------|----------|----------|
| 职责 | 页面展示 | 逻辑处理 |
| 典型技术 | HTML、CSS、JavaScript、图片 | Servlet、JSP、Controller（Spring） |
| 特点 | 内容固定，不随请求变化 | 内容由服务器逻辑生成，每次可能不同 |
| 示例 | 导航栏、Logo、样式表 | 用户登录验证、订单查询、数据接口 |

### 2.2 B/S 架构 vs C/S 架构

| 对比维度 | B/S（Browser/Server） | C/S（Client/Server） |
|----------|----------------------|-----------------------|
| 客户端 | 浏览器，无需安装 | 需单独开发维护专用客户端 |
| 维护成本 | 低，只需更新服务器 | 高，每个客户端都需更新 |
| 逻辑部署 | 全部在服务器端 | 客户端 + 服务器端分担 |
| 性能 | 受限于网络带宽和浏览器 | 可充分利用客户端硬件能力 |
| 典型例子 | 淘宝网页版、OA 系统 | QQ 桌面版、网游客户端 |

**趋势**：当前企业级开发以 B/S 架构为主流，客户端通过浏览器访问，所有业务逻辑部署在服务器端。

### 2.3 HTTP 协议概述

**HTTP（HyperText Transfer Protocol）**：超文本传输协议，定义了浏览器与服务器之间**数据传输的规则**。

---

## 三、SpringBoot Web 入门

### 3.1 创建 SpringBoot 项目的基本流程

1. 使用 IDEA 新建项目 → Spring Initializr
2. 选择 JDK 版本、项目元信息（Group / Artifact）
3. 勾选 **Spring Web** 起步依赖
4. 完成创建，自动生成主启动类

### 3.2 第一个 Controller

```java
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @RestController = @Controller + @ResponseBody
 * 表示当前类是一个请求处理类，所有方法返回值直接写入 HTTP 响应体
 */
@RestController
public class HelloController {

    /**
     * @RequestMapping 映射 HTTP 请求路径到该方法
     * 当浏览器访问 http://localhost:8080/hello 时触发
     */
    @RequestMapping("/hello")
    public String hello(String name) {
        System.out.println("name: " + name);
        return "Hello " + name + "~";
    }
}
```

**要点解读**：

- `@RestController`：复合注解，等价于 `@Controller` + `@ResponseBody`，返回的字符串直接作为响应体而非视图名。
- `@RequestMapping("/hello")`：将 `/hello` 路径映射到该方法，支持所有 HTTP 方法（GET/POST 等）。后续学习中还会接触到更精细的 `@GetMapping`、`@PostMapping` 等。
- 方法参数 `String name`：Spring 自动将请求参数 `name` 的值绑定到形参。

### 3.3 起步依赖（Starter）

SpringBoot 通过 **起步依赖** 简化依赖管理：使用某功能只需引入对应的 starter，无需手动添加一堆关联的第三方 jar。

```
spring-boot-starter-web
  └── spring-boot-starter（核心）
  └── spring-boot-starter-tomcat（内嵌 Tomcat）
  └── spring-webmvc（Spring MVC）
  └── jackson-databind（JSON 序列化）
  └── ...（其他传递依赖）
```

这背后的原理是 **Maven 的依赖传递机制**——引入一个 starter，它内部声明的所有依赖会自动传递到项目中。

### 3.4 内嵌 Tomcat

- SpringBoot 默认内嵌 Tomcat 服务器，无需单独部署到外部 Tomcat。
- 默认监听端口：**8080**。
- 启动 main 方法后，Tomcat 自动启动并部署当前应用。

---

## 四、HTTP 协议详解

### 4.1 HTTP 协议基本结构

**请求报文 = 请求行 + 请求头 + 空行 + 请求体**

```
POST /api/user/login HTTP/1.1           ← 请求行
Host: www.example.com                   ← 请求头
Content-Type: application/json
Content-Length: 56
                                        ← 空行（必须）
{"username":"admin","password":"123456"} ← 请求体（POST 独有）
```

**响应报文 = 响应行 + 响应头 + 空行 + 响应体**

```
HTTP/1.1 200 OK                         ← 响应行
Content-Type: application/json          ← 响应头
Content-Length: 85
                                        ← 空行（必须）
{"code":200,"msg":"登录成功","token":"xxx"} ← 响应体
```

### 4.2 HTTP 协议三个核心特点

| 特点 | 说明 |
|------|------|
| 基于 TCP 协议 | HTTP 是应用层协议，底层依赖 TCP 的可靠传输 |
| 一次请求对应一次响应 | 一问一答模式，请求与响应一一对应 |
| 无状态协议 | 每次请求响应都是独立的，服务器不记录之前的交互。因此需要 Cookie、Session、Token 等机制来维持会话状态 |

### 4.3 GET 请求格式

GET 请求的参数拼接在 URL 后面：

```
http://localhost:8080/hello?name=张三&age=18
```

格式：`?` 开头，`key=value` 键值对，多个参数用 `&` 分隔。

### 4.4 常见请求头详解

| 请求头 | 含义 | 示例 |
|--------|------|------|
| `Host` | 请求的主机名（域名:端口） | `www.baidu.com` |
| `User-Agent` | 浏览器版本及操作系统信息 | `Mozilla/5.0 (Windows NT 10.0; Win64; x64)` |
| `Accept` | 浏览器能接受的资源类型（MIME） | `text/html,application/json` |
| `Accept-Language` | 偏好语言 | `zh-CN,zh;q=0.9` |
| `Accept-Encoding` | 支持的压缩类型 | `gzip, deflate, br` |
| `Content-Type` | **请求主体**的数据类型 | `application/json`、`application/x-www-form-urlencoded` |
| `Content-Length` | **请求主体**的大小（字节） | `56` |

> **重点**：请求头与请求体之间**必须有一个空行**作为分隔标志。请求体是 **POST 请求独有的**，GET 请求没有请求体。

### 4.5 请求数据的获取流程

1. 浏览器发起 HTTP 请求
2. Tomcat 接收请求，解析请求数据
3. 服务器将解析后的数据封装为 `HttpServletRequest` 对象
4. 调用 Controller 方法时，将封装好的对象作为参数传入（或按需将单个参数值注入）
5. Controller 通过形参直接获取请求数据

```java
@RestController
public class UserController {

    @RequestMapping("/login")
    public String login(HttpServletRequest request) {
        // 通过 HttpServletRequest 获取请求信息
        String username = request.getParameter("username");
        String userAgent = request.getHeader("User-Agent");
        String method = request.getMethod();

        System.out.println("请求方式: " + method);
        System.out.println("用户名: " + username);
        System.out.println("浏览器: " + userAgent);

        return "登录成功，欢迎 " + username;
    }
}
```

**简化写法**（推荐）：

```java
@RequestMapping("/login")
// Spring 自动将同名的请求参数绑定到形参
public String login(String username, String password) {
    return "登录成功，欢迎 " + username;
}
```


---

## 今日总结

| 模块 | 核心要点 |
|------|----------|
| JUnit 单元测试 | 注解驱动（@Test/@BeforeAll/@AfterAll/@BeforeEach/@AfterEach/@ParameterizedTest），六大断言方法，测试覆盖边界值 |
| Maven scope | compile（全阶段）vs test（仅测试阶段），del /s *.lastUpdated 清理下载失败文件 |
| Web 基础 | 静态资源（页面展示）vs 动态资源（逻辑处理），B/S 架构（浏览器访问）vs C/S 架构（专用客户端） |
| SpringBoot Web | @RestController + @RequestMapping，起步依赖（Maven 依赖传递），内嵌 Tomcat 端口 8080 |
| HTTP 协议 | 请求行/头/体 + 响应行/头/体，基于 TCP、一问一答、无状态，GET 参数拼 URL，POST 有请求体 |
| 请求数据获取 | 服务器解析封装为 HttpServletRequest，通过方法参数注入 Controller |

