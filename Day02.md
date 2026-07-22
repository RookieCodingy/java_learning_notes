
# Day02：Java 面向对象核心（上）—— static / final / 枚举 / 封装 / 继承 / 方法重写 / 多态

> 源代码路径：`C:\Users\石天行\IdeaProjects\learning_test\src\com\tianxing\test`

---

## 一、static 关键字

`static` 表示**静态**，属于**类**而不属于某个具体对象。被 static 修饰的成员随类的加载而加载，优先于对象存在，被该类的所有对象**共享**。

### 1.1 静态变量（类变量）

- 用 `static` 修饰的成员变量称为**静态变量**或**类变量**。
- 该变量属于类本身，**所有实例共享同一份数据**。
- 调用方式：`类名.变量名`（推荐）或 `对象名.变量名`。

```java
public class Student {
    private String name;          // 实例变量：每个对象各自一份
    public static int count = 0;  // 静态变量：所有对象共享

    public Student(String name) {
        this.name = name;
        count++;   // 每 new 一个对象 count 就 +1
    }
}
// 使用: Student.count
```

### 1.2 静态方法

- 用 `static` 修饰的方法称为**静态方法**。
- 静态方法中**不能直接访问非静态成员**（实例变量/实例方法），因为静态方法中没有 `this` 引用。
- 非静态方法可以访问静态成员。
- 常用于**工具类**（如 `Math`、`Arrays`、`Collections`）。

```java
public class MathUtils {
    // 静态工具方法
    public static int add(int a, int b) {
        return a + b;
    }

    public static int max(int a, int b) {
        return a > b ? a : b;
    }
}
// 调用: MathUtils.add(3, 5)
```

### 1.3 静态代码块

- 格式：`static { ... }`
- **类加载时执行一次且仅执行一次**，在构造方法之前执行。
- 常用于**初始化静态资源**（加载驱动、读取配置文件等）。

```java
public class DatabaseConfig {
    public static String url;
    public static String username;

    static {
        // 类加载时自动执行，只执行一次
        url = "jdbc:mysql://localhost:3306/mydb";
        username = "root";
        System.out.println("静态代码块执行：数据库配置已加载");
    }
}
```

### static 小结

| 成员类型 | 归属 | 加载时机 | 能否访问非静态 | 调用方式 |
|---------|------|---------|--------------|---------|
| 静态变量 | 类 | 类加载时 | — | `类名.变量` |
| 静态方法 | 类 | 类加载时 | ❌（无 this） | `类名.方法()` |
| 实例变量 | 对象 | new 对象时 | — | `对象.变量` |
| 实例方法 | 对象 | new 对象时 | ✅ | `对象.方法()` |

---

## 二、final 关键字

`final` 意为**最终的、不可改变的**。可修饰类、方法、变量。

### 2.1 final 修饰类

- 被 final 修饰的类**不能被继承**（断子绝孙类）。
- 典型例子：`String`、`System`、`Math`。

```java
public final class StringUtils {   // 不可被继承
    // ...
}
// class SubUtils extends StringUtils {}  // ❌ 编译错误
```

### 2.2 final 修饰方法

- 被 final 修饰的方法**可以被子类继承，但不能被重写**。

```java
public class Parent {
    public final void show() {
        System.out.println("父类最终方法，子类不可重写");
    }
}

public class Child extends Parent {
    // @Override
    // public void show() { ... }   // ❌ 编译错误
}
```

### 2.3 final 修饰变量

- 修饰**基本类型**：值不可改变。
- 修饰**引用类型**：引用地址不可改变，但对象内部属性可以改。
- 命名规范：**全大写 + 下划线分隔**（`MAX_VALUE`）。

```java
public class Constants {
    public static final double PI = 3.1415926;           // 基本类型常量
    public static final String APP_NAME = "MyApp";       // 引用类型常量（String 不可变）
    public static final int[] ARR = {1, 2, 3};           // 引用类型，ARR 不可指向新数组，但 ARR[0] 可改
}
```

---

## 三、枚举（enum）

### 3.1 基本定义

枚举是一种特殊的类，用于定义**一组固定常量**。

```java
public enum Season {
    SPRING, SUMMER, AUTUMN, WINTER
}
```

### 3.2 核心理解

- **每一个枚举项都是该枚举类的对象**。
- 枚举的构造方法**默认用 `private` 修饰**（不允许外部 new）。
- 内置方法 `values()` 返回所有枚举项的数组。

### 3.3 带属性与行为的枚举

```java
public enum Season {
    // 枚举项（自动调用 private 构造方法）
    SPRING("春天", "温暖"),
    SUMMER("夏天", "炎热"),
    AUTUMN("秋天", "凉爽"),
    WINTER("冬天", "寒冷");

    // 属性
    private final String chineseName;
    private final String description;

    // 构造方法（默认 private）
    Season(String chineseName, String description) {
        this.chineseName = chineseName;
        this.description = description;
    }

    // 行为
    public String getChineseName() {
        return chineseName;
    }

    public String getDescription() {
        return description;
    }
}

// 使用示例
public class Test {
    public static void main(String[] args) {
        Season s = Season.SPRING;
        System.out.println(s.getChineseName());  // 春天

        // values() 获取所有枚举项
        for (Season season : Season.values()) {
            System.out.println(season + ": " + season.getDescription());
        }
    }
}
```

### 3.4 枚举 vs 常量

| 对比维度 | 普通常量 `static final` | 枚举 `enum` |
|---------|------------------------|------------|
| 类型安全 | 弱（可传任意 int） | 强（编译期检查） |
| 可读性 | 差 | 好 |
| 可扩展方法 | 不支持 | 支持属性和方法 |

---

## 四、封装（Day01 回顾）

封装是面向对象三大特征之一，核心思想是**隐藏内部实现细节，暴露可控的访问接口**。

### 实现方式

- 用 `private` 修饰成员变量，禁止外部直接访问。
- 提供 `public` 的 **getter / setter** 方法控制访问。

```java
public class Person {
    private String name;    // 私有：外部不可直接访问
    private int age;

    // Getter：获取值
    public String getName() {
        return name;
    }

    // Setter：设置值，可在方法内加入校验逻辑
    public void setAge(int age) {
        if (age >= 0 && age <= 150) {
            this.age = age;
        } else {
            System.out.println("年龄不合法！");
        }
    }

    public int getAge() {
        return age;
    }
}
```

### 封装的意义

1. **安全性**：防止外部直接篡改数据，可在 setter 中校验。
2. **可维护性**：修改内部实现不影响调用方。
3. **解耦**：调用方只依赖接口，不依赖实现细节。

---

## 五、继承（extends）

### 5.1 基本概念

继承是子类获取父类属性和行为的机制，用 `extends` 关键字。

```java
public class Animal {
    public String name;

    public void eat() {
        System.out.println(name + " 正在吃东西");
    }
}

public class Dog extends Animal {
    public void bark() {
        System.out.println(name + " 汪汪叫");
    }
}
```

### 5.2 继承特点

| 特点 | 说明 |
|-----|------|
| **单继承** | 一个子类只能有一个**直接**父类（Java 不支持多继承） |
| **多重继承** | 支持多层继承：A → B → C（`C extends B extends A`） |
| **就近原则** | 访问变量/方法时，先从子类找，找不到再向上找父类 |
| **Object 类** | 所有类的最终祖先，不写 extends 默认继承 `Object` |

### 5.3 this 与 super 的区别

| 关键字 | 指向 | 常见用途 |
|-------|------|---------|
| `this` | 当前对象（本类） | 访问本类成员变量/方法、调用本类构造方法 |
| `super` | 父类对象 | 访问父类成员变量/方法、调用父类构造方法 |

```java
public class Zi extends Fu {
    String name = "子类";

    public void show() {
        String name = "局部";
        System.out.println(name);       // 局部变量（就近原则）
        System.out.println(this.name);  // 本类成员变量 → "子类"
        System.out.println(super.name); // 父类成员变量
    }
}
```

### 5.4 构造方法中的 super()

- 子类构造方法的第一行**默认隐式调用** `super()`，先完成父类初始化。
- 如果要调用父类有参构造，必须**显式写在第一行**：`super(参数)`。

```java
public class Person {
    private String name;
    public Person(String name) {
        this.name = name;
    }
}

public class Student extends Person {
    private int score;
    public Student(String name, int score) {
        super(name);      // 必须显式调用父类有参构造
        this.score = score;
    }
}
```

---

## 六、方法重写（Override）

### 6.1 定义

子类中定义了与父类**方法签名相同**（方法名、参数列表一致）的方法，覆盖父类的实现。

### 6.2 @Override 注解

用于编译器校验是否真的是重写（方法名拼错 / 参数不一致时会报错），**强烈建议加上**。

### 6.3 重写规则

| 维度 | 规则 |
|-----|------|
| 方法签名 | 方法名 + 参数列表必须**完全一致** |
| 返回值 | 子类返回值 ≤ 父类返回值（可以是父类返回值的子类） |
| 访问权限 | 子类 ≥ 父类（父类 public → 子类必须 public；父类 protected → 子类 protected 或 public） |
| 异常 | 子类抛出的异常 ≤ 父类 |
| 静态方法 | **不能被重写**（静态方法属于类，不属于对象） |
| final 方法 | 不能被重写 |

### 6.4 示例

```java
public class Phone {
    public void call() {
        System.out.println("打电话");
    }
}

public class SmartPhone extends Phone {
    @Override
    public void call() {           // 重写父类方法
        super.call();              // 可选：保留父类功能
        System.out.println("视频通话");
    }
}
```

### 6.5 重写 vs 重载

| 对比 | 方法重写（Override） | 方法重载（Overload） |
|-----|-------------------|-------------------|
| 发生位置 | 父子类之间 | 同一个类中 |
| 方法签名 | 完全相同 | 方法名相同，参数列表**不同** |
| 返回值 | ≤ 父类 | 无要求 |
| 访问权限 | ≥ 父类 | 无要求 |
| 多态体现 | 运行时多态 | 编译时多态 |

---

## 七、多态（Polymorphism）

### 7.1 核心概念

**多态**：同一个行为（方法）在不同对象上表现出不同的形态。本质是**父类引用指向子类对象**。

```java
Fu f = new Zi();   // 父类引用 f 指向子类对象
```

### 7.2 多态中成员访问规则（核心口诀）

| 成员类型 | 编译期（看左边） | 运行期（看右边） |
|---------|---------------|---------------|
| **成员变量** | 看左边（父类） | 看左边（父类） |
| **成员方法** | 看左边（父类） | 看右边（子类） |

> **口诀**：变量调用编译运行都看左，方法调用编译看左运行看右。

```java
public class Animal {
    public String name = "动物";     // 父类变量
    public void eat() {
        System.out.println("动物吃东西");
    }
}

public class Cat extends Animal {
    public String name = "猫";       // 子类变量
    @Override
    public void eat() {
        System.out.println("猫吃鱼");
    }
}

// 测试
Animal a = new Cat();
System.out.println(a.name);  // 输出: "动物"  （变量：编译运行都看左边）
a.eat();                     // 输出: "猫吃鱼"（方法：编译看左，运行看右）
```

### 7.3 强制类型转换（向下转型）

将父类引用转换为子类类型，以便调用子类特有方法。

```java
Animal a = new Cat();
Cat c = (Cat) a;         // 向下转型
c.catchMouse();          // 调用 Cat 特有方法
```

> ⚠️ 转换前必须用 `instanceof` 判断，否则可能抛出 `ClassCastException`。

### 7.4 instanceof 类型判断

```java
Animal a = new Cat();

if (a instanceof Cat) {
    Cat c = (Cat) a;
    c.catchMouse();
} else if (a instanceof Dog) {
    Dog d = (Dog) a;
    d.guardHouse();
}
```

### 7.5 多态弊端

**不能调用子类特有功能**。父类引用只能调用父类中定义的方法，即使实际对象是子类。

```java
Animal a = new Cat();
a.eat();               // ✅ 可以（父类有这个方法）
// a.catchMouse();     // ❌ 编译错误！父类没有 catchMouse()
```

**解决方案**：向下转型（如 7.3 所示）。

### 7.6 多态的应用场景

```java
// 场景：方法参数用父类，可接收任意子类
public void feed(Animal a) {
    a.eat();
}
// 调用
feed(new Cat());    // 猫吃鱼
feed(new Dog());    // 狗吃骨头
feed(new Pig());    // 猪吃饲料
```

### 7.7 多态小结

| 要素 | 说明 |
|-----|------|
| 前提 | 有继承 / 实现关系 + 方法重写 |
| 格式 | `父类 变量 = new 子类();` |
| 变量 | 编译运行都看左（父类） |
| 方法 | 编译看左，运行看右（子类重写的方法） |
| 向下转型 | `子类 z = (子类) f;`，配合 `instanceof` 安全检查 |
| 优势 | 代码复用、扩展性强、解耦 |
| 弊端 | 不能直接调用子类特有方法，需向下转型 |

---

## 八、Day02 知识体系总结

```
                        ┌── 静态变量（类共享）
              ┌─ static ─┼── 静态方法（工具类，无 this）
              │         └── 静态代码块（类加载时执行一次）
              │
              │         ┌── 修饰类：不可继承
核心关键字 ────┼─ final ─┼── 修饰方法：不可重写
              │         └── 修饰变量：值不可改（常量）
              │
              └─ enum  ─── 固定常量集合，枚举项即对象，构造默认 private，values() 遍历
              
              ┌─ 封装   ─── private + getter/setter（安全、可维护）

              │         ┌── extends（单继承、多重继承 A→B→C）
面向对象 ────┼─ 继承 ──┼── 就近原则 / this（本类） vs super（父类）
三大特征      │         └── 构造方法 super() 放第一行
              │
              │         ┌── 子类重写父类方法 + @Override 注解
              └─ 多态 ──┼── Fu f = new Zi();  变量看左，方法编译看左运行看右
                        ├── 向下转型 (Zi) f + instanceof 安全判断
                        └── 弊端：不能调子类特有方法 → 需转型
```

