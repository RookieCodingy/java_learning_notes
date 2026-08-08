
# Day19 - 后端Web进阶(AOP)

## 一、AOP 基础

### 1.1 什么是 AOP

**AOP（Aspect Oriented Programming）**：面向切面编程 / 面向方面编程，即**面向特定方法编程**。

**为什么需要 AOP**：项目中多个业务方法存在相同的共性功能（如统计耗时、记录日志、权限控制），如果逐个方法添加相同代码，会导致：

- 代码重复、冗余
- 维护困难（修改共性逻辑需要改所有方法）

AOP 的思路：将共性功能抽取到一个切面类中，通过配置指定应用到哪些方法上，无需修改原业务代码。

### 1.2 AOP 快速入门

**引入依赖**：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

**编写 AOP 程序**：

```java
@Slf4j
@Component
@Aspect
public class TimeAspect {

    @Around("execution(* com.tianxing.service.*.*(..))")  // 切入点表达式
    public Object recordTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long begin = System.currentTimeMillis();            // 1. 记录开始时间
        Object result = joinPoint.proceed();                // 2. 调用原始方法
        long end = System.currentTimeMillis();              // 3. 记录结束时间
        log.info("{} 方法耗时: {}ms", joinPoint.getSignature(), end - begin);
        return result;                                      // 4. 返回原始方法结果
    }
}
```

| 步骤 | 说明 |
|------|------|
| `@Aspect` | 声明该类为切面类 |
| `@Component` | 交给 IOC 容器管理 |
| `@Around` | 环绕通知，指定切入点表达式 |
| `ProceedingJoinPoint` | 连接点对象，`proceed()` 调用原始方法 |

### 1.3 AOP 应用场景

| 场景 | 说明 |
|------|------|
| 记录操作日志 | 自动记录谁在什么时间调用了什么方法 |
| 事务管理 | Spring 事务 `@Transactional` 底层就是 AOP |
| 权限控制 | 拦截方法调用，校验用户权限 |

### 1.4 AOP 执行流程

底层基于**动态代理**：Spring 为目标对象生成代理对象，调用代理对象的方法时，先执行切面逻辑，再调用目标方法。

```
调用者 → 代理对象 → 切面通知(前) → 目标方法 → 切面通知(后) → 返回结果
```

### 1.5 AOP 核心概念

| 概念 | 说明 |
|------|------|
| **连接点 JoinPoint** | 可以被 AOP 控制的方法（所有方法都是候选） |
| **切入点 PointCut** | 实际被 AOP 控制的方法（通过表达式筛选出的方法） |
| **通知 Advice** | 抽取出来的共性功能方法 |
| **切面 Aspect** | 通知 + 切入点的对应关系（即切面类） |
| **目标对象 Target** | 通知所应用的对象 |

**`@Pointcut` 抽取公共切入点**：

```java
@Aspect
@Component
public class MyAspect {
    // 抽取公共切入点表达式
    @Pointcut("execution(* com.tianxing.service.*.*(..))")
    public void servicePt() {}

    @Around("servicePt()")   // 直接引用方法名
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // ...
    }

    @Before("servicePt()")
    public void before() {
        // ...
    }
}
```

---

## 二、AOP 进阶

### 2.1 通知类型（5 种）

根据目标方法执行的**不同时机**，分为 5 种通知：

| 通知类型 | 执行时机 | 是否需手动调用目标方法 | 异常时是否执行 |
|------|------|------|------|
| `@Around` | 目标方法前后都执行 | 需手动调用 `proceed()` | 环绕控制 |
| `@Before` | 目标方法执行前 | 否 | 始终执行 |
| `@After` | 目标方法执行后（最终） | 否 | 始终执行（类似 finally） |
| `@AfterReturning` | 目标方法正常返回后 | 否 | 异常时不执行 |
| `@AfterThrowing` | 目标方法抛出异常后 | 否 | 仅异常时执行 |

**`@Around` 环绕通知代码模板**：

```java
@Around("execution(* com.tianxing.service.*.*(..))")
public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
    // 目标方法执行前...
    Object result = joinPoint.proceed();  // 必须手动调用！
    // 目标方法执行后...
    return result;
}
```

### 2.2 通知顺序

**默认顺序**：同类型通知按切面类名字母排序。

- 目标方法**之前**的通知：字母靠前的**先执行**
- 目标方法**之后**的通知：字母靠前的**后执行**（类似栈，先进后出）

**`@Order` 控制顺序**：

```java
@Aspect
@Component
@Order(1)  // 数字越小优先级越高
public class LogAspect { ... }
```

| 阶段 | @Order 小的 | @Order 大的 |
|------|------|------|
| 目标方法前 | 先执行 | 后执行 |
| 目标方法后 | 后执行 | 先执行 |

### 2.3 切入点表达式

**作用**：决定项目中哪些方法需要加入通知。

#### 2.3.1 execution — 按方法签名匹配

```
execution(访问修饰符? 返回值 包名.类名.方法名(参数类型) throws 异常?)
```

| 通配符 | 含义 |
|------|------|
| `*` | 匹配单个任意字符（包名 / 类名 / 方法名 / 返回值 / 一个参数） |
| `..` | 匹配多层路径（包）或任意参数（参数列表） |

| 表达式示例 | 匹配范围 |
|------|------|
| `execution(* com.tianxing.service.*.*(..))` | service 包下所有类的所有方法 |
| `execution(* com.tianxing.service.impl.*.delete*(..))` | service.impl 包下所有以 delete 开头的方法 |
| `execution(* com.tianxing..*(..))` | com.tianxing 包及所有子包的全部方法 |

#### 2.3.2 @annotation — 按注解匹配

用于匹配标识了特定注解的方法，需要自定义注解：

```java
// 1. 自定义注解
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyLog { }

// 2. 在业务方法上标注
@MyLog
public void delete(Integer id) { ... }

// 3. AOP 切面匹配
@Around("@annotation(com.tianxing.annotation.MyLog)")
public Object log(ProceedingJoinPoint joinPoint) throws Throwable { ... }
```

---

## 三、AOP 案例

### 3.1 需求：操作日志记录

在员工管理的增删改操作中，自动记录操作日志（操作人、操作时间、操作方法、参数）。

### 3.2 代码实现

```java
@Slf4j
@Component
@Aspect
public class LogAspect {

    @Autowired
    private OperateLogMapper operateLogMapper;

    @Around("@annotation(com.tianxing.annotation.Log)")
    public Object recordLog(ProceedingJoinPoint joinPoint) throws Throwable {
        // 操作前：记录开始时间
        long begin = System.currentTimeMillis();

        // 调用目标方法
        Object result = joinPoint.proceed();

        // 操作后：记录日志
        long end = System.currentTimeMillis();
        OperateLog operateLog = new OperateLog();
        operateLog.setOperateTime(LocalDateTime.now());
        operateLog.setMethod(joinPoint.getSignature().getName());
        operateLog.setCostTime(end - begin);
        operateLogMapper.insert(operateLog);

        return result;
    }
}
```

### 3.2 连接点信息获取

| 通知类型 | 连接点参数类型 | 说明 |
|------|------|------|
| `@Around` | `ProceedingJoinPoint` | 可获取方法签名、参数，**可调用 `proceed()`** |
| 其他 4 种 | `JoinPoint` | `JoinPoint` 是 `ProceedingJoinPoint` 的父类型 |

常用方法：
- `joinPoint.getSignature()` — 获取方法签名
- `joinPoint.getArgs()` — 获取方法参数

### 3.3 获取当前登录员工 — ThreadLocal

**问题**：AOP 切面中需要记录操作人，但切面方法无法直接获取请求中的 token。

**解决方案**：在 Filter/Interceptor 中解析 token 并将员工信息存入 `ThreadLocal`，AOP 中通过 `ThreadLocal.get()` 获取。

#### 3.3.1 ThreadLocal 原理

**ThreadLocal** 不是线程，而是线程的局部变量。
- 为**每个线程**提供一份**独立的存储空间**
- 不同线程之间**互相隔离**，不会相互干扰
- 同一线程 / 同一请求内，可实现数据共享

| 操作 | 方法 | 说明 |
|------|------|------|
| 存储 | `ThreadLocal.set(value)` | 向当前线程存入数据 |
| 获取 | `ThreadLocal.get()` | 从当前线程取出数据 |
| 清除 | `ThreadLocal.remove()` | 用完必须清除，防止内存泄漏 |

#### 3.3.2 实现流程

```
TokenFilter 拦截请求 → 解析 token 获取员工ID
  → ThreadLocal.set(empId)
  → AOP 切面中 ThreadLocal.get() 获取操作人
  → 请求结束后 ThreadLocal.remove() 清空
```

```java
// 工具类：封装 ThreadLocal 操作
public class BaseContext {
    private static final ThreadLocal<Integer> THREAD_LOCAL = new ThreadLocal<>();

    public static void setCurrentId(Integer id) { THREAD_LOCAL.set(id); }
    public static Integer getCurrentId()       { return THREAD_LOCAL.get(); }
    public static void remove()                { THREAD_LOCAL.remove(); }
}

// Filter 中存入
BaseContext.setCurrentId(empId);  // 登录员工ID

// AOP 中取出
Integer empId = BaseContext.getCurrentId();
```

---

## 今日总结

| 模块          | 核心要点                                                                                              |
| ----------- | ------------------------------------------------------------------------------------------------- |
| AOP 概念      | 面向切面/特定方法编程，抽取共性功能，无侵入式增强业务方法                                                                     |
| 核心概念        | 连接点 → 切入点(`@Pointcut`可抽取) → 通知 → 切面(`@Aspect`) → 目标对象                                             |
| 5 种通知       | `@Around`(前后+手动proceed) / `@Before` / `@After`(始终) / `@AfterReturning`(正常) / `@AfterThrowing`(异常) |
| 切入点表达式      | `execution(* 包.类.方法(..))` 按签名匹配；`@annotation(注解)` 按注解匹配                                           |
| 通知顺序        | 同类型默认字母序；`@Order(数字)` 控制，数字小的前置先执行、后置后执行                                                          |
| 动态代理        | Spring 生成代理对象，调用者 → 代理 → 切面 → 目标方法                                                                |
| ThreadLocal | 线程隔离存储，同一请求内共享数据；set/get/remove，用完必须清                                                             |

