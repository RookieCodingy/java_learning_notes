
# Day15 - 分页查询、动态SQL、事务管理与文件上传

## 一、PageHelper 分页插件

### 1.1 引入依赖

```xml
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper-spring-boot-starter</artifactId>
    <version>2.1.0</version>
</dependency>
```

### 1.2 使用步骤

**第一步：Service 层调用 `PageHelper.startPage()`**

```java
@Override
public PageResult<Emp> page(EmpQueryParam empQueryParam) {
    // 1. 设置分页参数（page: 当前页, pageSize: 每页条数）
    PageHelper.startPage(empQueryParam.getPage(), empQueryParam.getPageSize());
    // 2. 执行查询
    List<Emp> empList = empMapper.list(empQueryParam);
    // 3. 强转为 Page 对象，获取分页信息
    Page<Emp> p = (Page<Emp>) empList;
    return new PageResult<>(p.getTotal(), p.getResult());
}
```

**第二步：封装分页结果（PageResult）**

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<E> {
    private Long total;   // 总记录数
    private List rows;    // 当前页数据
}
```

### 1.3 分页查询原理

PageHelper 底层通过 MyBatis 拦截器拦截 SQL 执行过程，自动改造 SQL：

**第一步：COUNT 查询**

PageHelper 自动执行一条 COUNT 语句获取总记录数：

```
原始 SQL：
  SELECT e.*, d.name deptName FROM emp e LEFT JOIN dept d ON ...

自动生成 COUNT SQL：
  SELECT COUNT(0) FROM emp e LEFT JOIN dept d ON ...
```

**第二步：追加 LIMIT**

在原始 SQL 末尾追加 `LIMIT` 子句完成分页：

```
最终执行 SQL：
  SELECT e.*, d.name deptName FROM emp e LEFT JOIN dept d ON ...
  ORDER BY e.update_time DESC
  LIMIT 0, 10     ← 由 PageHelper 自动追加
```

| 参数 | 示例（第 2 页，每页 10 条） | 说明 |
|------|------|------|
| `LIMIT offset, count` | `LIMIT 10, 10` | 跳过前 10 条，取 10 条 |
| 计算方式 | `offset = (page - 1) × pageSize` | 当前页之前的所有记录数 |

### 1.4 条件分页查询

条件分页 = 动态 SQL（按条件筛选） + 分页（LIMIT 限制数量）。两者独立工作，PageHelper 只负责追加 LIMIT。

**完整执行流程：**

```
前端请求          →  /emps?page=1&pageSize=10&name=张&gender=1
EmpQueryParam     →  { page=1, pageSize=10, name="张", gender=1 }
PageHelper        →  设置分页参数
EmpMapper.list()  →  执行动态 SQL（<if>/<where> 拼接条件）
PageHelper        →  拦截 SQL，先 COUNT 后追加 LIMIT
返回              →  PageResult{ total=50, rows=[10条数据] }
```

**实际执行的 SQL（条件筛选 + 分页）：**

```sql
-- 第一步：COUNT
SELECT COUNT(0) FROM emp e LEFT JOIN dept d ON e.dept_id = d.id
WHERE e.name LIKE '%张%' AND e.gender = 1;

-- 第二步：分页查询
SELECT e.*, d.name deptName FROM emp e LEFT JOIN dept d ON e.dept_id = d.id
WHERE e.name LIKE '%张%' AND e.gender = 1
ORDER BY e.update_time DESC
LIMIT 0, 10;
```

**Controller 完整调用链：**

```java
// Controller：直接接收 EmpQueryParam，无需单独接收 page/pageSize
@GetMapping
public Result page(EmpQueryParam empQueryParam) {
    PageResult<Emp> pageResult = empService.page(empQueryParam);
    return Result.success(pageResult);
}

// Service：startPage 设置后，紧跟的第一次查询自动分页
@Override
public PageResult<Emp> page(EmpQueryParam empQueryParam) {
    PageHelper.startPage(empQueryParam.getPage(), empQueryParam.getPageSize());
    List<Emp> empList = empMapper.list(empQueryParam);
    Page<Emp> p = (Page<Emp>) empList;
    return new PageResult<>(p.getTotal(), p.getResult());
}
```

**关键注意事项：**

| 要点 | 说明 |
|------|------|
| SQL 结尾不加分号 | 否则 PageHelper 无法追加 LIMIT |
| 只对紧跟的第一次查询生效 | `startPage()` 后的首次 Mapper 调用才被拦截 |
| 条件为空时查全部 | `<where>` 自动去除多余的 AND/关键字，无条件时返回全量分页数据 |
| 前端参数直接映射 | Spring MVC 自动将 `?page=1&pageSize=10&name=张` 绑定到 `EmpQueryParam` |

### 1.5 查询参数封装（EmpQueryParam）

参数较多时，封装到实体类中，方便维护：

```java
@Data
public class EmpQueryParam {
    private Integer page = 1;           // 默认第 1 页
    private Integer pageSize = 10;      // 默认每页 10 条
    private String name;                // 搜索姓名
    private Integer gender;             // 性别筛选
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate begin;            // 入职起始日期
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate end;              // 入职结束日期
}
```

> `@DateTimeFormat` 将前端传来的日期字符串转换为 `LocalDate`。

---

## 二、动态 SQL

SQL 语句随用户输入条件动态变化，MyBatis 通过 XML 标签实现。

### 2.1 `<if>` — 条件判断

```xml
<select id="list" resultType="com.tianxing.pojo.Emp">
    SELECT e.*, d.name deptName
    FROM emp e LEFT JOIN dept d ON e.dept_id = d.id
    <where>
        <if test="name != null and name != ''">
            e.name LIKE CONCAT('%', #{name}, '%')
        </if>
        <if test="gender != null">
            AND e.gender = #{gender}
        </if>
        <if test="begin != null">
            AND e.entry_date BETWEEN #{begin} AND #{end}
        </if>
    </where>
</select>
```

### 2.2 `<where>` — 自动处理 WHERE 关键字

| 功能 | 说明 |
|------|------|
| 动态生成 WHERE | 内部有条件时自动添加 `WHERE` 关键字 |
| 去除多余 AND/OR | 自动去掉第一条条件前面的 `AND` 或 `OR` |

### 2.3 `<foreach>` — 批量操作

用于批量插入、批量查询等场景：

```xml
<insert id="insertBatch">
    INSERT INTO emp_expr(emp_id, begin, end, company, job) VALUES
    <foreach collection="exprList" item="expr" separator=",">
        (#{expr.empId}, #{expr.begin}, #{expr.end}, #{expr.company}, #{expr.job})
    </foreach>
</insert>
```

| 属性 | 说明 |
|------|------|
| `collection` | 遍历的集合名称 |
| `item` | 每次遍历取出的元素 |
| `separator` | 每次遍历之间的分隔符 |
| `open` | 遍历开始前拼接的字符串 |
| `close` | 遍历结束后拼接的字符串 |

### 2.4 `concat()` 函数

用于拼接字符串，常用于模糊查询：

```sql
-- 等价于 '%张三%'
CONCAT('%', '张三', '%')
```

---

## 三、主键返回（@Options）

新增数据后，需要获取数据库自动生成的主键 ID（用于后续关联操作，如插入工作经历时需要 `empId`）。

```java
@Options(useGeneratedKeys = true, keyProperty = "id")
@Insert("INSERT INTO emp(username, name, gender, ...) VALUES (#{username}, #{name}, #{gender}, ...)")
void insert(Emp emp);
```

| 属性 | 说明 |
|------|------|
| `useGeneratedKeys = true` | 启用主键返回 |
| `keyProperty = "id"` | 将生成的主键值回填到实体类的 `id` 属性 |

**配合批量插入工作经历：**

```java
@Transactional
@Override
public void save(Emp emp) {
    // 1. 插入员工（主键自动回填到 emp.id）
    empMapper.insert(emp);

    // 2. 为工作经历绑定员工 ID
    List<EmpExpr> exprList = emp.getExprList();
    if (!CollectionUtils.isEmpty(exprList)) {
        exprList.forEach(expr -> expr.setEmpId(emp.getId()));
        empExprMapper.insertBatch(exprList);
    }
}
```

---

## 四、事务管理

### 4.1 事务概念

事务是一组操作的集合，要么**同时成功**，要么**同时失败**。

```sql
START TRANSACTION;   -- 开启事务
-- 执行 SQL 操作
COMMIT;              -- 提交（全部生效）
ROLLBACK;            -- 回滚（全部撤销）
```

### 4.2 Spring 声明式事务：@Transactional

```java
@Transactional
@Override
public void save(Emp emp) {
    empMapper.insert(emp);       // 插入员工
    empExprMapper.insertBatch(exprList);  // 插入工作经历
    // 任一步失败，全部回滚
}
```

| 作用位置 | 说明 |
|----------|------|
| 方法上 | 仅当前方法纳入事务 |
| 类上 | 类中所有方法纳入事务 |
| 接口上 | 实现类的所有方法纳入事务 |

> 一般加在需要多次增删改查的**业务层方法**上方。

### 4.3 rollbackFor — 指定回滚异常

`@Transactional` 默认只在 **运行时异常（RuntimeException）** 时回滚：

```java
// 所有异常都回滚
@Transactional(rollbackFor = Exception.class)
```

### 4.4 propagation — 事务传播行为

当一个事务方法被另一个事务方法调用时，决定如何控制事务。

| 属性值 | 说明 |
|--------|------|
| `REQUIRED`（默认） | 如果已存在事务就加入，没有则新建 |
| `REQUIRES_NEW` | 无论是否有事务，都新建一个独立事务 |

```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
```

### 4.5 ACID 四大特性

| 特性 | 说明 |
|------|------|
| **原子性** (Atomicity) | 事务是最小执行单元，不可再分 |
| **一致性** (Consistency) | 事务前后数据保持一致 |
| **隔离性** (Isolation) | 并发事务间相互隔离，互不干扰 |
| **持久性** (Durability) | 提交/回滚后，数据变更是永久的 |

### 4.6 事务日志配置

在 `application.yml` 中开启事务日志，便于调试：

```yaml
logging:
  level:
    org.springframework.jdbc.support.JdbcTransactionManager: debug
```

---

## 五、文件上传

### 5.1 前端表单要求

| 要求 | 示例 |
|------|------|
| method | `post` |
| enctype | `multipart/form-data` |
| 文件选择 | `<input type="file" name="file">` |

### 5.2 后端接收：MultipartFile

```java
@PostMapping("/upload")
public Result upload(String name, Integer age, MultipartFile file) throws IOException {
    log.info("上传文件：{}, {}, {}", name, age, file);
    // 保存到本地
    file.transferTo(new File("D:/images/" + file.getOriginalFilename()));
    return Result.success();
}
```

| 方法 | 说明 |
|------|------|
| `file.getOriginalFilename()` | 获取原始文件名 |
| `file.transferTo(File)` | 将文件保存到指定路径 |

### 5.3 UUID 防止文件名冲突

多个用户上传同名文件会相互覆盖。用 UUID 生成唯一文件名：

```java
String originalFilename = file.getOriginalFilename();
// 获取后缀（如 .jpg）
String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
// UUID + 后缀 → 唯一文件名
String newFileName = UUID.randomUUID().toString() + ext;
file.transferTo(new File("D:/images/" + newFileName));
```

### 5.4 文件大小限制

SpringBoot 默认最大上传文件为 **1MB**，可在 `application.yml` 中修改：

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 5MB       # 单个文件最大
      max-request-size: 10MB   # 单次请求总大小
```

---

## 六、阿里云 OSS（对象存储）

### 6.1 使用场景

文件上传后保存在应用服务器，存在以下问题：
- 占用服务器磁盘空间
- 服务器带宽有限，影响访问速度
- 不便于多服务器集群共享

**解决方案**：将文件上传到阿里云 OSS，返回云端 URL 存入数据库。

### 6.2 引入依赖

```xml
<dependency>
    <groupId>com.aliyun.oss</groupId>
    <artifactId>aliyun-sdk-oss</artifactId>
    <version>3.18.4</version>
</dependency>
```

### 6.3 集成步骤

1. 开通 OSS 服务，创建 Bucket
2. 获取 AccessKey ID 和 AccessKey Secret
3. 参照官方 SDK 编写上传代码
4. 上传成功后获取文件 URL 存入数据库

---

## 今日总结

| 模块         | 核心要点                                                                   |
| ---------- | ---------------------------------------------------------------------- |
| PageHelper | `startPage()` → 执行查询 → `Page` 强转取 total；SQL 结尾不加分号                     |
| 动态 SQL     | `<if>` 条件判断 + `<where>` 自动处理 WHERE/AND；`<foreach>` 批量操作                |
| 主键返回       | `@Options(useGeneratedKeys=true, keyProperty="id")` 获取自增 ID            |
| 事务         | `@Transactional` 声明式事务；ACID 四性；`rollbackFor` 指定回滚异常；`propagation` 传播行为 |
| 文件上传       | `MultipartFile` 接收；`transferTo()` 保存；UUID 防重名；`max-file-size` 限制大小     |
| 阿里云 OSS    | 引入 SDK → 配置密钥 → 上传文件 → 存储 URL                                          |

