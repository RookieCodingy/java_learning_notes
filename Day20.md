
# Day20 - SpringBoot 原理与 Bean 管理

## 一、配置优先级

SpringBoot 支持多种配置方式，当同一属性在多个位置配置时，按优先级从高到低：

### 1.1 五种配置优先级

| 优先级 | 配置方式 | 说明 |
|------|------|------|
| 最高 | 命令行参数 | 启动时 `--server.port=9090` |
| 高 | Java 系统属性 | IDEA → Edit Configuration → VM options: `-Dserver.port=9090` |
| 中 | application.properties | `server.port=8081` |
| 低 | application.yml | `server.port: 8082`（主流格式） |
| 最低 | application.yaml | `server.port: 8082` |

命令行 > 系统属性 > properties > yml > yaml

### 1.2 验证示例

同时创建三个配置文件并设置不同端口：

```properties
# application.properties
server.port=8081
```

```yaml
# application.yml
server:
  port: 8082
```

```yaml
# application.yaml
server:
  port: 8082
```

启动后实际生效的是 `application.properties` 中的 `8081`，因为 properties 优先级高于 yml/yaml。

---

## 二、Bean 的管理

IOC 容器中管理的对象称为 Bean。

### 2.1 Bean 的作用域

通过 `@Scope("")` 注解设置 Bean 的作用范围。

| 作用域 | 说明 | 实例化时机 | 使用场景 |
|------|------|------|------|
| **singleton** | 容器内同名 Bean 只有一个实例（**默认**，单例） | 项目启动时创建；加 `@Lazy` 可延迟到首次使用时 | 无状态 Bean（大部分情况） |
| **prototype** | 每次使用该 Bean 都创建新实例（多例） | 每次获取时创建 | 有状态 Bean |
| request | 每个请求范围内创建新实例 | Web 环境 | — |
| session | 每个会话范围内创建新实例 | Web 环境 | — |
| application | 每个应用范围内创建新实例 | Web 环境 | — |

> 后三种仅限 Web 环境，了解即可。

**面试题 1**：Spring 容器中的 Bean 是单例还是多例的？单例 Bean 什么时候实例化？

> 默认是**单例**的。单例 Bean 默认在**项目启动时**创建，可通过 `@Lazy` 注解延迟初始化，推迟到第一次使用时才创建。

**面试题 2**：Spring 容器中的 Bean 是线程安全的吗？

| 情况 | 线程安全性 |
|------|------|
| 单例 + 无状态（不保存数据） | **安全** |
| 单例 + 有状态（保存数据） | **不安全**，多线程同时操作可能导致数据不一致 |

### 2.2 第三方 Bean

**自定义类**：通过 `@Component` 及其衍生注解（`@RestController`、`@Service`、`@Repository`）声明。

**第三方类**（依赖 jar 包中的类）：使用 `@Bean` 注解。

```java
@Configuration  // 声明该类为配置类
public class CommonConfig {

    @Bean  // 将方法返回值交给 IOC 容器管理
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // 如果第三方 Bean 需要依赖其他 Bean，直接在方法参数声明即可
    // 容器会根据类型自动装配，无需 @Autowired
    @Bean
    public SomeService someService(RestTemplate restTemplate) {
        return new SomeService(restTemplate);
    }
}
```

| 要点 | 说明 |
|------|------|
| 位置 | 启动类（不推荐）或单独 `@Configuration` 配置类（推荐） |
| Bean 名称 | 默认方法名；可通过 `@Bean(name="xxx")` 指定 |
| 依赖注入 | 方法形参声明即可，自动按类型装配 |

---

## 三、SpringBoot 原理

### 3.1 起步依赖原理

**本质**：Maven 的**传递依赖**。

- `spring-boot-starter-web` 依赖了 `spring-webmvc`、`spring-boot-starter-tomcat`、`jackson` 等
- 引入一个 starter，自动传递引入它所依赖的全部 jar 包
- 开发者无需手动管理版本兼容问题

### 3.2 自动配置

**核心问题**：第三方依赖的 `@Component` 为什么默认不生效？如何让它生效？

#### 3.2.1 问题原因

`@SpringBootApplication` 中的 `@ComponentScan` 默认扫描启动类所在包及其子包。第三方依赖的类在外部包中，扫描不到。

#### 3.2.2 两种解决方案

| 方案 | 做法 | 缺点 |
|------|------|------|
| `@ComponentScan` | 启动类上加 `@ComponentScan(basePackages={"com.tianxing","com.third"})` | 性能低，覆盖默认扫描范围 |
| `@Import` | `@Import({ThirdClass.class})` 导入指定类 | 需逐个指定，繁琐 |

`@Import` 的四种导入形式：

| 形式 | 说明 |
|------|------|
| 导入普通类 | `@Import({Xxx.class})` 直接导入 |
| 导入配置类 | `@Import({XxxConfig.class})` |
| 导入 ImportSelector 实现类 | 批量导入，`selectImports()` 返回全类名数组 |
| `@EnableXxx` 注解 | 封装了 `@Import`，第三方提供，使用者只需加 `@EnableXxx` |

#### 3.2.3 自动配置源码分析

`@SpringBootApplication` 底层包含三个核心注解：

```
@SpringBootApplication
├── @SpringBootConfiguration   → 标识当前类为配置类
├── @ComponentScan             → 组件扫描（启动类所在包及子包）
└── @EnableAutoConfiguration   → 自动配置的核心
       └── @Import({AutoConfigurationImportSelector.class})
              └── selectImports()
                    └── 读取 META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
                    └── 返回所有自动配置类全类名
```

**流程总结**：

```
@SpringBootApplication
  → @EnableAutoConfiguration
    → @Import(AutoConfigurationImportSelector.class)
      → selectImports() 读取 autoConfiguration.imports 文件
        → 返回自动配置类列表 → 交给 IOC 容器
          → @Conditional 条件装配，有选择地创建 Bean
```

#### 3.2.4 @Conditional 条件装配

并不是所有自动配置类中的 Bean 都会被创建。配置类中的 `@Bean` 方法通常配合 `@Conditional` 系注解，根据条件决定是否创建。

| 常用注解 | 条件 |
|------|------|
| `@ConditionalOnClass` | 类路径中存在指定类 |
| `@ConditionalOnMissingBean` | 容器中不存在指定 Bean |
| `@ConditionalOnProperty` | 配置文件中存在指定属性 |
| `@ConditionalOnBean` | 容器中存在指定 Bean |

### 3.3 自定义 Starter

以阿里云 OSS 为例，将 OSS 相关依赖和自动配置封装成一个 starter。

**步骤**：

| 步骤 | 操作 |
|------|------|
| 1 | 创建 `aliyun-oss-spring-boot-starter` 模块（Maven 模块，只保留 pom.xml，统一管理依赖） |
| 2 | 创建 `aliyun-oss-spring-boot-autoconfigure` 模块（Maven 模块，含 pom.xml 和 src） |
| 3 | starter 模块的 pom.xml 中引入 autoconfigure 模块依赖 |
| 4 | autoconfigure 模块中编写自动配置类，`@Bean` 配置好 OSS 相关对象 |
| 5 | 在 `META-INF/spring/xxx.imports` 中配置自动配置类的全类名 |

使用时只需引入 starter 依赖，在 yml 中配置 OSS 参数即可自动装配。

---

## 今日总结

| 模块 | 核心要点 |
|------|----------|
| 配置优先级 | 命令行 > 系统属性 > properties > yml > yaml，共 5 种 |
| Bean 作用域 | 默认 singleton（单例），启动时创建；prototype 多例，每次获取创建；`@Lazy` 延迟初始化 |
| 单例线程安全 | 无状态 Bean 安全，有状态 Bean 不安全 |
| 第三方 Bean | `@Configuration` + `@Bean` 方法声明；方法形参自动装配 |
| 起步依赖 | Maven 传递依赖，starter 统一管理版本 |
| 自动配置原理 | `@EnableAutoConfiguration` → `@Import` → `selectImports()` 读取 `.imports` 文件 → `@Conditional` 条件装配 |
| `@Import` 四种形式 | 普通类 / 配置类 / ImportSelector / `@EnableXxx` 封装 |
| 自定义 Starter | starter 管理依赖，autoconfigure 完成配置，`.imports` 声明全类名 |

