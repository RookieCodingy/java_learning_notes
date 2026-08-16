# Day25 - 苍穹外卖Day03：菜品模块增删改查与阿里云OSS

## 一、新增菜品

**技术要点**

**阿里云 OSS**：新增菜品的图片上传走 `CommonController.upload` → `AliOssUtil.upload` 链路，UUID 重命名后用 `putObject` 上传，返回的 URL 存入 dish 表的 image 字段，工具类由 `OssConfiguration` 注入、配置由 `AliOssProperties` 读取 `sky.alioss`。

**一个 ServiceImpl 操作两张表**：`DishServiceImpl.saveWithFlavor` 同时写 dish 与 dish_flavor 两张表，方法上的 `@Transactional` 保证主表插入失败或口味批量插入失败时整体回滚，不留脏数据。

**主键回显**：插入主表后需立即拿到自增 id 给口味表用，`DishMapper.xml` 的 `<insert useGeneratedKeys="true" keyProperty="id">` 会把主键写回 `Dish.id`，与之前学过的 `@Options(useGeneratedKeys = true, keyProperty = "id")` 注解方式等价。

## 二、阿里云 OSS 遇到的问题

阿里云oss遇到的问题：
因为之前我看官网文档学的是oss v2版本，项目里用的是老版本，我想着能不能自己将新版oss配置进去，但是配置过程中遇到很多问题:

1. 根pom文件与子pom文件都添加同一依赖，但根pom文件中未指定version，子pom文件中也没有添加version，导致子模块编译失败
   - 解决方案：将根pom文件中的依赖删去，留下子pom文件中的依赖，子pom文件中的依赖虽然未指定version，但会自动找到合适版本

（纠正与优化：更规范的做法是**依赖统一由父 pom 的 `dependencyManagement` 管理版本**，子模块只在自身 `dependencies` 中声明坐标（不写 version）。这样既避免父 pom 的 dependencies 被子模块无条件继承造成重复依赖，又能统一管理版本号。若直接依赖父 pom 的 dependencies，子模块会继承全部依赖，且无 version 时 Maven 无法解析。）

2. 所有配置完成过后，启动springboot失败，看报错应该是springboot版本兼容问题，问ai的解答是knife4j与boot版本冲突，但是我并没有修改其他依赖，只添加了oss的apche和oss本身两个依赖，为什么会引起它们的冲突？用ai修改了很久都没有改好，最后放弃使用新版本

（纠正与优化：新版 OSS SDK（v3）依赖的 `aliyun-java-sdk-core` / HTTPClient 等传递依赖版本较新，可能覆盖项目原有依赖的版本号，从而引发与 knife4j（其底层 springfox/swagger 对 Spring Boot 版本敏感）的兼容冲突。排查思路：① 用 `mvn dependency:tree` 查看冲突依赖；② 对冲突依赖使用 `exclusions` 排除；③ 优先选择与项目 Spring Boot 版本匹配的 OSS SDK 版本。放弃使用新版本、沿用项目配套的老版本 SDK 也是稳妥的取舍。）

3. 老师使用的是明文存储key和secret，可以在这里进行优化将key和secret存储到环境变量中，通过调用环境变量，保证信息的安全性

（优化说明：该优化思路正确。实际做法是在 `application.yml` 中不写死 key/secret，而是引用环境变量，例如：`access-key-id: ${ALIYUN_OSS_ACCESS_KEY_ID}`、`access-key-secret: ${ALIYUN_OSS_ACCESS_KEY_SECRET}`，由 `AliOssProperties` 读取。也可使用 Spring 配置中心或密钥管理服务，切勿将 AccessKey 提交到 Git 仓库。）

## 三、菜品分页查询

**技术要点**

```xml
<select id="pageQuery" resultType="com.sky.entity.Dish">
    select d.*, c.name as categoryName from dish d
    left outer join category c on d.category_id = c.id
    <where>
        <if test="name != null"> and d.name like CONCAT('%', #{name}, '%')</if>
        <if test="categoryId != null"> and d.category_id = #{categoryId}</if>
        <if test="status != null"> and d.status = #{status}</if>
    </where>
    order by d.create_time desc
</select>
```

**左外连接与逻辑外键**：dish 与 category 之间没有物理外键约束，靠 category_id 在查询时 `left outer join` 关联并 `c.name as categoryName` 带出分类名，这就是逻辑外键的多表查询用法，左外连接保证即使分类缺失也能查出菜品。

**动态条件与分页**：`DishServiceImpl.page` 先用 `PageHelper.startPage(page, pageSize)` 开启分页，再执行 `pageQuery` 自动拼 limit 并返回 total；xml 中 `<where>` + `<if>` 根据 name/categoryId/status 动态拼接条件，`CONCAT('%', #{name}, '%')` 实现模糊查询且预编译防注入。

## 四、删除菜品

**技术要点**

```xml
<delete id="deleteByIds">
    delete from dish where id in
    <foreach item="id" collection="ids" separator="," open="(" close=")">
        #{id}
    </foreach>
</delete>
```

**删除条件判断**：`DishServiceImpl.delete` 先逐条校验菜品是否起售（起售中抛 `DISH_ON_SALE`），再通过 `SetMealDishMapper.getSetmealIdsByDishIds` 校验是否被套餐关联（关联则抛 `DISH_BE_RELATED_BY_SETMEAL`），校验通过才删除。

**ids 数据的处理**：Controller 用 `@RequestParam List<Long> ids` 一次性接收多个 id，Mapper 用 `<foreach>` 展开成 `id in (?, ?, ...)` 批量删除，避免逐条拼接 SQL。

**Service 层优化**：注释掉的旧代码是 for 循环逐条 `delete(id)`，优化后 `dishMapper.deleteByIds(ids)` 与 `dishFlavorMapper.deleteByDishIds(ids)` 各一次完成主表与口味表删除，数据库往返次数从 2N 降到 2。

**减少 for 循环的动态 SQL**：`<foreach collection="ids" item="id" separator="," open="(" close=")">` 把 Java 里的循环下推到 SQL 中一次执行，配合预编译 `#{id}` 既高效又防注入。

## 五、修改菜品

**技术要点**

**多表操作**：`DishServiceImpl.updateWithFlavor` 先 `dishMapper.update` 更新主表（xml 用 `<set>` + `<if>` 只更新非空字段），再 `deleteByDishId` 删除旧口味、`insertBatch` 重新插入新口味，方法上加 `@Transactional` 与 `@AutoFill(OperationType.UPDATE)` 保证事务一致并自动填充 update_time/update_user。

## 今日总结

| 模块 | 核心知识点 | 关键实现 |
| --- | --- | --- |
| 新增菜品 | 一个 ServiceImpl 操作多张表需事务管理；主键回显的 XML 实现 | `@Transactional`；`useGeneratedKeys=true keyProperty="id"` |
| 阿里云 OSS | 文件上传链路；新老版本 SDK 兼容；密钥安全 | `CommonController.upload` -> `AliOssUtil`；环境变量存储密钥 |
| 菜品分页查询 | 左外连接 + 逻辑外键；PageHelper 分页；动态条件 | `left outer join category`；`<where>` + `<if>`；`CONCAT` 防注入 |
| 删除菜品 | 删除前状态/关联校验；ids 集合处理；动态 SQL 优化 | `<foreach>` 批量 in 删除；减少 for 循环 |
| 修改菜品 | 多表更新；先删后插口味；动态 set | `<set>` + `<if>`；`deleteByDishId` + `insertBatch` |
