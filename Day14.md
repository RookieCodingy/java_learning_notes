
# Day14 - 多表关系与多表查询

## 一、多表关系

在实际业务中，表与表之间不是孤立的，存在三种关系。

### 1.1 一对多（One-to-Many）

一个部门下有多个员工，一个员工只属于一个部门。

- **实现方式**：在"多"的一方添加字段，关联"一"的一方的主键
- **示例**：`emp` 表中添加 `dept_id` 字段，关联 `dept` 表的 `id`

```
dept（一）           emp（多）
┌────┬──────┐      ┌────┬──────┬─────────┐
│ id │ name │      │ id │ name │ dept_id │
├────┼──────┤      ├────┼──────┼─────────┤
│ 1  │ 研发 │◄─────│ 1  │ 张三 │    1    │
│ 2  │ 市场 │◄─────│ 2  │ 李四 │    1    │
└────┴──────┘      │ 3  │ 王五 │    2    │
                   └────┴──────┴─────────┘
```

### 1.2 外键约束（Foreign Key）

#### 物理外键

使用数据库原生 `FOREIGN KEY` 定义外键关联：

**建表时添加：**

```sql
CREATE TABLE emp (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50),
    dept_id INT,
    CONSTRAINT fk_emp_dept FOREIGN KEY (dept_id) REFERENCES dept(id)
);
```

**建表后添加：**

```sql
ALTER TABLE emp ADD CONSTRAINT fk_emp_dept
    FOREIGN KEY (dept_id) REFERENCES dept(id);
```

**物理外键的缺点：**

| 缺点 | 说明 |
|------|------|
| 影响增删改效率 | 每次操作都需检查外键关系 |
| 不适用分布式集群 | 跨库无法维护外键约束 |
| 容易引发死锁 | 多表级联操作时锁竞争 |

#### 逻辑外键（推荐）

不在数据库层面定义外键，而是在业务逻辑层通过代码保证数据一致性。这是实际开发中的主流做法。

```
物理外键 → 数据库约束
逻辑外键 → 业务代码约束（更灵活，性能更好）
```

### 1.3 一对一（One-to-One）

用于单表拆分，将大表拆为两张小表以提升查询效率。

- **实现方式**：在任意一方添加外键关联另一方主键，并设置为 **UNIQUE**
- **示例**：用户表拆分

```
user（基础信息）                user_ext（扩展信息）
┌────┬──────┐                 ┌────┬──────┬─────────┐
│ id │ name │                 │ id │ addr │ user_id │
├────┼──────┤                 ├────┼──────┼─────────┤
│ 1  │ 张三 │◄────────────────│ 1  │ 北京 │    1    │
└────┴──────┘                 └────┴──────┴─────────┘
```

### 1.4 多对多（Many-to-Many）

一个学生可以选多门课，一门课可以被多个学生选择。

- **实现方式**：建立第三张 **中间表**，至少包含两个外键，分别关联两方主键

```
student             student_course（中间表）         course
┌────┬──────┐      ┌────┬───────────┬──────────┐   ┌────┬──────────┐
│ id │ name │      │ id │ student_id│ course_id│   │ id │ name     │
├────┼──────┤      ├────┼───────────┼──────────┤   ├────┼──────────┤
│ 1  │ 张三 │◄─────│ 1  │     1     │    1     │──►│ 1  │ Java     │
│ 2  │ 李四 │◄─────│ 2  │     1     │    2     │──►│ 2  │ MySQL    │
└────┴──────┘      │ 3  │     2     │    1     │   └────┴──────────┘
                   └────┴───────────┴──────────┘
```

### 1.5 三种关系总结

| 关系 | 实现方式 | 典型场景 |
|------|----------|----------|
| 一对多 | 多的一方加外键 | 部门 ↔ 员工 |
| 一对一 | 任意一方加外键 + UNIQUE | 用户基本信息 ↔ 扩展信息 |
| 多对多 | 建中间表（两个外键） | 学生 ↔ 课程 |

---

## 二、多表查询

### 2.1 笛卡尔积

直接 `SELECT * FROM A, B` 会得到 A × B 的笛卡尔积，大部分是无效数据。

```sql
-- 错误：产生笛卡尔积
SELECT * FROM emp, dept;
-- 假设 emp 4 行，dept 3 行 → 结果 12 行（大量无效组合）
```

**消除方式**：添加连接条件 `WHERE emp.dept_id = dept.id`。

### 2.2 内连接

查询 A、B 两表的**交集**部分。

#### 隐式内连接

```sql
SELECT 字段列表
FROM 表1, 表2
WHERE 条件;
```

```sql
SELECT emp.name, dept.name
FROM emp, dept
WHERE emp.dept_id = dept.id;
```

#### 显式内连接（推荐）

```sql
SELECT 字段列表
FROM 表1 [INNER] JOIN 表2
ON 连接条件;
```

```sql
SELECT e.name, d.name
FROM emp e
INNER JOIN dept d ON e.dept_id = d.id;
```

| 注意点 | 说明 |
|--------|------|
| 字段指定 | 多表联查时，字段前加表名指定来源：`emp.name` |
| 表别名 | 可用别名简化书写：`FROM emp e`，起别名后只能用别名 |

### 2.3 外连接

#### 左外连接

查询**左表所有数据** + 两表交集：

```sql
SELECT 字段列表
FROM 表1 LEFT [OUTER] JOIN 表2
ON 连接条件;
```

```sql
-- 查出所有部门及其员工（包含没有员工的部门）
SELECT d.name, e.name
FROM dept d
LEFT JOIN emp e ON d.id = e.dept_id;
```

#### 右外连接

查询**右表所有数据** + 两表交集：

```sql
SELECT 字段列表
FROM 表1 RIGHT [OUTER] JOIN 表2
ON 连接条件;
```

> 开发中**偏向左外连接**，右外连接可通过交换表顺序改写为左外连接。

### 2.4 连接对比

| 连接类型 | 结果 | 语法 |
|----------|------|------|
| 内连接 | A ∩ B | `INNER JOIN ... ON` |
| 左外连接 | A + (A ∩ B) | `LEFT JOIN ... ON` |
| 右外连接 | B + (A ∩ B) | `RIGHT JOIN ... ON` |

---

## 三、子查询

SELECT 语句中嵌套 SELECT 语句，称为子查询（嵌套查询）。

```sql
SELECT * FROM t1 WHERE column1 = (SELECT column1 FROM t2 ...);
```

### 3.1 按结果分类

| 类型 | 结果形式 | 常用运算符 | 说明 |
|------|----------|------------|------|
| 标量子查询 | 单个值（一行一列） | `=`, `>`, `<`, `>=`, `<=`, `<>` | 最简单 |
| 列子查询 | 一列多行 | `IN`, `NOT IN` | 多值匹配 |
| 行子查询 | 一行多列 | `=`, `<>` | 较少用 |
| 表子查询 | 多行多列 | 常作为临时表 | 放在 FROM 后 |

#### 标量子查询

```sql
-- 查询比"张三"工资高的员工
SELECT * FROM emp
WHERE salary > (SELECT salary FROM emp WHERE name = '张三');
```

#### 列子查询

```sql
-- 查询在"研发部"或"市场部"的员工
SELECT * FROM emp
WHERE dept_id IN (SELECT id FROM dept WHERE name IN ('研发部', '市场部'));
```

#### 行子查询

```sql
-- 查询与"张三"同部门同薪资的员工
SELECT * FROM emp
WHERE (dept_id, salary) = (
    SELECT dept_id, salary FROM emp WHERE name = '张三'
);
```

#### 表子查询

```sql
-- 将子查询结果作为临时表
SELECT * FROM (
    SELECT * FROM emp WHERE entry_date > '2024-01-01'
) AS new_emp;
```

### 3.2 按位置分类

| 位置 | 示例 | 说明 |
|------|------|------|
| WHERE 后 | `WHERE id = (子查询)` | 标量、列、行子查询 |
| FROM 后 | `FROM (子查询) AS t` | 表子查询，作为临时表 |
| SELECT 后 | `SELECT name, (子查询)` | 标量子查询 |

---

## 今日总结

| 模块 | 核心要点 |
|------|----------|
| 一对多 | 多的一方加外键；物理外键 vs 逻辑外键（推荐） |
| 一对一 | 任意一方加外键 + UNIQUE，用于单表拆分 |
| 多对多 | 建中间表，含两个外键关联两方主键 |
| 内连接 | 查询交集：隐式 `FROM A,B WHERE` / 显式 `JOIN ON` |
| 外连接 | 左外：左表全 + 交集；右外：右表全 + 交集；开发偏向左外 |
| 子查询 | 标量/列/行/表四种；可放 WHERE / FROM / SELECT 后 |

