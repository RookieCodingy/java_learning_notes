# Java 学习 Day01：算法练习 + 面向对象基础


---

## 一、算法练习

### 1.1 testDemo2 — 二路归并找中位数

#### 问题描述

给定两个**正序（升序）数组** `arr1` 和 `arr2`，将它们合并为一个有序数组，然后输出合并后数组的**中位数**。

> 示例：`arr1 = [1, 3, 5, 7, 9, 11]`，`arr2 = [2, 4, 6, 8]`
> 合并后 → `[1, 2, 3, 4, 5, 6, 7, 8, 9, 11]`，中位数为 `(5 + 6) / 2 = 5.5`

#### 核心思路：双指针归并

同时维护两个数组各自的索引 `index1` 和 `index2`，每次比较两个指针所指元素的大小，把较小的放入结果数组并移动对应指针。

```
arr1: [1, 3, 5, 7, 9, 11]    index1 →
arr2: [2, 4, 6, 8]            index2 →
        ↓ 比较 1 vs 2 → 取 1，index1++
        ↓ 比较 3 vs 2 → 取 2，index2++
        ...依此类推
```

#### 难点：数组越界处理

当一个数组已被取完（指针超出长度），应该直接从另一个数组取元素，否则访问 `arr[index]` 会抛出 `ArrayIndexOutOfBoundsException`。

```java
// 越界判断：arr1 取完 → 只从 arr2 取
if (index1 >= arr1.length) {
    arr[i] = arr2[index2];
    index2++;
    continue;  // 跳过本轮后续比较
}
// arr2 取完 → 只从 arr1 取
if (index2 >= arr2.length) {
    arr[i] = arr1[index1];
    index1++;
    continue;
}
```

#### 中位数计算

- **奇数长度**：直接取中间元素 `arr[len / 2]`
- **偶数长度**：取中间两个元素的平均值 `(arr[len/2] + arr[len/2 - 1]) / 2.0`

> 注意 `/ 2.0` 保证结果为 `double` 类型，避免整数除法截断。

#### 方法设计

将"合并 + 求中位数"封装为独立方法 `findMedian(int[] arr1, int[] arr2)`，`main` 中只负责准备数据并调用，体现**单一职责**原则。

#### 完整代码

```java
package com.tianxing.test;

public class testDemo2 {
    public static void main(String[] args) {
        int[] arr1 = new int[]{1, 3, 5, 7, 9, 11};
        int[] arr2 = new int[]{2, 4, 6, 8};
        System.out.println(findMedian(arr1, arr2));
    }

    public static double findMedian(int[] arr1, int[] arr2) {
        int index1 = 0, index2 = 0;
        int[] arr = new int[arr1.length + arr2.length];

        for (int i = 0; i < arr.length; i++) {
            if (index1 >= arr1.length) {
                arr[i] = arr2[index2++];
                continue;
            }
            if (index2 >= arr2.length) {
                arr[i] = arr1[index1++];
                continue;
            }
            arr[i] = (arr1[index1] < arr2[index2]) ? arr1[index1++] : arr2[index2++];
        }

        // 求中位数
        if (arr.length % 2 == 0) {
            return (arr[arr.length / 2] + arr[arr.length / 2 - 1]) / 2.0;
        } else {
            return arr[arr.length / 2] / 1.0;
        }
    }
}
```

#### 知识点总结

| 知识点 | 说明 |
|--------|------|
| 双指针归并 | 两个有序数组合并为一个有序数组的经典算法 |
| 数组越界 | 指针超出数组长度时必须做边界判断 |
| 索引变量 | 用 `index1`、`index2` 追踪两个数组当前读取位置 |
| 中位数公式 | 奇数取中间，偶数取两中间值的平均 |
| 方法封装 | 核心逻辑抽成方法，提高可读性和复用性 |

---

### 1.2 testDemo3 — 接雨水

#### 问题描述

给定一个非负整数数组表示柱状图，每个柱子宽度为 1，计算下雨后能接多少单位的雨水。

> 示例：`height = [0, 1, 0, 2, 1, 0, 1, 3, 2, 1, 2, 1]`，输出 `6`

```
         █
     █░░░██░█
 █░░██░██████
 ------------
 0 1 0 2 1 0 1 3 2 1 2 1
    ↑        ↑       ↑
    水=1     水=2     水=1  ...
```

#### 解法：动态规划（三次遍历）

**关键洞察**：每个位置能接多少水 = 该位置"左边最高墙"和"右边最高墙"的较小值 - 当前位置的墙高度。

```
位置 i 的水量 = min(leftMax[i], rightMax[i]) - height[i]
```

**步骤**：

1. **左→右遍历**：计算每个位置 i 的左侧最高墙 `leftMax[i]`
2. **右→左遍历**：计算每个位置 i 的右侧最高墙 `rightMax[i]`
3. **取交集**：`minArr[i] = min(leftMax[i], rightMax[i])`
4. **减去墙面积**：`总水量 = sum(minArr) - sum(height)`

```java
// 步骤1：从左往右 → leftMax[i] 是 [0..i] 的最大值
int max = arr[0];
for (int i = 0; i < arr.length; i++) {
    if (max > arr[i])
        leftarr[i] = max;
    else {
        max = arr[i];
        leftarr[i] = max;
    }
}

// 步骤2：从右往左 → rightMax[i] 是 [i..n-1] 的最大值（同理）

// 步骤3：取左右最小值的交集
for (int i = 0; i < arr.length; i++) {
    minarr[i] = Math.min(leftarr[i], rightarr[i]);
}

// 步骤4：总面积 - 墙面积 = 水量
int sum = 0;
for (int v : minarr) sum += v;
for (int v : arr)   sum -= v;
```

#### 思考：其他解法

| 解法 | 时间复杂度 | 空间复杂度 | 核心思想 |
|------|-----------|-----------|---------|
| 动态规划（当前） | O(n) | O(n) | 预计算左右最大值数组 |
| 双指针 | O(n) | O(1) | 左右指针相向移动，维护 `leftMax` 和 `rightMax`，谁小就计算谁那一侧的水量 |
| 单调栈 | O(n) | O(n) | 栈中维护递减序列，遇到更高的墙就弹出并计算凹槽水量 |

> **双指针优化思路**：不需要 O(n) 额外空间，用两个指针 `left`、`right` 从两端向中间移动，维护两个变量 `leftMax` 和 `rightMax`。当 `leftMax < rightMax` 时，`left` 位置的水量由 `leftMax` 决定，计算后 `left++`；否则由 `rightMax` 决定，`right--`。

#### 完整代码

```java
package com.tianxing.test;

public class testDemo3 {
    public static void main(String[] args) {
        int[] arr = {0, 1, 0, 2, 1, 0, 1, 3, 2, 1, 2, 1};

        // 左→右：leftarr[i] = max(height[0..i])
        int[] leftarr = new int[arr.length];
        int max = arr[0];
        for (int i = 0; i < arr.length; i++) {
            max = Math.max(max, arr[i]);
            leftarr[i] = max;
        }

        // 右→左：rightarr[i] = max(height[i..n-1])
        int[] rightarr = new int[arr.length];
        max = arr[arr.length - 1];
        for (int i = arr.length - 1; i >= 0; i--) {
            max = Math.max(max, arr[i]);
            rightarr[i] = max;
        }

        // 取交集 → 减墙
        int[] minarr = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            minarr[i] = Math.min(leftarr[i], rightarr[i]);
        }

        int sum = 0;
        for (int v : minarr) sum += v;
        for (int v : arr)   sum -= v;

        System.out.println(sum);  // 输出 6
    }
}
```

#### 知识点总结

| 知识点 | 说明 |
|--------|------|
| 动态规划思维 | 将问题拆解为子问题：每个位置的水量仅取决于左右两侧最高墙 |
| 双向遍历 | 左→右、右→左各一次，构建辅助数组 |
| 取交集 | `min(leftMax, rightMax)` 是"木桶效应"——水位由较矮的一侧决定 |
| 减去墙面积 | `totalWater = sum(minArr) - sum(height)`，简洁高效 |
| 双指针优化 | 省去 O(n) 额外空间，将空间复杂度降至 O(1) |
| 单调栈 | 另一种视角：找"凹槽"计算水量 |

---

### 1.3 testDemo4 — 彩票中奖问题

#### 问题描述

模拟大乐透彩票系统：
- **前区**：1~35 中选 5 个不重复号码
- **后区**：1~12 中选 2 个不重复号码
- 前区与后区之间允许数字重复
- 根据前后区命中个数判断奖级

#### 需求拆分（分而治之）

| 步骤 | 需求 | 对应方法 |
|------|------|---------|
| 1 | 随机生成一注彩票号码 | `createLotteryNumbers()` |
| 2 | 用户输入购买号码 | `buyLotteryNumbers()` |
| 3 | 判断是否中奖及奖级 | `judgeLotteryNumbers(arr1, arr2)` |
| 辅助 | 判断数字在指定范围内是否重复 | `contains(arr, num, start, end)` |

#### 关键实现细节

**1. 去重逻辑**

用辅助方法 `contains(int[] arr, int num, int start, int end)` 检查 `arr[start..end]` 是否已包含 `num`。

```java
public static boolean contains(int[] arr, int num, int start, int end) {
    for (int i = start; i <= end; i++) {
        if (arr[i] == num) return true;
    }
    return false;
}
```

生成时若发现重复，`i--` 回退索引重新生成：

```java
if (contains(arr, number, 0, 4)) {
    i--;  // 本次无效，重新生成
} else {
    arr[i] = number;
}
```

**2. Scanner 输入校验**

用户输入时需要两层校验：范围校验（1~35 / 1~12）+ 重复校验。

```java
if (number < 1 || number > 35) {
    System.out.println("输入的数字超出范围，请重新输入");
    i--;
} else if (contains(arr, number, 0, 4)) {
    System.out.println("输入的数字重复，请重新输入");
    i--;
} else {
    arr[i] = number;
}
```

**3. 中奖判定**

分别统计前区命中 `count1` 和后区命中 `count2`，然后匹配奖级规则：

| 奖级 | 条件（前区+后区） |
|------|-----------------|
| 一等奖 | 5+2 |
| 二等奖 | 5+1 |
| 三等奖 | 5+0 或 4+2 |
| 四等奖 | 4+1 或 3+2 |
| 五等奖 | 4+0 或 3+1 或 2+2 |
| 六等奖 | 3+0 / 1+2 / 2+1 / 0+2 |

#### 完整代码

```java
package com.tianxing.test;

import java.util.Random;
import java.util.Scanner;

public class testDemo4 {
    public static void main(String[] args) {
        int[] arr1 = createLotteryNumbers();       // 系统生成
        int[] arr2 = buyLotteryNumbers();          // 用户购买
        judgeLotteryNumbers(arr1, arr2);           // 判定中奖
    }

    // 生成彩票号码
    public static int[] createLotteryNumbers() {
        int[] arr = new int[7];
        Random r = new Random();

        // 前区 1-35，5个不重复
        for (int i = 0; i < 5; i++) {
            int number = r.nextInt(1, 36);  // [1, 36) → 1~35
            if (!contains(arr, number, 0, 4)) {
                arr[i] = number;
            } else {
                i--;
            }
        }
        // 后区 1-12，2个不重复
        for (int i = 5; i < 7; i++) {
            int number = r.nextInt(12) + 1;
            if (!contains(arr, number, 5, 6)) {
                arr[i] = number;
            } else {
                i--;
            }
        }
        return arr;
    }

    // 判断数字在指定区间是否重复
    public static boolean contains(int[] arr, int num, int start, int end) {
        for (int i = start; i <= end; i++) {
            if (arr[i] == num) return true;
        }
        return false;
    }

    // 用户购买
    public static int[] buyLotteryNumbers() { /* ... 同前区/后区输入校验逻辑 ... */ }

    // 判定中奖
    public static void judgeLotteryNumbers(int[] arr1, int[] arr2) { /* ... */ }
}
```

#### 知识点总结

| 知识点 | 说明 |
|--------|------|
| 需求拆分 | 将一个复杂问题拆为多个子需求，逐个方法实现 |
| `Scanner` 类 | `Scanner(System.in)` 读取控制台输入，`nextInt()` 读取整数 |
| `Random` 类 | `nextInt(bound)` 生成 `[0, bound)` 随机数；`nextInt(origin, bound)` 生成 `[origin, bound)` |
| 去重技巧 | 辅助方法 `contains` 配合 `i--` 索引回退 |
| 输入校验 | 范围 + 重复双重校验，不合法时提示并重新输入 |
| 奖级分支 | 多个 `if-else if` 匹配不同命中组合 |

---

## 二、面向对象

### 2.1 类和对象

| 概念 | 说明 |
|------|------|
| **类（Class）** | 对一类事物的抽象描述，是创建对象的模板/蓝图 |
| **对象（Object）** | 根据类创建出来的具体实例，拥有类中定义的属性和行为 |

```java
// 类：学生模板
public class Student {
    String name;   // 属性
    int age;       // 属性
    void study() { System.out.println("学习中..."); }  // 行为
}

// 对象：具体的学生
Student s1 = new Student();
s1.name = "张三";
s1.age = 20;
```

---

### 2.2 封装：private + setter/getter

#### 为什么需要 private

直接用 `对象.属性` 赋值无法做数据校验（如年龄不能为负数）。用 `private` 隐藏属性，通过方法控制访问，这就是**封装**。

```java
public class Student {
    private String name;  // 外部不能直接访问
    private int age;

    // getter：获取属性值
    public String getName() { return name; }

    // setter：设置属性值，可加入校验逻辑
    public void setAge(int age) {
        if (age >= 0 && age <= 150) {
            this.age = age;
        } else {
            System.out.println("年龄不合法");
        }
    }
}
```

---

### 2.3 构造方法

> 构造方法是一种特殊方法，**名称与类名相同，没有返回值**（连 `void` 也不写），在 `new` 创建对象时自动调用。

```java
public class Student {
    private String name;
    private int age;

    // 无参构造方法
    public Student() { }

    // 有参构造方法：创建对象时直接赋值
    public Student(String name, int age) {
        this.name = name;
        this.age = age;
    }
}

// 使用
Student s = new Student("张三", 20);  // 调用有参构造
```

| 规则 | 说明 |
|------|------|
| 不写构造方法 | Java 自动提供默认无参构造 |
| 写了有参构造 | 默认无参构造**消失**，需要手动补写 |
| 构造方法可重载 | 可以有多个，参数列表不同即可 |

---

### 2.4 快捷键：Alt + Insert

在 IntelliJ IDEA 中，`Alt + Insert` 可快速生成：
- **构造方法**（Constructor）
- **Getter and Setter**
- `toString()` 等方法

> 选中类内部空白处 → `Alt + Insert` → 选择需要生成的内容 → 勾选字段 → 确定。

---

### 2.5 就近原则与 this 关键字

#### 就近原则

当局部变量和成员变量重名时，Java 优先使用**最近的**变量（局部变量）。

```java
public class Student {
    private String name;  // 成员变量

    public void setName(String name) {  // 局部变量 name
        name = name;  // ❌ 都是局部变量，成员变量根本没被赋值！
    }
}
```

#### this 关键字

`this` 代表**当前对象**的引用，用来明确访问**成员变量/方法**而非局部变量。

```java
public void setName(String name) {
    this.name = name;  // ✅ this.name 是成员变量，右边 name 是局部变量
}
```

| 场景 | this 用法 |
|------|----------|
| 区分同名变量 | `this.属性 = 参数` |
| 调用本类构造方法 | `this()` / `this(参数)` — 必须在构造方法第一行 |
| 返回当前对象 | `return this`（链式调用） |

---

### 2.6 内存分配

Java 运行时内存分为三大区域：

| 区域 | 存放内容 | 特点 |
|------|---------|------|
| **栈内存 (Stack)** | 方法执行时的局部变量、方法参数 | 方法执行完毕自动释放，先进后出 |
| **堆内存 (Heap)** | `new` 创建的对象、数组、`static` 静态成员变量 | 由 GC 垃圾回收器管理，生命周期长 |
| **方法区 (Method Area)** | 类的 `.class` 字节码文件、静态变量、常量池 | 类加载时存入，全局共享 |

```
┌─────────────────────┐
│      方法区           │  Student.class 字节码
│  (类信息、静态变量)    │
├─────────────────────┤
│      堆内存           │  new Student() → 对象实例
│  (对象、数组)         │  static 变量
├─────────────────────┤
│      栈内存           │  main() → 局部变量 s（存的是对象的堆地址）
│  (方法、局部变量)     │  setName() → 局部变量 name
└─────────────────────┘
```

#### 关键理解

```java
Student s = new Student();
```

- `Student s`：在**栈**中声明引用变量 `s`
- `new Student()`：在**堆**中创建对象
- `=`：将堆中对象的**地址**赋给栈中的 `s`
- `s` 存的是地址，不是对象本身

---

### 2.7 对象在方法中的传递

> **Java 中参数传递只有一种方式：值传递。**
> 但对象类型的"值"是**堆内存地址**，所以方法内通过地址可以修改对象的属性——这被称为"地址传递"（本质是值传递中的引用值传递）。

```java
public static void change(Student s) {
    s.setName("李四");  // 通过地址修改了堆中对象的 name
}

public static void main(String[] args) {
    Student s = new Student("张三", 20);
    change(s);
    System.out.println(s.getName());  // 输出：李四 — 对象被修改了
}
```

```
调用 change(s) 时：
  s 的地址值 → 复制一份传给形参 → 形参指向同一个堆对象 → 修改属性生效
```

> ⚠️ 但如果在方法内 `s = new Student(...)` 重新赋值形参，则只改变形参指向，不影响 `main` 中的 `s`。

---

### 2.8 三个类

在实际项目中，通常按职责将类划分为三类：

| 类类型 | 职责 | 文件名示例 |
|--------|------|-----------|
| **JavaBean** | 描述实体对象，包含私有属性 + 构造方法 + getter/setter。不包含业务逻辑 | `Student.java`、`Goods.java` |
| **工具类** | 提供通用静态方法（如数组操作、字符串处理、数学计算）。通常构造方法私有化，方法全是 `static` | `ArrayUtils.java`、`MathUtils.java` |
| **测试类** | 包含 `main` 方法，用于调用和验证其他类的功能 | `Test.java`、`Demo.java` |

#### 示例

**JavaBean：Student.java**

```java
public class Student {
    private String name;
    private int age;

    public Student() { }
    public Student(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
}
```

**工具类：ArrayUtils.java**

```java
public class ArrayUtils {
    // 构造方法私有化，防止外部创建对象
    private ArrayUtils() { }

    public static int max(int[] arr) {
        int max = arr[0];
        for (int v : arr) if (v > max) max = v;
        return max;
    }

    public static void print(int[] arr) {
        for (int v : arr) System.out.print(v + " ");
        System.out.println();
    }
}
```

**测试类：Test.java**

```java
public class Test {
    public static void main(String[] args) {
        Student s = new Student("张三", 20);
        System.out.println(s.getName());

        int[] arr = {1, 5, 3, 9, 2};
        System.out.println(ArrayUtils.max(arr));  // 9
    }
}
```

---

## 三、Day01 总览

| 模块 | 核心内容 |
|------|---------|
| testDemo2 | 双指针归并、数组越界保护、中位数计算、方法封装 |
| testDemo3 | 动态规划思维、左右遍历、取交集减墙、双指针/单调栈扩展 |
| testDemo4 | 需求拆分、Scanner + Random、去重与输入校验、多分支奖级判定 |
| 面向对象 | 类与对象、private 封装、setter/getter、构造方法、`this` 关键字 |
| 内存模型 | 栈（方法/局部变量）、堆（new/static）、方法区（class字节码） |
| 对象传递 | 本质是地址传递（引用值传递），方法内可修改对象属性 |
| 三层架构 | JavaBean（数据）、工具类（功能）、测试类（入口） |

---

## 四、学习资源

| 来源 | 内容 | 课时 |
|------|------|------|
| 黑马 AI + Java | Java 基础 + 面向对象 | P79 ~ P100 |
