
# Day12 - MyBatis 与 SpringBoot 配置文件

---

## 一、MyBatis 入门

### 1.1 什么是 MyBatis

MyBatis 是一个 **Java 持久层框架**，用于简化 JDBC 操作。它将 SQL 语句与 Java 代码解耦，通过 Mapper 接口映射 SQL，极大减少了 JDBC 模板代码（手动建立连接、拼接 SQL、解析结果集等）。

| 核心作用 | 说明 |
|----------|------|
| 持久层框架 | 专注数据库访问，不侵入业务逻辑 |
| 简化 JDBC | 自动封装连接、预编译、结果映射 |
| SQL 可控 | SQL 写在 XML 或注解中，开发灵活调优 |

### 1.2 SpringBoot 集成 MyBatis

在 SpringBoot 项目中集成 MyBatis 只需两步：

**第一步：properties 配置数据库连接**

```properties
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/db_name
spring.datasource.username=root
spring.datasource.password=123456
```

**第二步：定义 Mapper 接口**

```java
@Mapper
public interface UserMapper {
    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(Long id);
}
```

| 注解 | 用途 |
|------|------|
| `@Mapper` | 声明该接口为 MyBatis Mapper，由框架自动生成代理实现类 |
| `@Select` | 标记查询方法，值为 SQL 语句 |
| `@Insert` | 标记新增方法 |
| `@Update` | 标记修改方法 |
| `@Delete` | 标记删除方法 |

---

## 二、数据库连接池

### 2.1 什么是数据库连接池

数据库连接池是一个**管理数据库连接的容器**。应用启动时预先创建一批连接放入池中，后续请求直接复用已有连接，使用完毕后归还连接而非销毁。同时负责释放空闲超时的连接，避免数据库连接遗漏导致资源耗尽。

```
应用 → 从连接池借连接 → 执行 SQL → 归还连接 → 连接复用于下次请求
```

### 2.2 连接池的优点

| 优点 | 说明 |
|------|------|
| 资源复用 | 连接可被重复使用，避免频繁创建和销毁连接的开销 |
| 提升响应速度 | 请求到达时直接从池中获取现成连接，无需等待 TCP 握手和数据库认证 |
| 释放空闲连接 | 自动回收超过最大空闲时间的连接，防止连接泄漏 |

### 2.3 标准接口：DataSource

Java 中数据库连接池的统一接口是 `javax.sql.DataSource`，核心方法为 `getConnection()`，所有连接池实现都遵循该接口。

```java
// 从连接池获取一个连接
Connection conn = dataSource.getConnection();
// 使用完毕后归还（不是关闭物理连接）
conn.close();
```

### 2.4 常见连接池实现

| 连接池 | 说明 |
|--------|------|
| HikariCP | SpringBoot 默认连接池，性能高、轻量级 |
| Druid | 阿里巴巴开源连接池，功能丰富（监控、SQL 分析、安全防护） |

### 2.5 切换连接池

**第一步：pom.xml 引入依赖**

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid-spring-boot-starter</artifactId>
    <version>1.2.18</version>
</dependency>
```

**第二步：properties 指定连接池类型**

```properties
spring.datasource.type=com.alibaba.druid.pool.DruidDataSource
```

---

## 三、MyBatis 增删改查

### 3.1 占位符与拼接符

| 符号 | 说明 | 适用场景 |
|------|------|----------|
| `#{...}` | **占位符**：预编译 SQL，将参数替换为 `?`，防止 SQL 注入 | 绝大多数参数传递 |
| `${...}` | **拼接符**：直接将字符串拼接进 SQL，不预编译 | 表名、字段名等特殊场景 |

**`#{...}` 预编译原理：**

```java
@Select("SELECT * FROM user WHERE name = #{name}")
```

编译后实际执行的 SQL：

```sql
SELECT * FROM user WHERE name = ?
```

参数通过 `PreparedStatement` 安全传入，攻击者无法通过输入 `1' OR '1'='1` 篡改 SQL 语义。

**`${...}` 使用示例（仅限表名）：**

```java
@Select("SELECT * FROM ${tableName} WHERE id = #{id}")
```

### 3.2 新增数据

将参数值封装到对象的属性中，通过 `#{属性名}` 自动取值。

```java
@Mapper
public interface UserMapper {
    @Insert("INSERT INTO user(name, age, email) VALUES(#{name}, #{age}, #{email})")
    void insert(User user);
}
```

调用时：

```java
User user = new User();
user.setName("张三");
user.setAge(25);
user.setEmail("zhangsan@example.com");
userMapper.insert(user);
```

MyBatis 会自动从 `user` 对象中读取 `name`、`age`、`email` 属性值填入 SQL。

### 3.3 修改数据

```java
@Update("UPDATE user SET name = #{name}, age = #{age} WHERE id = #{id}")
void update(User user);
```

### 3.4 删除数据

```java
@Delete("DELETE FROM user WHERE id = #{id}")
void deleteById(Long id);
```

### 3.5 @Param 注解

为方法参数起名字，解决多参数传递时 MyBatis 无法识别的问题。

```java
@Select("SELECT * FROM user WHERE name = #{name} AND age = #{age}")
List<User> findByNameAndAge(@Param("name") String name, @Param("age") Integer age);
```

| 场景          | 是否需要 @Param                             |
| ----------- | --------------------------------------- |
| 多个方法参数      | **必须**加，Java 编译为字节码后形参名称不会保留，不加无法找到对应形参 |
| 单个参数        | 可以省略                                    |
| 基于官方骨架创建的项目 | 可省略                                     |

---

## 四、XML 映射配置

### 4.1 XML 映射文件位置

XML 映射文件必须与 Mapper 接口放在**同包同名**位置。在 `src/main/resources` 下创建时，需用 `/` 分隔目录层级而非 `.`（否则会被识别为带点的文件名）。

```
src/main/java/com/example/mapper/UserMapper.java        ← Mapper 接口
src/main/resources/com/example/mapper/UserMapper.xml     ← 映射文件
```

### 4.2 XML 文件结构

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="com.example.mapper.UserMapper">

    <select id="findById" resultType="com.example.entity.User">
        SELECT * FROM user WHERE id = #{id}
    </select>

    <insert id="insert">
        INSERT INTO user(name, age) VALUES(#{name}, #{age})
    </insert>

    <update id="update">
        UPDATE user SET name = #{name} WHERE id = #{id}
    </update>

    <delete id="deleteById">
        DELETE FROM user WHERE id = #{id}
    </delete>

</mapper>
```

| XML 标签 | 对应注解 | 属性说明 |
|----------|----------|----------|
| `<mapper namespace>` | — | Mapper 接口的全限定名 |
| `<select>` | `@Select` | `id`=方法名，`resultType`=返回值类型全限定名 |
| `<insert>` | `@Insert` | `id`=方法名 |
| `<update>` | `@Update` | `id`=方法名 |
| `<delete>` | `@Delete` | `id`=方法名 |

### 4.3 XML 辅助配置

在 `application.properties` 中指定 XML 映射文件位置：

```properties
mybatis.mapper-locations=classpath:com/example/mapper/*.xml
```

### 4.4 MybatisX 插件

IDEA 中的 MybatisX 插件（图标为小蓝鸟），可在 Mapper 接口和 XML 之间快速跳转，并根据接口方法自动生成 XML 标签，提高开发效率。

---

## 五、踩坑记录

### 5.1 SpringBoot 版本过高导致 JUnit 报错

**现象：**

```
Exception in thread "main" java.lang.NoSuchMethodError:
'java.lang.String org.junit.platform.engine.discovery.MethodSelector.getMethodParameterTypes()'
```

**原因：** IDEA 版本较低，SpringBoot 版本过高对应的 JUnit 版本不兼容。直接修改 JUnit 版本无法解决。

**解决方案：** 降低 `spring-boot-starter-parent` 版本号，使其匹配当前 IDE 环境。

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.7.x</version>   <!-- 从 3.x 降级 -->
</parent>
```

### 5.2 Lombok 注解不生效

**现象：** `@Data`、`@AllArgsConstructor` 等 Lombok 注解不生效，编译时报找不到 getter/setter/构造方法。

**原因：** 一般是版本兼容性问题，Lombok 与 IDE 或编译器版本不匹配。

**解决方案：** 手动编写构造函数和 `toString()` 方法，绕过 Lombok 问题/修改Lombok版本。

```java
public class User {
    private String name;
    private Integer age;

    public User() {}

    public User(String name, Integer age) {
        this.name = name;
        this.age = age;
    }

    // getter / setter ...

    @Override
    public String toString() {
        return "User{name='" + name + "', age=" + age + "}";
    }
}
```

---

## 六、SpringBoot 配置文件

### 6.1 properties 格式

```properties
# application.properties
server.port=8080
spring.datasource.username=root
```

语法：`key=value`，每行一个配置项。

### 6.2 YAML 格式

```yaml
# application.yml 或 application.yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/db
    username: root
    password: 123456
```

### 6.3 YAML 语法规则

| 规则 | 说明 |
|------|------|
| 冒号后必须有空格 | `key: value`（`key:value` 非法） |
| 缩进表示层级关系 | 子级缩进 2 格（或任意一致的空格数） |
| 禁止使用 Tab | 缩进只能用空格（IDEA 会自动将 Tab 转空格） |
| 同级左对齐 | 同一层级的属性必须对齐 |
| `#` 开头表示注释 | `# 这是注释` |

### 6.4 YAML 数据类型写法

**对象 / Map：**

```yaml
user:
  name: 张三
  age: 18
  password: 123456
```

**数组 / List / Set：**

```yaml
hobby:
  - java
  - game
  - sport
```

**以 0 开头的值：**

```yaml
# 必须用单引号包裹，否则会按八进制处理
code: '010'
```

### 6.5 YAML vs properties

| 对比项 | properties | YAML |
|--------|------------|------|
| 格式 | `key=value` | 缩进层级 |
| 层级表达 | 用 `.` 连接（如 `spring.datasource.url`） | 用缩进 |
| 可读性 | 扁平，深层嵌套时冗长 | 层级清晰，一目了然 |
| 设计理念 | 以键值对为中心 | **以数据为中心**，更简洁 |
| 数组支持 | 需额外语法 | 原生 `-` 语法 |

---

## 今日总结

| 模块 | 核心要点 |
|------|----------|
| MyBatis 入门 | Java 持久层框架，简化 JDBC；Mapper + @Select/@Insert/@Update/@Delete |
| 数据库连接池 | 复用连接、提升响应、释放空闲超时连接；DataSource 接口；HikariCP（默认）/ Druid |
| MyBatis 增删改查 | `#{}` 预编译防注入 / `${}` 拼接（表名字段名用）；@Param 多参数必需 |
| XML 映射配置 | 同包同名（resources 下 / 创建）；namespace + 标签 id + resultType；MybatisX 插件 |
| 踩坑记录 | SpringBoot 版本降级解决 JUnit 兼容性；Lombok 不生效手动写构造器和 toString |
| SpringBoot 配置文件 | properties（key=value）vs YAML（缩进层级，以数据为中心，更简洁）；YAML 语法规则 |

