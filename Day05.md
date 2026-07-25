
# Day05：Java 常用工具类 & Web 前端基础
> 学习文档：[https://heuqqdmbyk.feishu.cn/wiki/space/7413668442156498972](https://heuqqdmbyk.feishu.cn/wiki/space/7413668442156498972)

---

## 第一部分：Java 常用工具类

### 一、Math 类

`java.lang.Math` 提供数学运算的静态方法，无需创建对象，直接通过类名调用。

| 方法 | 说明 | 示例 |
|------|------|------|
| `Math.abs(x)` | 返回绝对值 | `Math.abs(-10)` → `10` |
| `Math.ceil(x)` | 向上取整（往正无穷方向） | `Math.ceil(4.1)` → `5.0`；`Math.ceil(-4.1)` → `-4.0` |
| `Math.floor(x)` | 向下取整（往负无穷方向） | `Math.floor(4.9)` → `4.0`；`Math.floor(-4.1)` → `-5.0` |
| `Math.round(x)` | 四舍五入，返回离参数最近的整数；多个等距值时向正无穷方向取 | `Math.round(4.5)` → `5`；`Math.round(-4.5)` → `-4` |
| `Math.max(a, b)` | 返回较大值 | `Math.max(10, 20)` → `20` |
| `Math.min(a, b)` | 返回较小值 | `Math.min(10, 20)` → `10` |
| `Math.pow(a, b)` | a 的 b 次方 | `Math.pow(2, 3)` → `8.0` |
| `Math.sqrt(x)` | 平方根 | `Math.sqrt(16)` → `4.0` |
| `Math.cbrt(x)` | 立方根 | `Math.cbrt(8)` → `2.0` |
| `Math.random()` | 返回 `[0.0, 1.0)` 之间的 `double` 随机小数 | 生成 1~100 随机整数：`(int)(Math.random() * 100) + 1` |

---

### 二、System 类

`java.lang.System` 提供与系统相关的工具方法，均为静态方法。

| 方法 | 说明 |
|------|------|
| `System.exit(int status)` | 终止当前正在运行的 Java 虚拟机。`0` 表示正常退出，非 `0` 表示异常退出 |
| `System.currentTimeMillis()` | 返回当前系统时间的毫秒值（自 1970-01-01 00:00:00 UTC 至今），常用于计算程序耗时 |
| `System.arraycopy(src, srcPos, dest, destPos, length)` | 数组拷贝，将源数组指定位置开始的元素复制到目标数组 |

**`arraycopy` 五个参数详解：**

```java
System.arraycopy(源数组,   // Object src   — 源数组
                 起始索引,  // int srcPos   — 源数组开始复制的位置
                 目标数组,  // Object dest  — 目标数组
                 目标起始,  // int destPos  — 目标数组开始粘贴的位置
                 复制长度); // int length   — 要复制的元素个数
```

```java
int[] src = {1, 2, 3, 4, 5};
int[] dest = new int[5];
System.arraycopy(src, 1, dest, 2, 3);
// dest → {0, 0, 2, 3, 4}
```

---

### 三、Object 类

`java.lang.Object` 是所有类的**终极父类**。Java 中任何一个类都直接或间接继承自 `Object`。

#### 3.1 toString() 方法

- **默认行为**：返回 `类名@十六进制哈希值`（即地址值），例如 `com.tianxing.test.Student@1b6d3586`
- **与 println 的关系**：`System.out.println(obj)` 内部会默认调用 `obj.toString()` 方法
- **最佳实践**：在自定义类中**重写 toString()**，返回对象的关键属性值，便于调试和日志输出

```java
// 重写 toString()
@Override
public String toString() {
    return "Student{name='" + name + "', age=" + age + "}";
}
```

#### 3.2 equals(Object obj) 方法

- **默认行为**：底层使用 `==` 比较，即比较两个对象的**内存地址**是否相同
- **重写时机**：当需要按**属性值**判断两个对象是否相等时，必须重写 `equals()` 方法
- **重写惯例**：重写 `equals()` 通常也要重写 `hashCode()`，保证相等的对象有相同的哈希码

```java
@Override
public boolean equals(Object obj) {
    if (this == obj) return true;           // 地址相同，直接返回 true
    if (obj == null || getClass() != obj.getClass()) return false;
    Student other = (Student) obj;
    return age == other.age && Objects.equals(name, other.name);
}
```

---

### 四、包装类

Java 为 8 种基本数据类型提供了对应的引用类型（包装类），位于 `java.lang` 包。

| 基本类型 | 包装类 |
|----------|--------|
| `byte` | `Byte` |
| `short` | `Short` |
| `int` | `Integer` |
| `long` | `Long` |
| `float` | `Float` |
| `double` | `Double` |
| `char` | `Character` |
| `boolean` | `Boolean` |

#### 4.1 自动装箱与自动拆箱（JDK 1.5+）

- **自动装箱**：基本类型自动转为对应包装类对象。如 `Integer i = 10;` 底层等价于 `Integer.valueOf(10)`
- **自动拆箱**：包装类对象自动转为对应基本类型。如 `int n = i;` 底层等价于 `i.intValue()`

```java
Integer i = 100;          // 自动装箱
int n = i;                // 自动拆箱
Integer result = i + 50;  // 先拆箱计算，再装箱赋值
```

#### 4.2 Integer 常量池

- **范围**：`-128 ~ 127`
- **触发条件**：通过 `Integer.valueOf()` 或**直接赋值**（自动装箱）时会先查常量池
- **关键区别**：
  - 直接赋值 `Integer a = 127` → 走常量池，同一数值复用同一对象
  - `new Integer(127)` → **不经过常量池**，每次创建新对象

```java
Integer a = 127;                // 常量池已有，复用
Integer b = Integer.valueOf(127); // 同样走常量池，复用
Integer c = new Integer(127);   // 堆中新对象，不走常量池

System.out.println(a == b);  // true  （同一常量池对象）
System.out.println(a == c);  // false （不同对象）

Integer x = 128;   // 超出 -128~127，new 新对象
Integer y = 128;   // 同样 new 新对象
System.out.println(x == y); // false
```

#### 4.3 Integer 常用方法

| 方法 | 说明 |
|------|------|
| `Integer.valueOf(String/int)` | 返回 Integer 对象，会先查常量池 |
| `Integer.parseInt(String)` | 将数字字符串转为 `int` 基本类型 |
| `Integer.toBinaryString(int)` | 转为二进制字符串 |
| `Integer.toOctalString(int)` | 转为八进制字符串 |
| `Integer.toHexString(int)` | 转为十六进制字符串 |

```java
int n = Integer.parseInt("123");       // 123
String bin = Integer.toBinaryString(10);  // "1010"
String hex = Integer.toHexString(255);    // "ff"
```

---

### 五、BigInteger 类

`java.math.BigInteger` 用于表示**不可变的任意精度整数**，突破 `long` 的上限（约 9 × 10¹⁸）。

```java
BigInteger a = new BigInteger("99999999999999999999");
BigInteger b = new BigInteger("88888888888888888888");
BigInteger sum = a.add(b);          // 加法
BigInteger diff = a.subtract(b);    // 减法
BigInteger product = a.multiply(b); // 乘法
BigInteger quotient = a.divide(b);  // 除法
```

---

### 六、BigDecimal 类

`java.math.BigDecimal` 解决浮点数运算的**精度丢失**问题。

> `0.1 + 0.2 = 0.30000000000000004` 在 `double` 中会出现，`BigDecimal` 可精确表示。

#### 6.1 核心原理

- **底层存储**：将小数转为字符数组存储，因此精度不会丢失
- 计算机用二进制无法精确表示某些十进制小数（如 `0.1`），`BigDecimal` 通过保存每个十进制位来解决

#### 6.2 创建对象

| 方式 | 说明 | 推荐 |
|------|------|------|
| `new BigDecimal(String)` | 用字符串构造，精确 | ✅ 推荐 |
| `new BigDecimal(double)` | 用 double 构造，double 本身可能已丢失精度 | ❌ 不推荐 |
| `BigDecimal.valueOf(double)` | 静态方法，内部调 `Double.toString()` 避免精度丢失 | ✅ 可用 |

```java
// 对比
BigDecimal bd1 = new BigDecimal(0.01);                // 0.010000000000000000208...（不精确）
BigDecimal bd2 = new BigDecimal("0.01");               // 0.01（精确）
BigDecimal bd3 = BigDecimal.valueOf(0.01);             // 0.01（静态方法内部处理了精度）
```

#### 6.3 常量池

`BigDecimal` 对 `0 ~ 10` 的整数值提供了常量池支持：

```java
BigDecimal zero = BigDecimal.ZERO;   // 常量 0
BigDecimal one  = BigDecimal.ONE;    // 常量 1
BigDecimal ten  = BigDecimal.TEN;    // 常量 10
```

#### 6.4 算术方法

| 方法 | 说明 |
|------|------|
| `add(BigDecimal)` | 加法 |
| `subtract(BigDecimal)` | 减法 |
| `multiply(BigDecimal)` | 乘法 |
| `divide(BigDecimal, 舍入模式)` | 除法（除不尽时**必须**指定舍入模式，否则抛异常） |
| `divideAndRemainder(BigDecimal)` | 返回 `BigDecimal[]`，`[0]` 为商，`[1]` 为余数 |

```java
BigDecimal a = new BigDecimal("10");
BigDecimal b = new BigDecimal("3");

BigDecimal sum = a.add(b);              // 13
BigDecimal diff = a.subtract(b);        // 7
BigDecimal prod = a.multiply(b);        // 30
BigDecimal quot = a.divide(b, 2, RoundingMode.HALF_UP); // 3.33（保留2位，四舍五入）

BigDecimal[] result = a.divideAndRemainder(b);
// result[0] = 3（商），result[1] = 1（余数）
```

**常用舍入模式（RoundingMode）：**

| 模式 | 说明 |
|------|------|
| `HALF_UP` | 四舍五入（最常用） |
| `HALF_DOWN` | 五舍六入 |
| `CEILING` | 向正无穷方向舍入 |
| `FLOOR` | 向负无穷方向舍入 |
| `UP` | 远离零方向舍入 |

---

## 第二部分：Web 前端开发

> 飞书知识库：[https://heuqqdmbyk.feishu.cn/wiki/space/7413668442156498972](https://heuqqdmbyk.feishu.cn/wiki/space/7413668442156498972)

前端三要素：

| 技术 | 全称 | 作用 |
|------|------|------|
| HTML | HyperText Markup Language | 网页**结构**（骨架） |
| CSS | Cascading Style Sheets | 网页**表现**（皮肤） |
| JavaScript | — | 网页**交互**（行为） |

---

### 一、HTML（结构）

#### 1.1 标题标签

`<h1>` ~ `<h6>`，数字越小标题越大，`<h1>` 最大（通常一个页面只用一个），`<h6>` 最小。

#### 1.2 超链接

```html
<a href="https://www.example.com" target="_blank">链接文本</a>
```

| 属性 | 说明 |
|------|------|
| `href` | 目标 URL |
| `target` | `_self`（当前窗口打开，默认）/ `_blank`（新标签页打开） |

#### 1.3 颜色表示

| 方式 | 格式 | 示例 |
|------|------|------|
| `rgb` | `rgb(红, 绿, 蓝)`，每通道 0~255 | `rgb(255, 0, 0)` → 红色 |
| `rgba` | `rgba(红, 绿, 蓝, 透明度)`，透明度 0~1 | `rgba(0, 0, 255, 0.5)` → 半透明蓝 |
| 十六进制 | `#RRGGBB`，每通道 00~FF | `#00FF00` → 绿色；`#000000` → 黑色 |

#### 1.4 视频标签

```html
<video src="movie.mp4" autoplay controls loop muted poster="cover.jpg" width="600" height="400"></video>
```

| 属性 | 说明 |
|------|------|
| `src` | 视频文件路径 |
| `autoplay` | 自动播放（通常需搭配 `muted` 才生效） |
| `controls` | 显示播放控制条 |
| `loop` | 循环播放 |
| `muted` | 静音播放 |
| `poster` | 封面图片（视频加载前显示） |
| `width` | 宽度（单位：`px` 像素 / `%` 百分比） |
| `height` | 高度 |

#### 1.5 排版标签

| 标签 | 作用 |
|------|------|
| `<p>` | 段落（paragraph） |
| `<br>` | 换行（单标签） |
| `<hr>` | 水平分割线（单标签） |

#### 1.6 文本修饰标签

| 标签 | 效果 | 语义 |
|------|------|------|
| `<b>` / `<strong>` | **加粗** | strong 有强调语义，SEO 更友好 |
| `<u>` / `<ins>` | <u>下划线</u> | ins 表示插入内容 |
| `<i>` / `<em>` | *斜体* | em 有强调语义 |
| `<s>` / `<del>` | ~~删除线~~ | del 表示删除内容 |

#### 1.7 字符实体

HTML 中 `<`、`>` 等特殊字符需要转义：

| 实体 | 显示 |
|------|------|
| `&lt;` | `<`（小于号） |
| `&gt;` | `>`（大于号） |
| `&nbsp;` | 空格（non-breaking space，不会换行的空格） |

#### 1.8 图片标签

```html
<img src="image.jpg" alt="图片描述" width="300" height="200">
```

| 属性 | 说明 |
|------|------|
| `src` | 图片路径 |
| `alt` | 图片描述文本（加载失败时显示，也用于无障碍） |
| `width` | 图片宽度 |
| `height` | 图片高度 |

---

### 二、CSS（表现）

#### 2.1 三种选择器

| 选择器类型 | 语法 | 作用范围 | 优先级 |
|-----------|------|---------|--------|
| 元素选择器 | `标签名 { 样式 }` | 所有该标签 | 低 |
| 类选择器 | `.class名 { 样式 }` | 所有带该 class 的元素 | 中 |
| ID 选择器 | `#id名 { 样式 }` | 唯一 id 元素 | 高 |

**优先级排序（权重）：** ID 选择器 > 类选择器 > 元素选择器

```css
/* 元素选择器 — 所有 p 标签变红 */
p {
    color: red;
}

/* 类选择器 — 带 class="highlight" 的元素 */
.highlight {
    color: rgb(0, 128, 255);
}

/* ID 选择器 — id="title" 的元素 */
#title {
    color: #333333;
}
```

---

### 三、盒子模型

每个 HTML 元素都可以看作一个矩形盒子，从内到外共分四层：

```
┌─────────────────────────────┐
│          margin             │  ← 外边距（与其他元素的间距）
│  ┌───────────────────────┐  │
│  │        border         │  │  ← 边框
│  │  ┌─────────────────┐  │  │
│  │  │     padding     │  │  │  ← 内边距（内容与边框之间）
│  │  │  ┌───────────┐  │  │  │
│  │  │  │  content  │  │  │  │  ← 内容区（显示文字/图片）
│  │  │  └───────────┘  │  │  │
│  │  └─────────────────┘  │  │
│  └───────────────────────┘  │
└─────────────────────────────┘
```

| 层级 | 名称 | 说明 |
|------|------|------|
| `content` | 内容区 | 实际的文字/图片内容 |
| `padding` | 内边距 | 内容到边框的距离 |
| `border` | 边框 | 围绕内边距的线 |
| `margin` | 外边距 | 当前盒子与其他盒子的间距 |

---

### 四、Flex 弹性布局

Flex 是一维空间布局模型，用于在**一条轴（主轴）**上排列子元素，非常适合导航栏、卡片列表等场景。

#### 4.1 启用 Flex

```css
.container {
    display: flex;
}
```

设置后，子元素默认沿**主轴**从左到右排列。

#### 4.2 flex-direction（主轴方向）

| 属性值 | 主轴方向 | 效果 |
|--------|---------|------|
| `row`（默认） | 水平，左→右 | 子元素从左到右排列 |
| `row-reverse` | 水平，右→左 | 子元素从右到左排列 |
| `column` | 垂直，上→下 | 子元素从上到下排列 |
| `column-reverse` | 垂直，下→上 | 子元素从下到上排列 |

#### 4.3 justify-content（主轴对齐方式）

控制子元素在**主轴**上的分布方式：

| 属性值 | 效果 |
|--------|------|
| `flex-start`（默认） | 从主轴起点排列 |
| `flex-end` | 从主轴终点排列 |
| `center` | 居中对齐 |
| `space-between` | 两端对齐，中间均匀分布 |
| `space-around` | 每个子元素两侧间距相等（两端间距为中间一半） |
| `space-evenly` | 所有间距（含两端）完全相等 |

```css
.nav {
    display: flex;
    flex-direction: row;
    justify-content: space-between;
}
```

