
# Day27 - Redis入门

## 一、Redis是什么

一个基于内存的key-value的结构数据库

- **基于内存存储，读写性能高**：数据保存在内存中，进程重启后会丢失，通常需要配合持久化或主从复制使用。
- **存储热点数据（抢购、秒杀**：利用内存高读写性能承接高并发热点访问，减轻后端数据库压力，原话括号未闭合，应为"抢购、秒杀）"。
- **对于mysql的一个补充**：常作为 MySQL 前的缓存层，热点数据放 Redis，冷数据落 MySQL。
- 默认端口6379
- AUTH password：连接时校验访问密码，密码在配置文件中通过 requirepass 设置。

## 二、redis中的常用数据类型

value部分的数据类型

| 数据类型 | 说明 |
| -------- | ---- |
| string 普通字符串 | 最基础的类型，value 可以是字符串或数字，支持自增自减，适合计数器场景。 |
| hash 类似hashmap | 适合存储对象，可以单独更新某个字段。 |
| list 类似于linkedlist | 支持从头部或尾部插入弹出，可组合成简单的消息队列。 |
| set 类似于hashset | 元素无序且唯一，适合去重、标签等场景。 |
| sorted set/ zset | 每一个元素关联一个分数，根据分数升序排列且没有重复元素；经典排行榜实现，ZINCRBY 可直接为成员加分。 |

## 三、redis常用命令

### 字符串操作命令

| 命令 | 说明 |
| ---- | ---- |
| SET key value | 设置指定key的值 |
| GET key | 获取指定key的值 |
| SETEX key seconds value | 设置只当key的值，并将key的过期时间设为seconds秒（验证码 |
| SETNX key value | 只有key不存在时设置key的值 |

**注意**：SETEX 原话"只当"应为"指定"，末尾括号未闭合，应为"（验证码）"；常用来存验证码这类需要自动过期的数据。SETNX 常用于分布式锁的加锁操作，设置成功返回 1，失败返回 0。

![[Pasted image 20260817174401.png|275]]

### 哈希操作命令

| 命令 | 说明 |
| ---- | ---- |
| HSET key field value | 设置哈希表中指定字段的值 |
| HGET key field | 获取哈希表中指定字段的值 |
| HDEL key field | 删除哈希表中指定字段 |
| HKEYS key | 获取哈希表中所有字段名 |
| HVALS key | 获取哈希表中所有字段值 |

**注意**：与 HashMap 操作一一对应，适合操作对象中的单个字段。

### 列表操作命令

| 命令 | 说明 |
| ---- | ---- |
| LPUSH key value1 [value2] | 从头部插入一个或多个值 |
| LRANGE key start stop | 获取列表指定范围内的元素 |
| RPOP key | 从尾部取出一个元素 |
| LLEN key | 获取列表长度 |

**注意**：LPUSH 从头部插入，RPOP 从尾部取出，可组合实现简单队列。

### 集合操作命令

| 命令 | 说明 |
| ---- | ---- |
| SADD key member1 [member2] | 向集合添加一个或多个成员 |
| SMEMBERS key | 获取集合中所有成员 |
| SCARD key | 获取集合的成员数量 |
| SINTER key1 [key2] | 求多个集合的交集 |
| SUNION key1 [key2] | 求多个集合的并集 |
| SREM key member | 移除集合中指定成员 |

**注意**：SINTER 取交集、SUNION 取并集，适合做共同好友、推荐等计算。

### 有序集合操作命令

| 命令 | 说明 |
| ---- | ---- |
| ZADD key score1 member1 [score2 member2] | 添加成员及其分数 |
| ZRANGE key start stop [WITHSCORES] | 按分数升序获取指定范围内的成员 |
| ZINCRBY key increment member | 为成员的分数增加 increment |
| ZREM key member[member...] | 移除一个或多个成员 |

### 通用命令

| 命令 | 说明 |
| ---- | ---- |
| KEYS pattern | 查找所有符合 pattern 的 key |
| EXISTS key | 判断 key 是否存在 |
| TYPE key | 获取 key 的类型 |
| DEL key | 删除 key |

**注意**：KEYS 会全量扫描所有 key，生产环境数据量大时慎用，可改用 SCAN 遍历。

## 四、redisTemplate

redisTemplate调用方法opsFor... 可以创建操作对象

**注意**：opsForValue、opsForHash、opsForList、opsForSet、opsForZSet 分别对应 string、hash、list、set、zset 五种数据类型。

通用命令直接用redisTemplate调用方法

如 delete、expire、hasKey 等通用方法由 RedisTemplate 直接提供，不需要 opsFor。

## 五、一些思考


**Q1：为什么要设置key的序列化器？**

为了保持key的规范性，不设置序列化器key会变得很不规则。

RedisTemplate 默认使用 JDK 序列化器，key 会被存成带二进制前缀的形式，不便于阅读和按 key 查询；设置 String 序列化器后，key 以普通字符串形式存储，保持规范。

**Q2：怎样保持value在redis中的规范性？**

也可以仿照key的序列化器，创建一个value的string类型序列化器。
后面发现，如果要对value进行序列化，必须要先将需要插入的值强转为string类型，否则会报错？弹幕提到一种方法：可以设置为json格式的序列化器，待尝试。

把 value 序列化器换成 GenericJackson2JsonRedisSerializer 后，插入对象时不需要手动强转字符串，它会自动把对象序列化成 JSON 并带上类型信息，取出时也能自动转回对象。使用时注意对象需要有默认构造方法或保留类型信息，否则反序列化可能报错。

**Q3：bean名称冲突怎么办？**

通过在RestController后添加属性自定义bean名称。

例如给 @RestController 显式指定名称，Spring 容器中同类型存在多个 bean 时就能按名称区分，注入时配合 @Qualifier 精确选择。

## 六、今天的未解之谜

redis的conf文件中明明配置了password，但是不管是java连接redis还是用图形化工具连接redis，好像都不需要密码就能进行连接，只有在终端操作redis才需要password？java配置了password反而报错，最后注释了conf中的password不了了之

## 今日总结

| 知识点 | 要点说明 |
| ------ | ------ |
| Redis 是什么 | 基于内存的 key-value 结构数据库，默认端口 6379，作为 MySQL 的缓存补充 |
| 常用数据类型 | value 的五种类型：string / hash / list / set / sorted set(zset) |
| 常用命令 | 字符串、哈希、列表、集合、有序集合、通用六类命令 |
| redisTemplate | opsForXxx 创建对应数据类型的操作对象，通用命令直接调用 |
| key 序列化器 | 保证 key 规范性，避免 JDK 默认序列化的二进制前缀 |
| value 序列化器 | 可换 GenericJackson2JsonRedisSerializer，对象自动转 JSON 存取 |

