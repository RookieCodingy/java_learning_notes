# Day35：缓存与 Redis 缓存问题

---

## 一、缓存基础

### 1.1 什么是缓存？

- 数据交换的缓冲区，存储数据的临时地方，读写性能高
- cpu高速缓存

### 1.2 缓存的作用

- 降低后端负载
- 提高读写效率，降低响应时间

### 1.3 缓存的成本

- 数据一致性成本
- 代码维护成本
- 运维成本

---

## 二、缓存更新策略

### 2.1 三种更新策略

| 策略 | 说明 |
| --- | --- |
| 内存淘汰 | 利用 redis 内存淘汰机制 |
| 超时剔除 | expire 添加 TTL 时间，到期自动删除 |
| 主动更新 | 修改数据库的同时更新缓存 |

### 2.2 CacheAside（旁路缓存模式）

**读操作：**

- 对缓存进行查询，不存在则查数据库
- 将数据写入缓存，并设置超时时间

**写操作：**

- 先操作数据库，再删缓存相较更为线程安全
- 保证数据库与缓存的原子性

---

## 三、缓存三大问题

### 3.1 缓存穿透

客户端请求的数据再缓存和数据库中都不存在，缓存永远不会生效，所有请求都会打到数据库。

**解决方案：**

- 缓存空对象
  - 实现简单，维护方便
  - 额外内存消耗
  - 可能造成短期的不一致
- 布隆过滤
  - 在 redis 和客户端之间添加一层布隆过滤器
  - 原理：将数据库中的数据转为 hash 二进制，面对不存在请求则拒绝
  - 可能误判的原因：哈希碰撞
  - 内存占用少，没有多余的 key
  - 实现复杂、存在误判可能

**还可以主动防止缓存穿透：**

- 做好 id 的复杂度，避免被猜测 id 规律
- 做好数据基础格式校验
- 用户权限校验
- 热点参数的限流

**项目实现思路（hm-dianping）：**

项目采用**缓存空对象**方案（`ShopServiceImpl#queryWithPassThrough`）

- 缓存 key：`cache:shop:{id}`，正常数据 TTL 30 分钟（`CACHE_SHOP_TTL`），空值 TTL 2 分钟（`CACHE_NULL_TTL`）
- 查询流程：
  1. 查 Redis，命中且非空字符串则直接返回
  2. 命中空字符串说明缓存了空对象，直接返回 null（商户不存在）
  3. 未命中则查数据库：不存在时向 Redis 写入空字符串 `""`（TTL 2 分钟）；存在时写入 JSON（TTL 30 分钟）
- 关键代码：

```java
// 未命中缓存 -> 查数据库
Shop shop = getById(id);
if (shop == null) {
    // 空值处理：缓存空对象，TTL 2 分钟
    stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
    return null;
}
// 存在则缓存 JSON，TTL 30 分钟
stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);
```

- 实现要点：利用 `get()` 未命中返回 null、缓存空值时返回 `""` 的特点，区分"key 不存在"与"命中空值"，避免穿透请求反复打到数据库

### 3.2 缓存雪崩

统一时段内大量的 key 同时失效或者 redis 宕机，导致大量请求到达数据库，带来巨大压力。

**解决方案：**

- 给不同的 key 的 TTL 添加随机值
- 利用 redis 集群
- 缓存业务添加降级限流策略
- 给业务添加多级缓存

### 3.3 缓存击穿

热点 key 问题，被高并发访问且缓存重建业务较复杂的 key 突然失效，无数请求访问瞬间给数据库带来巨大冲击。

**解决方案：**

- 互斥锁
- 逻辑过期

**项目实现思路（hm-dianping）：**

项目在 `ShopServiceImpl` 中同时实现了互斥锁与逻辑过期两种方案，通过注释切换启用。

**方案一：互斥锁（`queryWithMutex`）**

- 锁 key：`lock:shop:{id}`，锁超时 10 分钟（`LOCK_SHOP_TTL`）
- 查询流程：
  1. 查 Redis，命中直接返回
  2. 未命中则尝试获取互斥锁（`setIfAbsent` 实现 SETNX + 过期时间）
  3. 获取锁失败：`Thread.sleep(200)` 休眠后递归重查（自旋等待其他线程重建完成）
  4. 获取锁成功：二次检查缓存（双重检查），再查数据库；商户不存在则缓存空串（2 分钟），存在则缓存 JSON（30 分钟）
  5. finally 释放锁
- 关键代码：

```java
// 加锁：SETNX + 过期时间
private boolean tryLock(String key) {
    Boolean flag = stringRedisTemplate.opsForValue()
            .setIfAbsent(key, "1", LOCK_SHOP_TTL, TimeUnit.MINUTES);
    return BooleanUtil.isTrue(flag);
}

// 获取锁失败，休眠后重试
if (!isLocked) {
    Thread.sleep(200);
    return queryWithMutex(id);
}
// 获取锁成功，查库并重建缓存（双重检查后）
stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);
// finally 中 unlock(lockKey) 释放锁
```

**方案二：逻辑过期（`queryWithLogicalExpire` + `saveShop2Redis`）**

- 缓存中存放 `RedisData` 封装：`expireTime`（逻辑过期时间）+ `data`（实际数据），写入缓存时**不设置 TTL**（key 永不物理过期）
- 查询流程：
  1. 解析 `RedisData`，`expireTime` 未过期则直接返回数据
  2. 已过期则尝试获取互斥锁：获取成功，提交到固定线程池（10 线程）异步重建缓存，finally 释放锁；获取失败，直接返回旧数据
- 关键代码：

```java
// 封装逻辑过期时间（项目演示为 20 秒）
RedisData redisData = new RedisData();
redisData.setData(shop);
redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireTime));
stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id, JSONUtil.toJsonStr(redisData));

// 过期后异步重建缓存
CACHE_REBUILD_EXECUTOR.submit(() -> {
    try {
        this.saveShop2Redis(id, 20L);
    } finally {
        unlock(lockKey);
    }
});
```

- 实现要点：逻辑过期牺牲短暂的数据一致性（读线程可能拿到过期数据）换取高可用，缓存重建异步执行，不阻塞读请求

---

## 四、设计原则补充

- 降低耦合
- 组合优于继承

---

## 今日总结

| 主题 | 要点 |
| --- | --- |
| 缓存定义 | 数据交换的缓冲区，存储数据的临时地方，读写性能高 |
| 缓存作用 | 降低后端负载，提高读写效率，降低响应时间 |
| 缓存成本 | 数据一致性成本、代码维护成本、运维成本 |
| 更新策略 | 内存淘汰、超时剔除、主动更新 |
| CacheAside | 读：查缓存未命中则查数据库并回填、设置超时；写：先操作数据库，再删缓存 |
| 缓存穿透 | 缓存和数据库中都不存在的数据导致请求直达数据库；方案：缓存空对象、布隆过滤，并做好 id 复杂度、参数校验、权限校验、限流 |
| 缓存雪崩 | 大量 key 同时失效或 redis 宕机；方案：TTL 随机值、redis 集群、降级限流、多级缓存 |
| 缓存击穿 | 热点 key 突然失效瞬间冲击数据库；方案：互斥锁、逻辑过期 |
| 设计原则 | 降低耦合、组合优于继承 |
