# Day07 - JavaScript 基础

---

## 一、JavaScript 基础语法

### 1.1 变量声明

JavaScript 是**弱类型语言**，变量无需指明类型，且可以随时改变类型。

#### let — 可变变量

```javascript
let a = 1;           // a 是 number
a = "hello world";   // a 变成 string，完全合法
```

#### const — 常量

```javascript
const b = 1;
b = 2;  // ❌ 报错！const 声明的常量不可重新赋值
```

| 关键字 | 可变 | 块级作用域 | 说明 |
|--------|------|-----------|------|
| `let` | 是 | 是 | 变量，可改值、可改类型 |
| `const` | 否 | 是 | 常量，声明时必须初始化，不可重新赋值 |
| `var`（旧） | 是 | 否 | 函数作用域，有变量提升，不推荐使用 |

> **注意**：`const` 声明对象时，对象的**属性**仍然可以修改，只是不能把变量指向另一个对象。

```javascript
const obj = { name: "张三" };
obj.name = "李四";   // ✅ 允许，修改属性
obj = {};            // ❌ 报错，不能重新赋值
```

---

### 1.2 输出方式

| 方式 | 用途 | 示例 |
|------|------|------|
| `alert()` | 浏览器弹出警告框 | `alert("你好");` |
| `console.log()` | 控制台输出（调试最常用） | `console.log(b);` |
| `document.write()` | 直接写入 HTML 页面 | `document.write("hello");` |

```javascript
let name = "tianxing";
alert(name);                    // 弹窗显示
console.log("调试:", name);     // F12 控制台查看
document.write("<h1>" + name + "</h1>");  // 页面渲染
```

---

### 1.3 数据类型

JavaScript 有 **5 种基本数据类型**：

| 类型 | 说明 | 示例 |
|------|------|------|
| `number` | 数字（整数和浮点数统一） | `1`, `3.14`, `-5`, `Infinity`, `NaN` |
| `string` | 字符串 | `"hello"`, `'world'`, `` `模板` `` |
| `boolean` | 布尔值 | `true`, `false` |
| `null` | 空值（刻意设置为空） | `let x = null;` |
| `undefined` | 未定义（变量声明未赋值） | `let y;` → y 为 undefined |

#### typeof 检测类型

```javascript
let a = 42;
let b = "hello";
let c = true;
let d = null;
let e;

console.log(typeof a);  // "number"
console.log(typeof b);  // "string"
console.log(typeof c);  // "boolean"
console.log(typeof d);  // "object"  ← 历史遗留 bug！
console.log(typeof e);  // "undefined"
```

> **注意**：`typeof null` 返回 `"object"`，这是 JavaScript 早期设计缺陷，已被 ECMAScript 规范保留至今。

---

### 1.4 模板字符串

用反引号 `` ` `` 包裹，`${}` 内嵌变量或表达式：

```javascript
let name = "张三";
let age = 18;
let msg = `我叫${name}，今年${age}岁`;  // "我叫张三，今年18岁"

// 还可嵌入表达式
let sum = `1 + 2 = ${1 + 2}`;          // "1 + 2 = 3"

// 支持多行
let poem = `床前明月光，
疑是地上霜。`;
```

> 传统引号写法 `"我叫" + name + "，今年" + age + "岁"` 繁琐且不支持换行，模板字符串优雅得多。

---

### 1.5 函数（三种写法）

JavaScript 函数**不需要指定返回值类型和形参类型**。

#### 写法一：普通函数（函数声明）

```javascript
function add(a, b) {
    return a + b;
}
console.log(add(1, 2));  // 3
```

特点：**函数提升**——可以在声明之前调用。

#### 写法二：匿名函数（函数表达式）

```javascript
let add = function(a, b) {
    return a + b;
};
console.log(add(1, 2));  // 3
```

特点：不能声明前调用（变量提升规则不同）。

#### 写法三：箭头函数（ES6）

```javascript
// 完整写法
let add = (a, b) => {
    return a + b;
};

// 简写（函数体只有一句 return 时可省略 {} 和 return）
let add = (a, b) => a + b;

// 只有一个参数时可省略 ()
let greet = name => `你好，${name}`;
```

| 写法 | 语法 | 函数提升 | this 绑定 |
|------|------|---------|-----------|
| 普通函数 | `function f(){}` | 有 | 动态（调用者决定） |
| 匿名函数 | `let f = function(){}` | 无 | 动态 |
| 箭头函数 | `let f = ()=>{}` | 无 | 静态（定义时捕获） |

---

### 1.6 对象

JavaScript 对象就是一组 **键值对（key-value）** 的集合，用 `{}` 字面量创建。

```javascript
let student = {
    name: "张三",        // 属性
    age: 18,
    gender: "男",
    sing: function() {   // 方法
        console.log(this.name + " 在唱歌");
    }
};

// 访问属性
console.log(student.name);    // "张三"
console.log(student["age"]);  // 18（方括号语法）

// 调用方法
student.sing();               // "张三 在唱歌"
```

> `this` 在对象方法中指向**调用该方法的对象本身**。

---

### 1.7 JSON

JSON（JavaScript Object Notation）是一种轻量级数据交换格式，规则：

- **key 必须用双引号**包裹
- 值可以是：字符串、数字、布尔、null、数组、对象
- 不支持函数、undefined、注释

```javascript
// 合法 JSON 字符串
let jsonStr = '{"name":"张三","age":18,"gender":"男"}';
```

#### JSON ↔ JS 对象互转

```javascript
// JS 对象 → JSON 字符串
let person = { name: "张三", age: 18, gender: "男" };
let jsonStr = JSON.stringify(person);
console.log(jsonStr);  // {"name":"张三","age":18,"gender":"男"}

// JSON 字符串 → JS 对象
let jsonString = '{"name":"李四","age":25}';
let obj = JSON.parse(jsonString);
console.log(obj.name); // "李四"
```

| 方法                    | 方向       | 用途             |
| --------------------- | -------- | -------------- |
| `JSON.stringify(obj)` | 对象 → 字符串 | 序列化，发送数据给后端    |
| `JSON.parse(str)`     | 字符串 → 对象 | 反序列化，解析后端返回的数据 |

---

## 二、DOM 操作

**DOM（Document Object Model）** 将 HTML 文档建模为一棵树，所有元素都是对象，可通过 JavaScript 操作。

### 2.1 获取元素

```javascript
// 获取单个元素（返回第一个匹配的）
let title = document.querySelector("h1");
let btn = document.querySelector("#submit-btn");   // ID 选择器
let box = document.querySelector(".box");          // class 选择器

// 获取多个元素（返回 NodeList，类似数组）
let items = document.querySelectorAll("li");
let allBoxes = document.querySelectorAll(".box");
```

| 方法 | 返回值 | 适用场景 |
|------|--------|---------|
| `querySelector(css选择器)` | 单个元素 / null | 获取一个元素 |
| `querySelectorAll(css选择器)` | NodeList（可遍历） | 获取一组元素 |

### 2.2 操作元素

```javascript
let el = document.querySelector("#demo");

// 修改文本内容
el.textContent = "新内容";
el.innerText = "新内容";

// 修改 HTML 内容
el.innerHTML = "<strong>加粗文字</strong>";

// 修改样式
el.style.color = "red";
el.style.fontSize = "20px";

// 修改属性
el.setAttribute("class", "active");
el.classList.add("highlight");

// 获取输入框的值
let input = document.querySelector("#username");
console.log(input.value);
```

---

## 三、事件监听

### 3.1 基本语法

```javascript
// 事件源.addEventListener(事件类型, 事件处理函数);
let btn = document.querySelector("#myBtn");
btn.addEventListener("click", function() {
    alert("按钮被点击了！");
});

// 使用箭头函数
btn.addEventListener("click", () => {
    console.log("clicked");
});
```

### 3.2 事件分类

#### 鼠标事件

| 事件 | 触发时机 |
|------|---------|
| `click` | 鼠标单击 |
| `dblclick` | 鼠标双击 |
| `mouseover` | 鼠标悬停（移入元素） |
| `mouseout` | 鼠标移出元素 |
| `mousemove` | 鼠标在元素上移动 |

```javascript
let box = document.querySelector(".box");
box.addEventListener("mouseover", () => box.style.background = "yellow");
box.addEventListener("mouseout", () => box.style.background = "white");
```

#### 键盘事件

| 事件 | 触发时机 | 常用属性 |
|------|---------|---------|
| `keydown` | 按键按下（按住会持续触发） | `event.key`, `event.code` |
| `keyup` | 按键松开 | `event.key` |
| `keypress` | 字符键按下（已废弃，推荐 keydown） | — |

```javascript
document.addEventListener("keydown", (event) => {
    console.log("按下了:", event.key);
    if (event.key === "Enter") {
        console.log("回车键！");
    }
});
```

#### 表单事件

| 事件 | 触发时机 |
|------|---------|
| `input` | 输入框内容改变（实时触发） |
| `focus` | 元素获得焦点 |
| `blur` | 元素失去焦点 |
| `submit` | 表单提交 |
| `change` | 内容改变且失去焦点后 |

```javascript
let input = document.querySelector("#username");
input.addEventListener("focus", () => console.log("输入框获焦"));
input.addEventListener("blur", () => console.log("输入框失焦"));
input.addEventListener("input", (e) => console.log("当前值:", e.target.value));
```

> **`input` vs `change`**：`input` 每次输入都触发，`change` 只在内容改变且失焦后触发一次。

### 3.3 事件对象 event

```javascript
btn.addEventListener("click", function(event) {
    console.log(event.target);  // 触发事件的元素
    console.log(event.type);    // 事件类型："click"
    event.preventDefault();     // 阻止默认行为（如阻止链接跳转）
    event.stopPropagation();    // 阻止事件冒泡
});
```

---

## 四、JS 模块化（ES6 Module）

### 4.1 导出（export）

```javascript
// math.js
export function add(a, b) {
    return a + b;
}

export function multiply(a, b) {
    return a * b;
}

export const PI = 3.14159;

// 也可统一导出
export { add, multiply, PI };
```

### 4.2 导入（import）

```html
<!-- HTML 中引入模块 -->
<script src="./js/main.js" type="module"></script>
```

```javascript
// main.js
import { add, multiply, PI } from "./math.js";

console.log(add(1, 2));        // 3
console.log(multiply(3, 4));   // 12
console.log(PI);               // 3.14159
```

### 4.3 默认导出

```javascript
// logger.js — 默认导出（一个模块只能有一个）
export default function log(msg) {
    console.log("[LOG]", msg);
}

// main.js — 导入默认导出（可自定义名称）
import myLog from "./logger.js";
myLog("hello");  // [LOG] hello
```

| 导出方式 | 导出语法                    | 导入语法                         | 一个模块可用几次 |
| ---- | ----------------------- | ---------------------------- | -------- |
| 命名导出 | `export function f(){}` | `import { f } from "..."`    | 多次       |
| 默认导出 | `export default ...`    | `import f from "..."`（不用花括号） | 一次       |

> **注意**：`type="module"` 的脚本默认**延迟执行**（相当于 `defer`），且默认使用**严格模式**。

---

## 五、综合示例：计数器

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>计数器</title>
</head>
<body>
    <h1>计数器：<span id="count">0</span></h1>
    <button id="inc">+1</button>
    <button id="dec">-1</button>

    <script type="module">
        // 导入模块
        import { increment, decrement } from "./counter.js";

        const countEl = document.querySelector("#count");
        let count = 0;

        document.querySelector("#inc").addEventListener("click", () => {
            count = increment(count);
            countEl.textContent = count;
        });

        document.querySelector("#dec").addEventListener("click", () => {
            count = decrement(count);
            countEl.textContent = count;
        });
    </script>
</body>
</html>
```

```javascript
// counter.js
export function increment(n) {
    return n + 1;
}

export function decrement(n) {
    return n - 1;
}
```
> **注意**：如果无法跨域浏览，可使用Live Server访问
---
