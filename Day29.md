# Day29 ：缓存菜品、SpringCache、购物车与用户下单

## 一、缓存菜品代码开发

### 1.1 需求与背景
- 用户端需要频繁查询菜品/套餐列表，若每次请求都直接查询数据库，**数据库压力大、响应慢**。
- 解决方案：引入缓存（Redis），将热点数据缓存起来，查询时先查缓存，命中则直接返回。

### 1.2 实现方式
- **方式一：手动缓存（利用 RedisTemplate）**
  - 查询逻辑：先查 Redis → 命中直接返回；未命中 → 查数据库 → 写入 Redis（可设置过期时间）。
  - 典型 key 设计：`dish_分类id`（如 `dish_17`）、套餐用 `setmeal_分类id`。
  - 代码示意：
    ```java
    @Autowired
    private StringRedisTemplate redisTemplate;

    public List<DishVO> list(Long categoryId) {
        String key = "dish_" + categoryId;
        // 1. 查缓存，命中直接返回
        String json = redisTemplate.opsForValue().get(key);
        if (json != null) {
            return JSON.parseArray(json, DishVO.class);
        }
        // 2. 缓存未命中，查数据库
        List<DishVO> list = dishMapper.selectByCategoryId(categoryId);
        // 3. 写入缓存
        redisTemplate.opsForValue().set(key, JSON.toJSONString(list));
        return list;
    }
    ```
  - 更新菜品（新增/修改/删除）时，需手动删除相关缓存 key，保证缓存与数据库一致。
- **方式二：SpringCache 自动缓存（注解方式）**
  - 无需手写 Redis 操作代码，通过 `@Cacheable` / `@CachePut` / `@CacheEvict` 注解自动完成缓存读写，详见第二章。

### 1.3 缓存一致性要点
- 缓存菜品后，**修改/删除菜品时必须同时清除对应缓存**，否则用户端读到脏数据。
- 常用做法：在增删改方法上加 `@CacheEvict`（如 `@CacheEvict(value = "dishCache", allEntries = true)`）清空整个缓存分区。

---

## 二、SpringCache

### 2.1 概述
- **SpringCache 是 Spring 提供的基于注解的缓存功能**（对缓存的统一抽象封装），结合 Redis 可实现高性能缓存。
- 核心思想：通过注解声明缓存行为，由框架自动完成"查缓存 / 写缓存 / 清缓存"，开发者无需手动操作 Redis。

### 2.2 @EnableCaching
- **作用**：开启 Spring Cache 缓存注解功能（启动注解扫描，注册缓存相关基础设施）。
- **使用位置**：配置类上，一般加在启动类或专门的缓存配置类上。
- 示例：
  ```java
  @EnableCaching
  @SpringBootApplication
  public class SkyApplication {
      public static void main(String[] args) {
          SpringApplication.run(SkyApplication.class, args);
      }
  }
  ```

### 2.3 @Cacheable
- **作用**：方法执行前先查询缓存：
  - 缓存中有数据 → 直接返回缓存数据（**方法不执行**）；
  - 缓存中无数据 → 调用方法，并将方法返回值放入缓存。
- **使用位置**：方法上（通常加在**查询**方法上）。
- 示例：
  ```java
  @Cacheable(value = "dishCache", key = "#id")
  public Dish getById(Long id) { ... }
  ```

### 2.4 @CachePut
- **作用**：**先执行方法**，再将方法返回值放入缓存（用于更新缓存，方法总会执行）。
- **使用位置**：方法上（通常加在**新增/修改**方法上）。
- 示例：
  ```java
  @CachePut(value = "dishCache", key = "#dish.id")
  public Dish update(Dish dish) { ... }
  ```
- 与 @Cacheable 的本质区别：@Cacheable 命中缓存后方法不执行；@CachePut 无论缓存是否存在**都执行方法**，执行后写缓存。

### 2.5 @CacheEvict
- **作用**：删除缓存中的一条或多条数据（用于清理缓存，方法总会执行）。
- **使用位置**：方法上（通常加在**删除/修改**方法上）。
- 示例：
  ```java
  // 删除单个缓存
  @CacheEvict(value = "dishCache", key = "#id")
  public void deleteById(Long id) { ... }

  // 清空整个分区缓存
  @CacheEvict(value = "setmealCache", allEntries = true)
  public void updateSetmeal(...) { ... }
  ```

### 2.6 三注解对比

| 注解 | 触发时机 | 方法是否执行 | 缓存行为 | 典型场景 |
| --- | --- | --- | --- | --- |
| @Cacheable | 方法执行前 | 缓存未命中才执行 | 先查缓存，命中直接返回；未命中执行方法并缓存结果 | 查询操作 |
| @CachePut | 方法执行后 | 总是执行 | 执行方法，将返回值写入/更新缓存 | 新增、修改后刷新缓存 |
| @CacheEvict | 方法执行后（默认） | 总是执行 | 执行方法，删除一条/多条缓存数据 | 删除、修改后清理缓存 |

### 2.7 常用属性说明

| 属性 | 说明 | 适用注解 | 示例 |
| --- | --- | --- | --- |
| value / cacheNames | 缓存名称（缓存分区），可指定多个 | 三个注解均可 | `value = "dishCache"` |
| key | 缓存的 key，支持 spEL 表达式 | 三个注解均可 | `key = "#id"` |
| keyGenerator | key 生成器（与 key 二选一） | 三个注解均可 | `keyGenerator = "myKeyGenerator"` |
| condition | **满足条件才**缓存/清除（方法调用前判断，不能访问 #result） | 三个注解均可 | `condition = "#id > 0"` |
| unless | **满足条件则不**缓存（方法调用后判断，可访问 #result） | @Cacheable、@CachePut | `unless = "#result == null"` |
| beforeInvocation | 是否在方法**执行前**清除缓存，默认 false（方法异常则不清缓存）；true 表示执行前清除，方法异常也会清除 | @CacheEvict | `beforeInvocation = true` |
| allEntries | 是否清除该缓存名称下的**所有**数据，默认 false | @CacheEvict | `allEntries = true` |
| sync | 是否同步（并发下仅一个线程加载并缓存数据） | @Cacheable | `sync = true` |

### 2.8 spEL 表达式（Spring Expression Language）
- **作用**：为注解的 key / condition / unless 等属性提供**动态取值**能力。
- **语法特点**：以 `#` 开头引用上下文对象。
- 常见写法：

| 写法 | 含义 | 示例 |
| --- | --- | --- |
| `#参数名` | 引用方法入参（按参数名） | `key = "#id"`、`key = "#categoryId"` |
| `#p0` / `#a0` | 按**位置**引用入参（p0=第一个参数，a1=第二个参数），参数名不可用时使用 | `key = "#p0"` |
| `#result` | 引用**方法返回值**（仅方法调用后可用，如 unless、@CachePut 的 key） | `unless = "#result == null"` |
| 对象导航 `.` | 通过 `.` 访问对象属性/方法，实现动态取值 | `key = "#setmeal.id"`、`key = "#order.getDishId()"` |
| 字符串拼接 | key 中拼接固定前缀，**字符串字面量必须加单引号** | `key = "'dish_' + #categoryId"` |

- 典型示例：
  ```java
  // 直接用参数作为 key
  @Cacheable(value = "dishCache", key = "#id")

  // 对象导航：取入参对象的属性作为 key
  @CachePut(value = "dishCache", key = "#dish.id")

  // key 拼接固定前缀
  @Cacheable(value = "setmealCache", key = "'setmeal_' + #setmeal.id")

  // condition：满足条件才缓存
  @Cacheable(value = "dishCache", key = "#id", condition = "#id > 0")

  // unless：返回值为 null 时不缓存
  @Cacheable(value = "dishCache", key = "#id", unless = "#result == null")
  ```

### 2.9 SpringCache 基于代理技术的实现原理
- **SpringCache 基于 AOP 代理技术实现**：Spring 会为标注了缓存注解的 Bean 生成**代理对象**，所有方法调用都经过代理对象拦截。
- 实现机制 = **代理对象 + 反射**：
  1. Bean 初始化时，Spring 通过 AOP 创建代理对象（JDK 动态代理或 CGLIB 代理）；
  2. 调用带缓存注解的方法时，实际调用的是代理对象的方法（拦截器 CacheInterceptor 生效）；
  3. 拦截器通过**反射**获取方法上的注解信息，解析 spEL 表达式（SpelExpressionParser），动态计算 key / condition / unless；
  4. 执行缓存逻辑：查缓存（@Cacheable）→ 反射调用目标方法 → 写缓存（@CachePut）/ 清缓存（@CacheEvict）。

- 与 Redis 结合：SpringCache 默认使用 ConcurrentMapCacheManager（内存缓存）；配置 Redis 后使用 RedisCacheManager，缓存数据实际存入 Redis，实现分布式缓存。

---

## 三、添加购物车模块代码开发

### 3.1 需求分析
- 用户登录后可将**菜品/套餐**加入购物车，再次添加同一项时**数量 +1**（不重复插入）。
- 菜品口味（规格）不同视为不同购物车项。

### 3.2 数据库表：shopping_cart（购物车表）
| 字段 | 说明 |
| --- | --- |
| id | 主键 |
| user_id | 当前用户 id（区分不同用户） |
| dish_id | 菜品 id（为 null 表示该项是套餐） |
| setmeal_id | 套餐 id（为 null 表示该项是菜品） |
| dish_flavor | 菜品口味/规格 |
| number | 数量 |
| amount | 单价 |
| image | 图片 |
| create_time | 创建时间 |

### 3.3 实现思路
1. **Controller 层**：接收 `AddShoppingCartDTO`（含 dishId / setmealId / dishFlavor），调用 Service。
2. **Service 层**：
   - 获取当前登录用户 id（通过 ThreadLocal 保存的用户信息）；
   - 根据 `user_id + dish_id/setmeal_id + dish_flavor` 查询购物车中是否已存在该记录；
   - **已存在** → 记录数 `number + 1`，执行 update；
   - **不存在** → 构建 ShoppingCart 对象：设置用户 id、菜品/套餐信息（名称、图片、单价）、数量初始为 1，执行 insert。
3. **Mapper 层**：提供 select 查询、update 数量、insert 插入三个方法。

- 代码示意（Service 核心逻辑）：
  ```java
  @Service
  public class ShoppingCartServiceImpl implements ShoppingCartService {
      @Autowired
      private ShoppingCartMapper shoppingCartMapper;

      public void addShoppingCart(AddShoppingCartDTO dto) {
          ShoppingCart cart = new ShoppingCart();
          cart.setUserId(BaseContext.getCurrentId()); // ThreadLocal 获取当前用户

          // 1. 查询购物车中是否已有该记录
          ShoppingCart exist = shoppingCartMapper.selectByCondition(cart);
          if (exist != null) {
              // 2. 已存在：数量 +1
              exist.setNumber(exist.getNumber() + 1);
              shoppingCartMapper.updateNumberById(exist);
          } else {
              // 3. 不存在：构建对象并插入（数量 = 1，回填名称/图片/单价等）
              cart.setNumber(1);
              shoppingCartMapper.insert(cart);
          }
      }
  }
  ```

### 3.4 注意点
- 套餐与菜品共用一张表，通过 `dish_id` / `setmeal_id` 二选一区分；
- 同一用户、同一菜品/套餐、**口味不同**时是两条记录；
- 删除购物车 / 下单成功时需要清空对应记录。

---

## 四、用户下单模块代码开发

### 4.1 需求分析
- 用户选择地址、支付方式、备注后提交订单，系统生成**订单主表 + 订单明细表**，同时**清空购物车**。

### 4.2 数据库表
- **orders（订单主表）**：id、number（订单号）、user_id、address_book_id（地址）、order_time（下单时间）、checkout_time（结账时间）、pay_method（支付方式）、amount（总金额）、remark（备注）、status（订单状态：1 待付款 / 2 待接单 ...）、phone、address 等。
- **order_detail（订单明细表）**：id、order_id（关联主表）、dish_id / setmeal_id、dish_flavor（口味）、number（数量）、amount（金额）、image、name 等。

### 4.3 实现思路
1. **Controller 层**：接收 `OrdersSubmitDTO`（含 addressBookId、payMethod、remark 等），调用 Service。
2. **Service 层（@Transactional 事务保证一致性）**：
   - 校验：地址簿存在、购物车非空（空则抛出业务异常）；
   - **构建 Orders 主表对象并插入**：生成订单号（时间戳）、设置订单状态为待付款、下单时间、总金额；
   - **遍历购物车构建 OrderDetail 明细，批量插入**：每个明细关联 order_id、记录菜品/套餐信息；
   - **清空当前用户购物车**：`DELETE FROM shopping_cart WHERE user_id = ?`。
3. **Mapper 层**：insert orders、批量 insert order_detail、清空购物车。

- 代码示意（Service 核心逻辑）：
  ```java
  @Service
  public class OrderServiceImpl implements OrderService {
      @Transactional // 保证多表操作一致性
      public Long submitOrder(OrdersSubmitDTO dto) {
          // 1. 校验购物车
          List<ShoppingCart> carts = shoppingCartMapper.selectByUserId(userId);
          if (carts == null || carts.isEmpty()) {
              throw new BusinessException("购物车为空");
          }

          // 2. 构建订单主表并插入（订单号、状态=待付款、下单时间、总金额等）
          Orders orders = new Orders();
          orders.setNumber(String.valueOf(System.currentTimeMillis())); // 订单号
          orders.setStatus(Orders.PENDING_PAYMENT);                     // 待付款
          orders.setOrderTime(LocalDateTime.now());
          orders.setAmount(总金额); // 遍历购物车累加
          orderMapper.insert(orders);

          // 3. 构建订单明细并批量插入
          List<OrderDetail> details = 遍历购物车构建明细;
          orderDetailMapper.insertBatch(details);

          // 4. 清空购物车
          shoppingCartMapper.deleteByUserId(userId);
          return orders.getId();
      }
  }
  ```

### 4.4 注意点
- 下单涉及订单主表、订单明细表、购物车三处数据变更，**必须加 @Transactional**，任一环节失败整体回滚；

---

## 五、今日总结

| 模块               | 核心知识点                                                             |
| ---------------- | ----------------------------------------------------------------- |
| 缓存菜品代码开发         | RedisTemplate 手动缓存、SpringCache 注解缓存、缓存一致性                         |
| SpringCache      | @EnableCaching / @Cacheable / @CachePut / @CacheEvict             |
| SpringCache 常用属性 | cacheNames/value、key、condition、unless、beforeInvocation、allEntries |
| spEL 表达式         | #参数名、#result、#p0/#a0、对象导航（.）、字符串拼接                                |
| SpringCache 代理原理 | AOP 代理对象 + 反射、CacheInterceptor                                    |
| 添加购物车模块          | shopping_cart 表、数量 +1、口味区分                                        |
| 用户下单模块           | orders / order_detail 表、@Transactional、清空购物车                      |

