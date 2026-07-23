# Day03 — 抽象类、接口、内部类、String、StringBuilder

> 源代码路径：`C:\Users\石天行\IdeaProjects\learning_test\src\com\tianxing\test`

---

## 一、抽象类（Abstract Class）

### 1.1 概念

父类中某些方法的实现无法确定（每个子类的具体行为不同），此时可以把方法声明为**抽象方法**，包含抽象方法的类就是**抽象类**。

- 将子类中**共有的方法抽取到父类中**，但父类无法给出统一实现
- 使用 `abstract` 关键字修饰

### 1.2 定义格式

```java
public abstract class Animal {
    private String name;

    // 构造方法（抽象类可以有构造方法）
    public Animal() {}
    public Animal(String name) {
        this.name = name;
    }

    // 抽象方法：没有方法体
    public abstract void eat();

    // 普通方法
    public String getName() { return name; }
}
```

### 1.3 核心规则

| 规则 | 说明 |
|------|------|
| **子类必须重写所有抽象方法** | 除非子类也是抽象类，否则编译报错 |
| **不能 `new` 抽象类对象** | `Animal a = new Animal();` 会报错 |
| **抽象类不一定有抽象方法** | 可以全是普通方法，意义在于阻止外界直接实例化 |
| **抽象类可以有构造方法** | 用于给成员变量赋值，`super()` 调用 |

### 1.4 构造方法的作用

```java
public abstract class Animal {
    private String name;
    public Animal(String name) { this.name = name; }
    public abstract void eat();
}

public class Cat extends Animal {
    public Cat(String name) { super(name); }  // 调用父类构造给 name 赋值
    @Override
    public void eat() { System.out.println("猫吃鱼"); }
}
```

### 1.5 作用总结

- **统一子类方法名称和格式**，制定规范
- 抽象类 = 模板方法模式的基础

---

## 二、接口（Interface）

### 2.1 概念

接口是一种**行为约束/规范**，定义了"能做什么"但不定义"怎么做"。当一个类中只有部分子类需要某种能力时，用接口来定义（类似"干爹"）。

- 使用 `interface` 关键字定义
- 使用 `implements` 关键字实现

### 2.2 定义与实现

```java
// 定义接口
public interface Swimable {
    void swim();  // 成员方法默认 public abstract
}

// 实现接口
public class Dog extends Animal implements Swimable {
    public Dog(String name) { super(name); }

    @Override
    public void eat() { System.out.println("狗吃骨头"); }

    @Override
    public void swim() { System.out.println("狗刨式游泳"); }
}
```

### 2.3 接口成员特点

| 成员类型 | 默认修饰符 | 说明 |
|----------|------------|------|
| **成员变量** | `public static final` | 只能是常量，必须赋值 |
| **成员方法** | `public abstract` | JDK8 以前只能是抽象方法 |
| **构造方法** | 无 | 接口没有构造方法 |

### 2.4 类与接口的关系

| 关系 | 语法 | 说明 |
|------|------|------|
| 类实现接口 | `class A implements B, C` | **多实现**：一个类可同时实现多个接口 |
| 类继承 + 实现 | `class A extends B implements C, D` | 可同时继承一个类并实现多个接口 |
| 接口继承接口 | `interface A extends B, C` | 接口之间可**单继承**也可**多继承** |

### 2.5 接口中的特殊方法（JDK 8+ / JDK 9+）

#### default（默认方法）— JDK 8

- 作用：升级接口时新增方法，已有子类不用立即重写
- 子类可以继承也可以重写

```java
public interface A {
    default void show() {
        System.out.println("接口A的默认方法");
    }
}
```

**冲突处理**：实现多个接口，其中默认方法名称相同 → **必须重写**：

```java
public interface A { default void show() { System.out.println("A"); } }
public interface B { default void show() { System.out.println("B"); } }

public class C implements A, B {
    @Override
    public void show() { System.out.println("C必须重写"); }  // 必须重写
}
```

#### static（静态方法）— JDK 8

- 只能通过 **接口名** 调用，不能通过实现类名或对象名调用

```java
public interface Utils {
    static void log(String msg) {
        System.out.println("[LOG] " + msg);
    }
}

// 调用
Utils.log("Hello");  // 正确
// new Impl().log("Hello");  // 错误
```

#### private（私有方法）— JDK 9

- 作用：抽取 default 方法和 static 方法中的**重复代码**

```java
public interface Calculator {
    default int addAndDouble(int a, int b) {
        return doubleValue(a + b);       // 调用私有方法
    }

    static int multiplyAndDouble(int a, int b) {
        return doubleValue(a * b);       // 调用静态私有方法
    }

    // 普通私有方法：给 default 方法使用
    private int doubleValue(int x) {
        return x * 2;
    }

    // 静态私有方法：给 static 方法使用
    private static int doubleValueStatic(int x) {
        return x * 2;
    }
}
```

---

## 三、内部类（Inner Class）

> 内部类是定义在另一个类内部的类，是外部类的一部分，单独存在没有意义。

### 3.1 成员内部类

定义在类中、方法外。可以访问外部类的所有成员（包括 `private`）。

```java
public class Outer {
    private int a = 10;

    // 成员内部类
    class Inner {
        public void show() {
            System.out.println(a);  // 直接访问外部类私有成员
        }
    }
}

// 创建对象
Outer.Inner oi = new Outer().new Inner();
```

也可以在外部类中提供获取内部类实例的方法：

```java
public class Outer {
    class Inner { }

    public Inner getInstance() {
        return new Inner();
    }
}
```

### 3.2 静态内部类

用 `static` 修饰的内部类。**只能访问外部类的静态成员**，访问非静态成员需要先创建外部类对象。

```java
public class Outer {
    static int a = 10;
    int b = 20;

    static class StaticInner {
        public void show() {
            System.out.println(a);        // OK，访问静态变量
            // System.out.println(b);     // 报错！不能访问非静态
            Outer o = new Outer();        // 需要先创建对象
            System.out.println(o.b);      // OK
        }
    }
}

// 创建对象（不需要外部类实例）
Outer.StaticInner si = new Outer.StaticInner();
```

**记忆口诀**：静态只能访问静态。

### 3.3 局部内部类

定义在**方法内部**，作用域仅限于该方法，类似于局部变量。

```java
public class Outer {
    public void method() {
        class LocalInner {
            public void print() {
                System.out.println("我是局部内部类");
            }
        }
        LocalInner li = new LocalInner();
        li.print();
    }
}
```

### 3.4 匿名内部类（重点）

没有名字的内部类，用于**简化只使用一次的类**的创建。

**格式**：

```java
new 类名/接口名() {
    // 重写方法
};
```

**示例一：接口的匿名实现**

```java
// 传统写法
class MySwimable implements Swimable {
    @Override
    public void swim() { System.out.println("游泳"); }
}
Swimable s1 = new MySwimable();

// 匿名内部类写法
Swimable s2 = new Swimable() {
    @Override
    public void swim() {
        System.out.println("游泳");
    }
};
```

**示例二：类的匿名子类**

```java
Animal cat = new Animal("猫") {
    @Override
    public void eat() {
        System.out.println("猫吃鱼");
    }
};
```

> **本质**：创建了一个没有名字的**子类/实现类**对象。

---

## 四、Java 常见 API

> API（Application Programming Interface）：应用程序编程接口，即 Java 提供的现成类库。

常用包：

- `java.lang` — 核心类（String、Math、System），自动导入
- `java.util` — 工具类（Scanner、Random、Arrays）
- `java.io` — 输入输出

---

## 五、String 类

> `String` 位于 `java.lang` 包，表示不可变的字符串。

### 5.1 构造方法

| 构造方法 | 说明 |
|----------|------|
| `new String()` | 创建空字符串 |
| `new String(String s)` | 根据字符串创建 |
| `new String(char[] arr)` | 根据字符数组创建 |
| `new String(byte[] arr)` | 根据字节数组创建（解码） |

### 5.2 串池机制（String Pool）

```java
String s1 = "abc";             // 字面量，存入串池
String s2 = "abc";             // 从串池复用同一对象
String s3 = new String("abc"); // new 创建，堆内存中新建对象，不复用
```

- `s1 == s2` → `true`（同一地址）
- `s1 == s3` → `false`（`new` 无法复用串池）
- **new 出来的字符串更消耗内存，不推荐**

| 创建方式 | 内存位置 | 是否复用 |
|----------|----------|----------|
| 字面量 `"abc"` | 串池（堆中） | 可复用 |
| `new String("abc")` | 堆（新对象） | 不复用 |

### 5.3 字符串比较

| 方法 | 说明 |
|------|------|
| `boolean equals(Object obj)` | 比较内容是否完全一致（区分大小写） |
| `boolean equalsIgnoreCase(String s)` | 忽略大小写比较内容 |

```java
String s1 = "Hello";
String s2 = "hello";
s1.equals(s2);              // false
s1.equalsIgnoreCase(s2);    // true
```

### 5.4 遍历字符串

```java
String s = "Hello";
// charAt(int index) 根据索引返回字符
System.out.println(s.charAt(0));        // H

// length() 返回字符串长度
for (int i = 0; i < s.length(); i++) {
    System.out.print(s.charAt(i));      // H e l l o
}
```

### 5.5 截取字符串

```java
String s = "HelloWorld";
s.substring(0, 5);    // "Hello" — 从 begin 到 end-1
s.substring(5);       // "World" — 从 begin 到末尾
```

### 5.6 字符串替换

```java
String s = "abcabc";
s.replace("a", "A");  // "AbcAbc" — 返回新字符串，原字符串不变
```

### 5.7 其他常用方法

| 方法 | 说明 |
|------|------|
| `toCharArray()` | 转为字符数组 |
| `indexOf(String s)` | 返回第一次出现的索引，未找到返回 -1 |
| `split(String regex)` | 按正则切割 |
| `trim()` | 去除首尾空格 |
| `startsWith(String s)` | 是否以指定字符串开头 |

---

## 六、StringBuilder

> 可变字符串序列，适合频繁拼接的场景，比 String 拼接更高效（避免创建大量临时对象）。

### 6.1 构造方法

| 构造方法 | 说明 |
|----------|------|
| `new StringBuilder()` | 空容器，默认容量 16 |
| `new StringBuilder(String s)` | 包含初始字符串 |

### 6.2 常用方法

| 方法 | 说明 |
|------|------|
| `append(任意类型)` | 追加数据，支持链式调用 |
| `reverse()` | 反转内容 |
| `length()` | 获取字符个数（不是容量） |
| `toString()` | 转为 String |

```java
StringBuilder sb = new StringBuilder();
sb.append("Hello").append(" ").append("World");  // 链式调用
System.out.println(sb.toString());   // "Hello World"
System.out.println(sb.reverse());    // "dlroW olleH"
```

### 6.3 数据脱敏示例

```java
// 手机号脱敏：138****1234
String phone = "13812341234";
StringBuilder sb = new StringBuilder(phone);
sb.replace(3, 7, "****");
System.out.println(sb);  // "138****1234"
```

---

## 七、字符串算法题思路提示

### 7.1 常见题型

| 题型 | 核心思路 |
|------|----------|
| **反转字符串** | `sb.reverse()` 或双指针交换 `char[]` |
| **统计字符次数** | 遍历 `charAt()` + 计数器 |
| **判断回文** | 首尾指针向中间比较 |
| **字符串拼接（大量）** | 使用 `StringBuilder` 而非 `+` |
| **查找子串位置** | `indexOf()` 循环查找 |
| **字符替换/脱敏** | `substring` + `replace` 或 `StringBuilder.replace` |
| **字符串切割** | `split()` + 遍历 |

### 7.2 实用技巧

```java
// 1. 字符串转字符数组
char[] arr = s.toCharArray();

// 2. 字符数组转字符串
String s = new String(arr);

// 3. 判断字符串是否为空
if (s != null && !s.isEmpty()) { ... }

// 4. 遍历并统计
for (int i = 0; i < s.length(); i++) {
    char c = s.charAt(i);
    // 处理 c
}
```

---

> **小结**：Day03 的核心是面向对象的高级特性——抽象类定义模板规范、接口定义行为契约、内部类组织代码结构，以及 Java 中最常用的字符串处理。这些内容是后续集合框架和 IO 流的基础。
