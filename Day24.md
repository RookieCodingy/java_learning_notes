
# Day24 - 苍穹外卖（公共字段填充与AOP）

## 一、BeanUtils 属性拷贝

调用 Spring 提供的 `BeanUtils.copyProperties` 方法，可以将一个对象的同名属性值拷贝到另一个对象，避免手动逐个 `set`。


**使用场景**：DTO（请求数据）→ Entity（数据库实体）之间的属性批量拷贝，如新增员工、修改员工。

**注意事项**：`copyProperties` 只拷贝**同名同类型**的属性；目标对象中存在、源对象中不存在的属性不会被影响。

## 二、todo 注释

代码中的 `TODO` 注释用于标记待办事项，表示该处逻辑尚未完成或后续需要补充处理，方便开发时定位未完成的工作。

```java
// TODO 后续补充实现
```

## 三、ThreadLocal - 线程局部变量

### 3.1 概念

`ThreadLocal` 是线程的局部变量，为每一个线程提供一个独立的存储空间，实现**线程隔离**——不同线程读写同一个 `ThreadLocal` 时，各自操作的是自己线程的副本，互不干扰。

### 3.2 源码示例（苍穹外卖 BaseContext）

项目中使用 `ThreadLocal` 封装了一个工具类，用于保存当前登录员工 id：

```java
public class BaseContext {

    public static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    public static Long getCurrentId() {
        return threadLocal.get();
    }

    public static void removeCurrentId() {
        threadLocal.remove();
    }
}
```

**使用流程**：登录拦截器解析 JWT 拿到员工 id 后 `setCurrentId(empId)` 存入；业务层需要操作人 id 时 `getCurrentId()` 取出。

**注意**：使用完建议调用 `remove()` 清理，避免内存泄漏（线程池复用线程时数据残留）。

## 四、SpringMVC 消息转换器

### 4.1 概念

SpringMVC 消息转换器（`HttpMessageConverter`）负责 Java 对象与 JSON 数据之间的相互转换，如 `@RequestBody` 接收 JSON 请求体并转为 Java 对象、返回结果时把 Java 对象转为 JSON 响应给前端。

### 4.2 自定义消息转换器

当自带的转换器难以满足项目要求时，可以自定义转换器。比如项目要求 `LocalDateTime` 等日期类型按 `yyyy-MM-dd HH:mm` 格式序列化/反序列化，默认转换器无法满足。

扩展 SpringMVC 框架消息转换器（WebMvcConfiguration）

在配置类中重写 `extendMessageConverters`，把自定义转换器添加到容器首位：

```java
/**
 * 扩展springMVC框架消息转换器
 */
protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    log.info("开始注册自定义消息转换器...");
    //创建一个消息转换器
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
    //为消息转换器设置一个对象转换器
    converter.setObjectMapper(new JacksonObjectMapper());
    //将自己的消息转换器添加进容器
    converters.add(0, converter);
}
```

**说明**：将自定义消息转换器添加到列表首位（`add(0, converter)`），确保优先使用自定义转换规则。

## 五、Builder 创建器

Builder 模式通过链式调用替代 `new` 对象后逐个 `set`，代码更简洁、可读性更强。

### 5.1 源码示例

员工启用/禁用：

```java
Employee employee = Employee.builder()
        .id(id)
        .status(status)
        .build();
```

登录成功后构建返回给前端的 VO：

```java
EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
        .id(employee.getId())
        .userName(employee.getUsername())
        .name(employee.getName())
        .token(token)
        .build();
```

## 六、公共字段填充

### 6.1 需求背景

每次新增/修改操作时，都要手动为 `create_time`、`create_user`、`update_time`、`update_user` 等公共字段赋值，代码重复且易遗漏。通过 **AOP + 自定义注解 + 切面类** 实现公共字段自动填充。

### 6.2 实现思路

```
AOP + 自定义注解 + 切面类
```

- **自定义注解**：标识哪些 Mapper 方法需要自动填充，并指定操作类型（INSERT / UPDATE）
- **切面类**：定义切入点（拦截带注解的 Mapper 方法），通过反射为实体对象的公共字段赋值
- **AOP 通知**：使用前置通知 `@Before`，在执行目标方法前完成赋值

### 6.3 自定义注解 AutoFill

```java
/**
 * 自定义注解，用于标识某个方法需要进行自动填充
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {
    //数据库操作类型UPDATE,INSERT
    OperationType value();
}
```

### 6.4 切面类 AutoFillAspect

```java
/**
 * 自定义切面类，实现公共字段填充
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    /**
     * 切入点：拦截 com.sky.mapper 包下带 @AutoFill 注解的方法
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){}

    /**
     * 前置通知，在通知中进行公共字段赋值
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("进行公共字段赋值");

        //获取到当前被拦截方法上的数据库操作类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();//获取方法签名
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);//获得方法上的注解对象
        OperationType operationType = autoFill.value();//获得数据库操作类型

        //获取到当前被拦截方法上的实体对象
        Object[] args = joinPoint.getArgs();//获取方法参数
        if(args==null || args.length==0){
            return;
        }

        Object entity = args[0];
        //准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId= BaseContext.getCurrentId();

        //根据不同操作类型为对应属性赋值
        if(operationType==OperationType.INSERT){
            //新增：填充 create_time、create_user、update_time、update_user
            try {
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                setCreateTime.invoke(entity, now);
                setCreateUser.invoke(entity, currentId);
                setUpdateUser.invoke(entity, currentId);
                setUpdateTime.invoke(entity, now);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }else{
            //修改：只填充 update_time、update_user
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
```

**补充说明**：
- `AutoFillConstant` 中定义了 setter 方法名的常量（如 `SET_CREATE_TIME = "setCreateTime"`）
- `BaseContext.getCurrentId()` 从 ThreadLocal 中取出当前登录用户 id
- 有了公共字段填充后，`EmployeeServiceImpl` 中手动赋值 `createTime` / `createUser` 等代码即可注释移除（见源码中已注释的 `setCreateTime`、`setCreateUser` 等行）

## 七、方法签名与反射

### 7.1 方法签名

方法签名包含：

| 组成 | 说明 |
|------|------|
| 方法名 | 方法的名字 |
| 参数列表 | 方法接收的参数类型与顺序 |
| 返回值类型 | 方法返回的数据类型 |
| 修饰符 | 如 public / private / static 等 |
| 声明它的类 | 方法所属的类 |

### 7.2 反射

**反射**就是程序在运行时**动态查看和操作自身结构**的能力。

通过创建 `Class` 对象，对 `Class` 对象进行操作——可以获取类的属性、方法、构造器，并在运行时调用它们。

**典型应用**：切面类中通过 `joinPoint.getSignature()` 获取方法签名，再用 `entity.getClass().getDeclaredMethod(...)` 获取实体类的 setter 方法并 `invoke` 调用，实现公共字段的动态赋值。

## 八、一些思考

> 以下内容附上解答和纠正，不要更改我的问题

**Q1：一些注解是不是也是基于这样的AOP思路进行实现？**

是的。许多框架注解（如 Spring 的 `@Transactional`、`@Cacheable`）正是基于 AOP（动态代理）实现的：通过切面在方法执行前后织入增强逻辑。自定义注解 + 切面类本质就是使用 AOP 机制，让"注解声明 + 切面统一处理"代替重复编码。

**Q2：原来是springMVC让前后端参数接收更加方便？**

是的。SpringMVC 通过消息转换器（`HttpMessageConverter`）把前端提交的 JSON 自动反序列化为 Java 对象（`@RequestBody`），也把后端返回的 Java 对象自动序列化为 JSON，让前后端参数接收与数据返回变得非常方便。

**Q3：jwt令牌技术是不是也是springmvc的一种实现？**

不是。JWT 令牌是一种独立的认证技术（基于 JSON 的开放标准），由 `JwtUtil` 工具类负责生成与解析（HS256 算法 + 密钥），本身不依赖 SpringMVC。在项目中，JWT 校验是通过 SpringMVC 的**拦截器**（`HandlerInterceptor`，如 `JwtTokenAdminInterceptor`）接入请求链路的：拦截 `/admin/**` 请求 → 解析校验令牌 → 通过则放行并把员工 id 存入 ThreadLocal，失败则返回 401。即：JWT 是技术本身，SpringMVC 拦截器是它在 Web 层的接入载体。

## 今日总结

| 知识点             | 要点说明                                                         |
| --------------- | ------------------------------------------------------------ |
| BeanUtils 属性拷贝  | copyProperties 拷贝对象同名属性，DTO → Entity 常用                      |
| todo 注释         | 标记代码中待办事项                                                    |
| ThreadLocal     | 线程局部变量，线程隔离；BaseContext 存当前用户 id                             |
| SpringMVC 消息转换器 | Java 对象与 JSON 互转；自定义 JacksonObjectMapper 处理 LocalDateTime 格式 |
| Builder 创建器     | 链式调用替代 new + set，如 Employee.builder().build()                |
| 公共字段填充          | AOP + 自定义注解 + 切面类，自动填充 create/update 时间与用户                   |
| 方法签名            | 方法名、参数列表、返回值类型、修饰符、声明类                                       |
| 反射              | 运行时动态查看和操作自身结构，通过 Class 对象操作                                 |
