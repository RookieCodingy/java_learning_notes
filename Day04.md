
# Java Day04：Lambda 表达式、集合与 gameTest 格斗游戏实战

---

## 一、Lambda 表达式

### 1.1 函数式接口

**概念**：只声明**一个抽象方法**的接口，称为函数式接口。

```java
// 函数式接口示例：只有一个抽象方法
public interface Swimmable {
    void swim();
}

// 非函数式接口：有多个抽象方法
public interface Flyable {
    void fly();
    void land();
}
```

函数式接口是 Lambda 表达式的前提 —— Lambda 只能简化**函数式接口**的匿名内部类写法。

### 1.2 @FunctionalInterface 注解

用于显式标注一个接口是函数式接口。如果接口中多写了一个抽象方法，编译器会直接报错。

```java
@FunctionalInterface
public interface Calculator {
    int compute(int a, int b);
    // int subtract(int a, int b);  // 编译报错！
}
```

### 1.3 Lambda 表达式语法

Lambda 本质是用更简洁的语法替代匿名内部类，格式为：

```
(形参列表) -> { 方法体 }
```

**对比示例**：

```java
// 传统匿名内部类
Swimmable s1 = new Swimmable() {
    @Override
    public void swim() {
        System.out.println("正在游泳");
    }
};

// Lambda 表达式（等价写法）
Swimmable s2 = () -> {
    System.out.println("正在游泳");
};
```

### 1.4 Lambda 省略规则

| 规则 | 说明 | 示例 |
|------|------|------|
| **参数类型可省略** | 编译器可自动推断参数类型 | `(a, b) -> a + b` |
| **单参数可省括号** | 只有一个参数时可省略小括号 | `x -> x * 2` |
| **单行代码可省大括号** | 方法体只有一行时可省略 `{}` 和 `return` | `(a, b) -> a + b` |

```java
// 完整写法
Calculator c1 = (int a, int b) -> { return a + b; };

// 省略参数类型
Calculator c2 = (a, b) -> { return a + b; };

// 省略大括号和 return（仅单行表达式）
Calculator c3 = (a, b) -> a + b;
```

**最佳实践**：能省则省，以代码可读性为准；多行逻辑不要强行省略大括号。

---

## 二、集合与包装类

### 2.1 ArrayList 集合

**概念**：长度**可变**的容器，底层基于数组实现，位于 `java.util` 包。

```java
import java.util.ArrayList;

ArrayList<String> names = new ArrayList<>();  // 泛型指定元素类型
names.add("张三");
names.add("李四");
System.out.println(names.size());           // 获取长度：2
System.out.println(names.get(0));           // 按索引获取："张三"
```

| 对比项 | 数组 | ArrayList |
|--------|------|-----------|
| 长度 | 固定，创建后不可变 | 动态可变 |
| 元素类型 | 基本类型 + 引用类型 | 只能存放**引用类型** |
| 增删 | 需手动移位 | 内置 `add` / `remove` |

### 2.2 包装类

**问题**：ArrayList 只能存引用类型，无法直接存放 `int`、`double` 等基本数据类型。

**解决**：使用对应的**包装类**——将基本类型"包装"成对象，存储在堆内存中。

| 基本类型 | 包装类 | 默认值 |
|----------|--------|--------|
| `int` | `Integer` | `null` |
| `double` | `Double` | `null` |
| `boolean` | `Boolean` | `null` |
| `char` | `Character` | `null` |
| `long` | `Long` | `null` |

```java
// 基本类型         包装类（引用类型，存储在堆内存）
int a = 10;        Integer b = 10;         // 自动装箱
ArrayList<Integer> list = new ArrayList<>();
list.add(100);                              // 自动装箱：int → Integer
int val = list.get(0);                      // 自动拆箱：Integer → int
```

**自动装箱/拆箱**（JDK 5+）：编译器自动完成基本类型与包装类的互转，开发者无需手动调用 `Integer.valueOf()` 或 `.intValue()`。

---

## 三、实战项目：gameTest 格斗游戏

### 3.1 项目概览

```
gameTest/src/com/heima/
├── App.java                          # 程序入口
├── domain/
│   ├── Character.java                # 角色基类
│   ├── HeroCharacter.java            # 英雄（玩家）类
│   ├── EnemyCharacter.java           # 敌人类
│   └── User.java                     # 用户类
└── ui/
    ├── Login.java                    # 登录注册界面
    └── Fighting.java                 # 战斗系统
```

**运行流程**：`App.main()` → `Login.start()`（登录/注册）→ `Fighting.gameStart()`（战斗主循环）

### 3.2 核心技术点梳理

| 技术点 | 应用位置 | 说明 |
|--------|----------|------|
| **继承** | `HeroCharacter` / `EnemyCharacter` 继承 `Character` | 复用基类的属性（HP、attack 等）和方法 |
| **方法重写（@Override）** | `EnemyCharacter.takeDamage()` | 敌人受伤时检查防御姿态，实现减伤 |
| **多态** | `ArrayList<EnemyCharacter>`、`Character` 类型变量引用子类对象 | 同一父类引用可指向不同子类对象 |
| **封装（private + getter/setter）** | `User` 类 | 用户名、密码等敏感字段私有化，对外暴露访问方法 |
| **ArrayList** | 多处使用（详见 3.3） | 动态管理用户列表、敌人列表、技能列表等 |
| **StringBuilder** | 多处使用（详见 3.4） | 高效拼接字符串 |
| **Scanner** | `Login` / `Fighting` | 接收键盘输入 |
| **Random** | `Login.getCode()` / `Fighting` | 生成随机数、随机敌人、随机恢复 |

### 3.3 ArrayList 在项目中的实际应用

`ArrayList` 是项目中使用最频繁的数据结构，共出现 **5 处关键应用**：

| 位置 | 泛型类型 | 作用 |
|------|----------|------|
| `Login.start()` | `ArrayList<User>` | 存储所有注册用户 |
| `Login.getCode()` | `ArrayList<Character>` | 存放大小写字母字符池（52个字符） |
| `Login.login()` 参数 | `ArrayList<User>` | 接收用户列表进行查找/校验 |
| `HeroCharacter.skillList` | `ArrayList<String>` | 存储英雄技能名称列表 |
| `Fighting.gameStart()` | `ArrayList<EnemyCharacter>` | 管理敌方角色池 |

**典型代码片段**：

```java
// 用户列表管理（Login.java）
ArrayList<User> users = new ArrayList<>();
// 注册时添加
users.add(u);
// 登录时遍历查找
for (int i = 0; i < users.size(); i++) {
    User user = users.get(i);
    // ...
}
```

```java
// 技能列表（HeroCharacter.java）
public ArrayList<String> skillList;
// 初始化后添加技能
player.skillList.add("普通攻击");
player.skillList.add("强力一击");
player.skillList.add("生命汲取");
```

### 3.4 StringBuilder 在项目中的实际应用

`StringBuilder` 用于高效拼接字符串，避免频繁创建 String 对象。项目中 **3 处使用**：

| 位置 | 用途 | 核心逻辑 |
|------|------|----------|
| `HeroCharacter.showSkill()` | 拼接技能列表字符串 | 遍历 `skillList`，技能间用 `, ` 分隔 |
| `Fighting.getBloodBar()` | 绘制血量条 | 用 `|` 和 `-` 拼接 20 格血条 |
| `User.creatID()` | 生成用户 ID | `"heima"` + 5 位随机数字 |
| `Login.getCode()` | 生成验证码 | 4 个随机字母 + 1 个数字，随机交换位置 |

```java
// 血条绘制（getBloodBar 方法）
StringBuilder sb = new StringBuilder();
sb.append(name).append("\t").append(": [");
for (int i = 0; i < barLength; i++) {
    sb.append(i < filled ? "|" : "-");
}
sb.append("]").append(HP).append("/").append(maxHP).append("HP");
// 输出示例：小石    : [|||||---------------] 50/100HP
```

```java
// ID 生成（creatID 方法）
StringBuilder sb = new StringBuilder("heima");
Random r = new Random();
for (int i = 0; i < 5; i++) {
    sb.append(r.nextInt(10));
}
// 输出示例：heima37291
```

### 3.5 核心类设计

#### Character（角色基类）

基类定义角色的通用属性和行为，供英雄和敌人继承。

```java
public class Character {
    public String name;     // 名称
    public int HP;          // 当前血量
    public int maxHP;       // 最大血量
    public int attack;      // 攻击力
    public int defense;     // 防御力

    public boolean isAlive() { return HP > 0; }       // 存活判定
    public void heal(int amount) { ... }               // 回复血量（不超过上限）
    public void takeDamage(int damage) { ... }         // 受到伤害（不低于0）
    public String show() { ... }                       // 展示属性
}
```

#### HeroCharacter（英雄类）

继承 `Character`，扩展 `skillList`（ArrayList）和 `showSkill` 方法。

```java
public class HeroCharacter extends Character {
    public ArrayList<String> skillList;

    public String showSkill() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < skillList.size(); i++) {
            sb.append(skillList.get(i));
            if (i < skillList.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }
}
```

#### EnemyCharacter（敌人类）

继承 `Character`，扩展 `skill`（技能类型）和 `defending`（防御姿态标记），**重写** `takeDamage` 实现防御减伤。

```java
public class EnemyCharacter extends Character {
    public String skill;
    public boolean defending;

    @Override
    public void takeDamage(int damage) {
        if (defending) {                    // 防御姿态：伤害减半
            damage = damage / 2 > 1 ? damage / 2 : 1;
            defending = false;              // 一次性，生效后关闭
        }
        super.takeDamage(damage);           // 调用父类受伤逻辑
    }
}
```

这是项目中**方法重写**的核心体现：子类不改父类签名，但扩展了行为。

#### User（用户类）

体现**封装**原则：字段 `private`，通过 `getter/setter` 访问，ID 在构造时自动生成。

```java
public class User {
    private String id;          // 自动生成：heima + 5位随机数
    private String username;    // 3-16位，只能字母开头
    private String password;    // 3-8位，字母+数字组合
    private boolean status;     // true=正常, false=禁用
}
```

### 3.6 战斗系统设计

#### 角色创建（createRole）

20 点自由分配到 HP / Attack / Defense，每点收益不同：

| 属性 | 每点收益 | 基础值 | 计算公式 |
|------|----------|--------|----------|
| HP | +10 | 100 | `100 + points × 10` |
| Attack | +2 | 10 | `10 + points × 2` |
| Defense | +1 | 0 | `points × 1` |

#### 回合制战斗流程

```
创建角色 → 随机敌人 → 显示血条 → 玩家回合 → 判定敌人死亡？
                                  ↓ 否
                              敌人回合 → 判定玩家死亡？→ 否 → 下一轮
                                  ↓ 是                    ↓ 是
                              游戏结束               ⬅ 下一局
```

#### 玩家技能

| 技能 | 消耗 | 效果 |
|------|------|------|
| 普通攻击 | 无 | `attack - enemy.defense` 伤害 |
| 强力一击 | 10 HP | `attack × 1.8` 伤害 |
| 生命汲取 | 10 HP | 随机恢复 1~20 HP |

#### 敌人 AI

50% 概率普通攻击 + 50% 概率技能：

| 敌人类型 | 技能 | 效果 |
|----------|------|------|
| 哥布林 | 普通攻击 | 基础伤害 |
| 幻影刺客 | 快速攻击 | 连续攻击 2 次（每次攻击力减半） |
| 超级坦克 | 防御姿态 | 下次受伤减半 |
| 邪恶法师 | 火球术 | `attack × 1.8` 伤害 |

#### 成长机制

- 每场战斗后随机恢复 20~40 HP
- 每 **3 胜场**获得属性提升：`maxHP +30`、`attack +5`、`defense +3`
- 每轮结束后所有敌人属性增强：`maxHP +10`、`attack +3`、`defense +2`

### 3.7 登录系统亮点

- **多重校验链**：长度（3-16 位）→ 格式（字母开头）→ 唯一性，层层过滤
- **验证码生成**：`List<Character>` 构建字母池 → `StringBuilder` 拼接 4 个随机字母 + 1 位数字 → `setCharAt` 随机交换位置确保数字可出现在任意位置
- **密码验证**：3 次机会，耗尽后 `setStatus(false)` 禁用账户
- **密码格式**：字母 + 数字组合（`checkPassword` 要求 `charCount > 0 && intCount > 0`），有效防止纯数字或纯字母弱密码

---

## 四、知识点与项目关联总结

| 今日知识点 | 项目中的应用 |
|------------|-------------|
| **函数式接口** | —（为后续学习铺垫，项目中未直接使用 Lambda） |
| **Lambda 表达式** | —（同理，掌握简化匿名内部类的思想） |
| **ArrayList** | 用户列表、敌人池、技能列表、验证码字符池 |
| **包装类** | `ArrayList` 泛型中所有基本类型均以包装类形式存储 |
| **继承** | `Character` 作为基类，英雄与敌人继承它 |
| **方法重写** | `EnemyCharacter` 重写 `takeDamage` 实现防御减伤 |
| **封装** | `User` 类字段私有化，通过 getter/setter 访问 |
| **多态** | 父类引用指向子类对象，`Character` 类型统一管理子类 |
| **StringBuilder** | 血条绘制、技能展示、ID 生成、验证码生成 |
| **Scanner** | 登录注册输入、战斗技能选择、属性分配 |
| **Random** | 验证码生成、随机敌人、随机恢复、50% 技能概率 |

---

## 五、今日要点速记

1. **Lambda** 只能简化**函数式接口**（只有一个抽象方法）的匿名内部类
2. **省略规则**：参数类型可省 → 单参数括号可省 → 单行代码大括号+return 可省
3. **ArrayList** 是长度可变的容器，只能存**引用类型**，基本类型需用**包装类**
4. **包装类**将基本类型转为对象存在堆内存，JDK 5+ 支持自动装箱/拆箱
5. **继承 + 重写**：`Character` 基类定义通用行为，子类按需重写（如 `EnemyCharacter.takeDamage`）
6. **封装**：`private` 字段 + 构造时自动生成 ID，外部通过 getter/setter 访问
7. **StringBuilder** 比 `+` 拼接高效，适用于循环中大量字符串操作场景

