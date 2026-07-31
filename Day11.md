
# Day11 - 数据库与MySQL、SQL语句、JDBC、正则表达式、查找算法

---

## 一、数据库与 MySQL

### 1.1 什么是关系型数据库

**关系型数据库**是建立在关系模型之上的数据库，数据以**二维表**的形式存储，表由行（记录）和列（字段）组成。MySQL 是最流行的开源关系型数据库之一。

| 概念 | 说明 |
|------|------|
| 数据库 | 存放数据的仓库 |
| 表 | 数据的逻辑组织单元，由行和列构成 |
| 行（Row） | 表中的一条记录 |
| 列（Column） | 表中的一个字段 |

### 1.2 SQL 分类

| 分类 | 全称 | 作用 | 代表语句 |
|------|------|------|----------|
| DDL | Data Definition Language | 数据定义 | CREATE / ALTER / DROP / RENAME |
| DML | Data Manipulation Language | 数据操作 | INSERT / UPDATE / DELETE |
| DQL | Data Query Language | 数据查询 | SELECT |
| DCL | Data Control Language | 数据控制 | GRANT / REVOKE |

---

## 二、DDL 数据定义语言

### 2.1 数据库操作

| 语句 | 说明 |
|------|------|
| `SHOW DATABASES;` | 展示所有已创建的数据库 |
| `SELECT DATABASE();` | 查询当前所处的数据库 |
| `CREATE DATABASE [IF NOT EXISTS] db01;` | 如果不存在 db01 就创建 |
| `USE db01;` | 切换到 db01 数据库 |
| `DROP DATABASE [IF EXISTS] db01;` | 如果存在就删除 db01 |

### 2.2 表创建

```sql
CREATE TABLE 表名(
    字段1 字段1类型 [约束] [COMMENT '注释'],
    字段2 字段2类型 [约束] [COMMENT '注释'],
    ...
);
```

### 2.3 约束

| 约束 | 关键字 | 说明 |
|------|--------|------|
| 非空 | `NOT NULL` | 字段值不能为 null |
| 唯一 | `UNIQUE` | 字段值不能重复 |
| 主键 | `PRIMARY KEY` | 非空且唯一，每张表只能有一个 |
| 默认 | `DEFAULT '默认值'` | 未指定值时使用默认值 |
| 外键 | `FOREIGN KEY` | 关联其他表的主键 |
| 自增 | `AUTO_INCREMENT` | 自动递增，通常用于主键 |

### 2.4 字段类型

#### 2.4.1 数值型

| 类型 | 说明 |
|------|------|
| `TINYINT` | 极小整数 |
| `SMALLINT` | 小整数 |
| `INT` | 普通整数 |
| `BIGINT` | 大整数 |
| `FLOAT` | 单精度浮点数 |
| `DOUBLE` | 双精度浮点数 |
| `DECIMAL` | 定点数，精确小数 |

#### 2.4.2 字符型

| 类型 | 说明 |
|------|------|
| `CHAR` | 定长字符串，长度固定，需指定长度 |
| `VARCHAR` | 变长字符串，需指定最大长度 |

#### 2.4.3 日期时间型

| 类型 | 格式 |
|------|------|
| `DATE` | `YYYY-MM-DD` |
| `TIME` | `HH:MM:SS` |
| `YEAR` | `YYYY` |
| `DATETIME` | `YYYY-MM-DD HH:MM:SS` |

### 2.5 其他表操作

| 语句 | 说明 |
|------|------|
| `SHOW TABLES;` | 查看当前数据库所有表 |
| `DESC 表名;` | 查看表结构 |
| `SHOW CREATE TABLE 表名;` | 查看表的创建语句 |

### 2.6 修改表结构（ALTER）

| 操作 | 语法 |
|------|------|
| 增加字段 | `ALTER TABLE 表名 ADD 字段名 类型(长度) [COMMENT '注释'] [约束];` |
| 修改字段 | `ALTER TABLE 表名 MODIFY 字段名 新类型 [约束];` |
| 删除字段 | `ALTER TABLE 表名 DROP 字段名;` |

### 2.7 修改表名与删除表

| 操作 | 语法 |
|------|------|
| 修改表名 | `RENAME TABLE 表名 TO 新表名;` |
| 删除表 | `DROP TABLE [IF EXISTS] 表名;` |

---

## 三、DML 数据操作语言

### 3.1 INSERT 插入数据

```sql
-- 指定字段插入
INSERT INTO 表名(字段1, 字段2) VALUES(值1, 值2);

-- 省略字段名，为所有字段添加值（值顺序必须与表结构一致）
INSERT INTO 表名 VALUES(值1, 值2, ...);

-- 批量插入
INSERT INTO 表名(字段1, 字段2) VALUES(值1, 值2), (值1, 值2);
```

### 3.2 UPDATE 更新数据

```sql
UPDATE 表名 SET 字段1=值1, 字段2=值2, ... [WHERE 条件];
```

> **注意**：如果不加 `WHERE` 条件，将更新整张表的所有记录。

### 3.3 DELETE 删除数据

```sql
DELETE FROM 表名 [WHERE 条件];
```

> **注意**：如果不加 `WHERE` 条件，将删除整张表的所有记录。

---

## 四、DQL 数据查询语言

### 4.1 完整语法顺序

```sql
SELECT    字段列表
FROM      表名列表
WHERE     条件列表
GROUP BY  分组字段列表
HAVING    分组后条件列表
ORDER BY  排序字段列表
LIMIT     分页参数
```

### 4.2 基础查询

| 关键字 | 说明 |
|--------|------|
| `DISTINCT` | 去重，去除重复记录 |
| `AS` | 起别名，可省略 |

```sql
SELECT DISTINCT job FROM emp;
SELECT name AS 姓名, salary AS 工资 FROM emp;
```

### 4.3 条件查询

| 条件 | 说明 |
|------|------|
| `BETWEEN ... AND ...` | 在某个范围之内（含边界值） |
| `IN(...)` | 在指定值列表中 |
| `LIKE` | 模糊匹配，`_` 匹配单个字符，`%` 匹配任意个字符 |
| `IS NULL` | 判断是否为空 |
| `AND` / `&&` | 逻辑与 |
| `OR` / `||` | 逻辑或 |
| `NOT` / `!` | 逻辑非 |

### 4.4 聚合函数

聚合函数对一列的值进行计算，将一列数据作为一个整体进行纵向运算，返回单个结果值。

| 函数 | 说明 |
|------|------|
| `COUNT(*)` | 统计总行数 |
| `MAX(字段)` | 最大值 |
| `MIN(字段)` | 最小值 |
| `AVG(字段)` | 平均值 |
| `SUM(字段)` | 求和 |

### 4.5 WHERE 与 HAVING 的区别

| 对比项 | WHERE | HAVING |
|--------|-------|--------|
| 过滤时机 | 分组前过滤 | 分组后过滤 |
| 能否对聚合函数判断 | 不能 | 能 |

示例：

```sql
SELECT job, COUNT(*)
FROM emp
WHERE entry_date <= '2015-01-01'
GROUP BY job
HAVING COUNT(*) >= 2;
```

### 4.6 ORDER BY 排序

```sql
ORDER BY 字段1 排序方式, 字段2 排序方式, ...;
```

| 排序方式 | 说明 |
|----------|------|
| `ASC` | 升序（默认） |
| `DESC` | 降序 |

> **多字段排序**：先按第一个字段排序，第一个字段值相同时再按第二个字段排序。

### 4.7 LIMIT 分页查询

```sql
SELECT 字段列表 FROM 表名 LIMIT 起始索引, 查询记录数;
```

> `LIMIT` 是 MySQL 特有的分页语法，其他数据库不一定支持。

---

## 五、JDBC

### 5.1 什么是 JDBC

**JDBC**（Java Database Connectivity）是 Java 语言操作关系型数据库的标准 API，提供统一的接口访问不同数据库。

### 5.2 JDBC 操作步骤

```
注册驱动 → 获取连接 → 获取 SQL 执行对象 → 执行 SQL → 释放资源
```

| 步骤 | 代码 |
|------|------|
| 获取连接 | `DriverManager.getConnection(url, user, password)` |
| 获取执行对象 | `connection.createStatement()` |
| 执行 SQL | `executeUpdate()` 或 `executeQuery()` |
| 释放资源 | 关闭 ResultSet → Statement → Connection |

### 5.3 executeUpdate

用于执行 **INSERT / UPDATE / DELETE** 语句。

```java
int rows = statement.executeUpdate(sql);
```

| 返回值 | 说明 |
|--------|------|
| `int` | 受影响的行数 |

### 5.4 executeQuery

用于执行 **SELECT** 语句，返回 `ResultSet` 结果集对象。

```java
ResultSet rs = statement.executeQuery(sql);
```

**解析 ResultSet 的方法：**

| 方法 | 说明 |
|------|------|
| `next()` | 判断当前行是否为有效行，返回 `boolean`；同时将光标移动到下一行 |
| `getXxx(字段名/列索引)` | 获取当前行的字段数据，如 `getString()`、`getInt()` |

### 5.5 PreparedStatement 预编译

**PreparedStatement** 是 Statement 的子接口，支持预编译 SQL。

```java
// 使用 ? 作为占位符
PreparedStatement ps = connection.prepareStatement("SELECT * FROM user WHERE name = ? AND password = ?");
ps.setString(1, name);
ps.setString(2, password);
ResultSet rs = ps.executeQuery();
```

#### 优势一：防止 SQL 注入

**SQL 注入**是指用户通过输入特定的字符串来拼接和干扰原始 SQL 语句，从而绕过验证或窃取数据。`PreparedStatement` 将 SQL 语句与参数分离，参数值中的特殊字符会被转义，无法改变 SQL 逻辑。

#### 优势二：性能更高

| 步骤 | Statement（静态） | PreparedStatement（预编译） |
|------|-------------------|---------------------------|
| 语法解析检查 | 每次执行 | 仅首次 |
| 优化 SQL | 每次执行 | 仅首次 |
| 编译 SQL | 每次执行 | 仅首次 |
| 缓存 | 无 | 编译结果缓存 |
| 执行 SQL | 每次执行 | 每次执行 |

> 预编译 SQL 只需一次编译，后续执行直接命中缓存，大幅提升性能。

---

## 六、正则表达式

### 6.1 基本概念

正则表达式（Regular Expression）用于：
- 校验字符串是否满足规则
- 在文本中查找满足要求的内容

### 6.2 Java 中的使用

```java
String str = "hello123";
// matches() 方法用于校验整个字符串是否匹配正则
boolean result = str.matches("\\w+");  // true
```

| 方法 | 说明 |
|------|------|
| `matches(regex)` | 判断字符串是否完全匹配正则表达式 |

---

## 七、查找算法

### 7.1 二分查找

**前提条件**：数组必须**有序**。

**算法思路**：设置 `min` 和 `max` 两个索引，每次取中间位置 `mid` 与目标值比较，根据比较结果缩小搜索范围。

```
1. 计算 mid = (min + max) / 2
2. 比较 arr[mid] 与目标值 key：
   - arr[mid] == key → 找到目标，结束
   - arr[mid] > key  → key 在左半边，max = mid - 1
   - arr[mid] < key  → key 在右半边，min = mid + 1
3. 重复上述步骤，直到 min > max 表示目标值不存在于数组中
```

### 7.2 插值查找

插值查找是二分查找的改进版，适合**分布较为均匀**的有序数组。

**mid 计算公式**：

```
mid = min + (key - arr[min]) / (arr[max] - arr[min]) * (max - min)
```

> 与二分查找的区别仅在于 `mid` 的计算方式：二分查找取中间位置，插值查找根据目标值与边界值的比例"预测"目标位置。

### 7.3 分块查找

**核心思想**：将数据分成若干块，块内无序但块间有序（前一块的最大值小于后一块的最小值），先确定目标所在的块，再在块内顺序查找。

**Block 类的属性**：

| 属性 | 说明 |
|------|------|
| 起始索引 | 该块在数组中的起始位置 |
| 结束索引 | 该块在数组中的结束位置 |
| 块内最大值 | 该块所有元素的最大值 |

**查找步骤**：

1. 根据目标值，在各块的最大值中确定目标属于哪一块
2. 在该块的范围内顺序查找目标值

---

## 今日总结

| 模块 | 核心要点 |
|------|----------|
| 数据库与 MySQL | 关系型数据库，数据以二维表存储；SQL 分为 DDL / DML / DQL |
| DDL | 数据库和表的创建/修改/删除，六大约束，三种字段类型 |
| DML | INSERT 批量插入、UPDATE 带 WHERE 更新、DELETE 注意全表风险 |
| DQL | 完整语法顺序，聚合函数，WHERE vs HAVING，ORDER BY 排序，LIMIT 分页 |
| JDBC | 五步操作流程，executeUpdate / executeQuery，PreparedStatement 防注入+缓存 |
| 正则表达式 | matches() 校验字符串格式 |
| 查找算法 | 二分查找（有序）、插值查找（均匀分布）、分块查找（块间有序） |

