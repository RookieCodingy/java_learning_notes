
# Day16 - 阿里云OSS、员工管理完善与全局异常处理

## 一、阿里云OSS文件上传

### 1.1 OSS简介

阿里云对象存储服务（Object Storage Service），用于存储任意类型的文件。笔记使用 **OSS SDK V2** 版本

**核心概念**：

| 概念 | 说明 | 示例 |
|------|------|------|
| Bucket | 存储空间，相当于根目录 | `java-web-learning-tianxing` |
| Region | 数据中心区域 | `cn-beijing` |
| Endpoint | 访问域名 | `https://oss-cn-beijing.aliyuncs.com` |
| AccessKey | 身份认证凭证 | 通过环境变量设置 |

### 1.2 @Value 与 @ConfigurationProperties

| 注解 | 用途 | 适用场景 |
|------|------|----------|
| `@Value("${aliyun.oss.endpoint}")` | 逐个注入 yml 属性 | 零散属性 |
| `@ConfigurationProperties(prefix = "aliyun.oss")` | 批量绑定同前缀属性到实体类 | **推荐**，配置项多时更清晰 |

**application.yml**

```yaml
aliyun:
  oss:
    endpoint: https://oss-cn-beijing.aliyuncs.com
    bucketName: java-web-learning-tianxing
    region: cn-beijing
```

**AliyunOSSProperties.java**

```java
@Data
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class AliyunOSSProperties {
    private String endpoint;
    private String bucketName;
    private String region;
}
```

- `@Component` 声明为 Bean，其他类通过 `@Autowired` 注入
- 属性名自动映射驼峰 ↔ 短横线

### 1.3 AliyunOSSOperator 工具类

封装 OSS 上传逻辑，对外暴露 `upload(byte[] content, String originalFilename)` 方法：

```java
@Component
public class AliyunOSSOperator {

    @Autowired
    private AliyunOSSProperties aliyunOSSProperties;

    public String upload(byte[] content, String originalFilename) throws Exception {
        // 凭证：从环境变量读取 AccessKey
        EnvironmentVariableCredentialsProvider credentialsProvider =
            new EnvironmentVariableCredentialsProvider();

        // 生成存储路径：yyyy/MM/UUID.扩展名
        String dir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String newFileName = UUID.randomUUID() +
            originalFilename.substring(originalFilename.lastIndexOf("."));
        String objectName = dir + "/" + newFileName;

        // 上传
        try (OSSClient ossClient = OSSClient.newBuilder()
                .credentialsProvider(credentialsProvider)
                .region(aliyunOSSProperties.getRegion())
                .build()) {
            ossClient.putObject(PutObjectRequest.newBuilder()
                    .bucket(aliyunOSSProperties.getBucketName())
                    .key(objectName)
                    .body(BinaryData.fromBytes(content))
                    .build());
        }

        // 拼接访问 URL
        return aliyunOSSProperties.getEndpoint().split("//")[0] + "//"
            + aliyunOSSProperties.getRegion() + "."
            + aliyunOSSProperties.getEndpoint().split("//")[1]
            + "/" + objectName;
    }
}
```

| 要点 | 说明 |
|------|------|
| UUID 防重名 | 生成唯一文件名，避免覆盖 |
| 日期分目录 | `yyyy/MM` 格式按月份归档 |
| try-with-resources | 自动关闭 OSSClient 连接 |
| URL 拼接 | `https://{region}.{endpoint域名}/{objectName}` |

### 1.4 UploadController 上传接口

```java
@PostMapping("/upload")
public Result upload(MultipartFile file) throws Exception {
    if (!file.isEmpty()) {
        String originalFilename = file.getOriginalFilename();
        String extName = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFileName = UUID.randomUUID().toString().replace("-", "") + extName;
        String url = aliyunOSSOperator.upload(file.getBytes(), uniqueFileName);
        return Result.success(url);  // 返回云端 URL
    }
    return Result.error("上传失败");
}
```

- `MultipartFile` 接收前端文件 → `file.getBytes()` 获取字节数组 → 返回 OSS 访问 URL

---

## 二、删除员工

### 2.1 Controller

```java
@DeleteMapping
public Result delete(@RequestParam List<Integer> ids) {
    empService.delete(ids);
    return Result.success();
}
```

- `@RequestParam List<Integer> ids`：接收前端传来的 ID 列表，支持批量删除

### 2.2 Service

```java
public void delete(List<Integer> ids) {
    empMapper.delete(ids);       // 删除主表 emp
    empExprMapper.delete(ids);   // 删除关联表 emp_expr
}
```

- 先删主表再删从表；生产环境建议加 `@Transactional`

### 2.3 Mapper：foreach 批量删除

```xml
<delete id="delete">
    delete from emp where id in
    <foreach collection="ids" item="id" separator="," open="(" close=")">
        #{id}
    </foreach>
</delete>
```

| `<foreach>` 属性 | 说明 |
|------|------|
| `collection` | 遍历的集合参数名 |
| `item` | 每次迭代变量名 |
| `separator` | 元素间分隔符 |
| `open` / `close` | 循环体首尾字符 |

---

## 三、修改员工

### 3.1 查询回显——resultMap

修改前需查询员工详情回显到表单。一个员工关联多条工作经历（一对多），且联查字段名与实体属性名不完全一致，需用 `resultMap` 手动映射。

```xml
<select id="getById" resultMap="empExprMap">
    select e.*,
           d.name     as dept_name,
           ex.id      as expr_id,
           ex.begin   as expr_begin,
           ex.end     as expr_end,
           ex.company as expr_company,
           ex.job     as expr_job
    from emp e
        left join dept d on e.dept_id = d.id
        left join emp_expr ex on ex.emp_id = e.id
    where e.id = #{id}
</select>
```

**resultMap 定义：**

```xml
<resultMap id="empExprMap" type="com.tianxing.pojo.Emp">
    <id property="id" column="id"/>
    <result property="username" column="username"/>
    <!-- ... 其他普通字段省略 ... -->
    <result property="deptName" column="dept_name"/>
    <!-- collection：一对多集合 -->
    <collection property="exprList" ofType="com.tianxing.pojo.EmpExpr">
        <id property="id" column="expr_id"/>
        <result property="begin" column="expr_begin"/>
        <result property="end" column="expr_end"/>
        <result property="company" column="expr_company"/>
        <result property="job" column="expr_job"/>
    </collection>
</resultMap>
```

| 标签 | 映射对象 | 场景 |
|------|----------|------|
| `<id>` | 主键字段 | 提升缓存效率 |
| `<result>` | 普通字段 | `property`=实体属性，`column`=列名 |
| `<collection>` | 一对多集合 | `property`=集合属性名，`ofType`=元素类型 |

### 3.2 更新策略

```java
@Transactional
public void update(Emp emp) {
    emp.setUpdateTime(LocalDateTime.now());
    empMapper.update(emp);                          // 1. 更新基本信息

    empExprMapper.delete(List.of(emp.getId()));      // 2. 删除旧工作经历

    List<EmpExpr> exprList = emp.getExprList();
    if (!CollectionUtils.isEmpty(exprList)) {
        exprList.forEach(e -> e.setEmpId(emp.getId()));
        empExprMapper.insertBatch(exprList);         // 3. 插入新工作经历
    }
}
```

- "先删再插"模式：比逐一比对差异更简洁可靠
- `@Transactional` 保证三步原子操作

### 3.3 `<set>` 动态更新

```xml
<update id="update">
    update emp
    <set>
        <if test="username != null and username != ''">username = #{username},</if>
        <if test="name != null and name != ''">name = #{name},</if>
        <!-- ... 其他字段类似 ... -->
        <if test="updateTime != null">update_time = #{updateTime},</if>
    </set>
    where id = #{id}
</update>
```

| `<set>` 特性 | 说明 |
|------|------|
| 自动加 SET | 在内容前添加 `SET` 关键字 |
| 去除尾逗号 | 自动处理拼接后的多余逗号 |
| 空字段保持原值 | 只有 `<if test>` 通过的字段才更新 |

---

## 四、全局异常处理器

项目中各层可能抛出异常，避免每个 Controller 方法写 try-catch，用全局异常处理器统一捕获并返回错误信息。

```java
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    public Result handleException(Exception e) {
        log.error("服务器发生异常：{}", e.getMessage());
        return Result.error("出错了，请联系管理员");
    }
}
```

| 注解 | 作用 |
|------|------|
| `@RestControllerAdvice` | 全局异常处理类，= `@ControllerAdvice` + `@ResponseBody` |
| `@ExceptionHandler` | 异常处理方法，默认捕获所有 Exception 及其子类 |

**流程**：Controller → Service → Mapper 抛出异常 → 向上传播 → 被 `@ExceptionHandler` 拦截 → 返回 `Result.error()`

- 可按异常类型细化处理（如 `@ExceptionHandler(SQLException.class)`）
- `log.error` 记录内部细节，返回给前端的 message 不暴露敏感信息

---

## 五、员工信息统计

### 5.1 职位人数统计——CASE 函数

```xml
<select id="countEmpJobData" resultType="java.util.Map">
    select
        case job
            when 1 then '班主任'
            when 2 then '讲师'
            when 3 then '学工主管'
            when 4 then '教研主管'
            when 5 then '咨询师'
            else '未知'
        end as jobName,
        count(*) as jobCount
    from emp
    group by job
</select>
```

**CASE 两种语法：**

| 形式 | 语法 | 示例 |
|------|------|------|
| 等值判断 | `case 字段 when 值1 then 结果1 else 默认 end` | 职位编号 → 职位名 |
| 条件判断 | `case when 条件1 then 结果1 else 默认 end` | 分数 ≥ 90 → '优秀' |

Service 层将查询结果拆分为两个 List（适配 ECharts 图表）：

```java
List<String> jobList = new ArrayList<>();  // X轴：职位名
List<Long> dataList = new ArrayList<>();   // Y轴：对应人数
return new JobOption(jobList, dataList);
```

### 5.2 性别人数统计——IF函数 + @MapKey

```xml
<select id="countEmpGenderData" resultType="java.util.Map">
    select if(gender = 1, '男', '女') as name, count(*) as value
    from emp
    group by gender
</select>
```

- `IF(条件, true值, false值)`：相当于三元表达式
- `@MapKey("name")`：以 `name` 字段作为返回 Map 的 key，将列表转为 `{"男": {...}, "女": {...}}` 结构

---

## 今日总结

| 模块      | 核心要点                                                                                                      |
| ------- | --------------------------------------------------------------------------------------------------------- |
| 阿里云 OSS | OSS SDK V2；`@ConfigurationProperties` 批量绑定配置；AliyunOSSOperator 封装上传（UUID+日期分目录）；UploadController 返回云端 URL |
| 删除员工    | `@RequestParam List<Integer>` 批量删除；`<foreach>` 动态 IN 子句；主表+从表同步删除                                         |
| 修改员工    | `resultMap` 手动映射（id/result/collection）；三表联查 getById 回显；`<set>` 动态更新；"先删再插"处理一对多关联                         |
| 全局异常处理  | `@RestControllerAdvice` + `@ExceptionHandler` 统一拦截异常，返回 `Result.error()`                                  |
| 员工统计    | CASE 函数（等值/条件两种语法）；IF 函数；`@MapKey` 注解；ECharts 双轴数据拆分                                                      |

