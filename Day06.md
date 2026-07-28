# Day06：HTML 表单与 JavaScript 入门


---

## 第一部分：HTML 表单（`<form>`）

### 1. `<form>` 标签

表单用于收集用户输入并提交到服务器。`<form>` 是表单的容器标签。

#### 核心属性

| 属性 | 说明 | 示例 |
|------|------|------|
| `action` | 表单提交的目标 URL（服务器处理地址） | `action="login.php"` |
| `method` | 表单提交的 HTTP 方法 | `method="get"` 或 `method="post"` |

#### `method` 详解：GET vs POST

| 对比维度 | GET | POST |
|---------|-----|------|
| 数据传输方式 | 参数附加在 URL 后面（查询字符串） | 参数放在 HTTP 请求体中 |
| URL 示例 | `login.php?username=admin&password=123` | `login.php`（URL 中看不到参数） |
| 安全性 | **低**。参数明文暴露在 URL 中，浏览器历史/服务器日志中可见 | **高**。参数在请求体中，不会出现在 URL |
| 数据大小限制 | **有**。浏览器对 URL 长度有限制（通常约 2KB~8KB） | **无限制**。适合传输大量数据 |
| 适用场景 | 搜索查询、分页参数等非敏感数据 | 登录、注册、上传文件等含敏感信息的操作 |
| 缓存 | URL 可被浏览器缓存 | 不会被缓存 |
| 书签 | 可收藏带参数的 URL | 无法收藏 |

```html
<!-- GET 方式：参数出现在 URL 中 -->
<form action="search.php" method="get">
    <input type="text" name="keyword" placeholder="输入搜索关键词">
    <input type="submit" value="搜索">
</form>
<!-- 提交后 URL 变为: search.php?keyword=xxx -->

<!-- POST 方式：参数不在 URL 中 -->
<form action="login.php" method="post">
    <input type="text" name="username" placeholder="用户名">
    <input type="password" name="password" placeholder="密码">
    <input type="submit" value="登录">
</form>
```

---

### 2. `<input>` 标签 —— 表单输入框

`<input>` 是表单中最常用的元素，通过 `type` 属性切换不同类型。

#### 常用属性

| 属性 | 说明 | 示例 |
|------|------|------|
| `type` | 指定输入框类型（见下方详表） | `type="text"` |
| `name` | 表单项名称，提交时作为参数名 | `name="username"` |
| `placeholder` | 输入框内的灰色提示文字（不影响值） | `placeholder="请输入用户名"` |
| `value` | 输入框的默认值 / 预填内容 | `value="默认文字"` |

> **重点**：`name` 属性是表单数据采集的关键。没有 `name` 的表单项，提交时不会被发送到服务器。

#### `type` 属性 — 全部类型详解

##### 文本与密码类

| type 值 | 说明 | 示例 |
|---------|------|------|
| `text` | 默认，单行文本输入框 | `<input type="text" name="username">` |
| `password` | 密码输入框，输入内容显示为圆点 | `<input type="password" name="pwd">` |

##### 选择类

| type 值 | 说明 | 关键点 |
|---------|------|--------|
| `radio` | 单选框 | 同一组需设置相同的 `name`，各自不同的 `value` |
| `checkbox` | 复选框 | 同一组多个可同时选中，`name` 相同表示同组 |

```html
<!-- 单选框：name 相同即为同一组，只能选一个 -->
<input type="radio" name="gender" value="male" checked> 男
<input type="radio" name="gender" value="female"> 女

<!-- 复选框：可多选 -->
<input type="checkbox" name="hobby" value="reading" checked> 阅读
<input type="checkbox" name="hobby" value="sports"> 运动
<input type="checkbox" name="hobby" value="music"> 音乐
```

##### 按钮类

| type 值 | 说明 | 行为 |
|---------|------|------|
| `submit` | 提交按钮 | 点击后将表单数据发送到 `action` 指定地址 |
| `reset` | 重置按钮 | 点击后将表单所有字段恢复为初始值 |
| `button` | 普通按钮 | 无默认行为，需配合 JavaScript 使用 |

```html
<form action="submit.php" method="post">
    <input type="text" name="username">
    <input type="submit" value="提交表单">
    <input type="reset" value="清空重填">
    <input type="button" value="普通按钮" onclick="alert('点击了按钮')">
</form>
```

##### 特殊功能类

| type 值 | 说明 |
|---------|------|
| `file` | 文件上传，点击弹出文件选择窗口 |
| `hidden` | 隐藏域，页面不可见但会随表单提交（常用于传递额外参数） |
| `image` | 图片形式的提交按钮，需配合 `src` 属性 |

```html
<input type="file" name="avatar">
<input type="hidden" name="userId" value="10086">
<input type="image" src="submit-btn.png" alt="提交">
```

##### HTML5 新增类型

| type 值 | 说明 | 视觉效果 / 行为 |
|---------|------|----------------|
| `color` | 颜色选择器 | 弹出系统取色面板 |
| `date` | 日期选择器 | 弹出日历控件，格式 `YYYY-MM-DD` |
| `datetime` | 日期+时间选择器（已废弃，不建议使用） | — |
| `datetime-local` | 本地日期+时间选择器 | 弹出日期和时间控件，不含时区 |

```html
<input type="color" name="bgColor" value="#ff0000">
<input type="date" name="birthday">
<input type="datetime-local" name="appointment">
```

---

### 3. `<select>` 下拉选择框

提供一组预定义的选项供用户选择。

| 属性 | 说明 |
|------|------|
| `name` | 选择框名称，提交时的参数名 |

子标签 `<option>` 的 `value` 属性表示该选项提交时的实际值，标签之间的文字是用户看到的显示文本。

```html
<select name="city">
    <option value="beijing">北京</option>
    <option value="shanghai">上海</option>
    <option value="guangzhou">广州</option>
    <option value="shenzhen">深圳</option>
</select>
<!-- 如果用户选择"上海"，提交的数据为: city=shanghai -->
```

---

### 4. `<textarea>` 文本域

多行文本输入区域，适用于留言、简介等较长文本。

| 属性 | 说明 |
|------|------|
| `name` | 文本域名称，提交时的参数名 |
| `rows` | 可见行数（控制高度） |
| `cols` | 可见列数（控制宽度，以字符为单位） |

```html
<textarea name="content" rows="5" cols="40" placeholder="请输入内容...">默认文本</textarea>
```

> 注意：`rows` 和 `cols` 只是视觉尺寸，用户可输入的内容不受此限制。

---

### 5. `<label>` 标签

将文本说明和表单控件关联，点击文本即可自动聚焦到对应输入框。

- `for` 属性的值与目标输入框的 `id` 属性值必须一致。

```html
<!-- 方式一：for + id 绑定 -->
<label for="username">用户名：</label>
<input type="text" name="username" id="username">

<!-- 方式二：包裹式（隐式关联） -->
<label>
    用户名：<input type="text" name="username">
</label>
```

> 点击"用户名"三个字，光标会自动跳入输入框。对单选框/复选框尤其实用，增大了可点击区域。

---

### 6. `<table>` 表格

#### 基本结构

| 标签 | 全称 | 说明 |
|------|------|------|
| `<table>` | table | 表格容器 |
| `<thead>` | table head | 表头区域（语义化） |
| `<tbody>` | table body | 表体区域（数据行） |
| `<tfoot>` | table foot | 表尾区域（合计行等） |
| `<tr>` | table row | 一行 |
| `<th>` | table header cell | 表头单元格（加粗居中） |
| `<td>` | table data cell | 普通数据单元格 |

```html
<table border="1">
    <thead>
        <tr>
            <th>姓名</th>
            <th>年龄</th>
            <th>城市</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>张三</td>
            <td>25</td>
            <td>北京</td>
        </tr>
        <tr>
            <td>李四</td>
            <td>30</td>
            <td>上海</td>
        </tr>
    </tbody>
    <tfoot>
        <tr>
            <td colspan="2">合计人数</td>
            <td>2</td>
        </tr>
    </tfoot>
</table>
```

#### 合并单元格

| 属性 | 说明 | 取值 |
|------|------|------|
| `rowspan` | 跨行合并（纵向合并） | 合并的行数 |
| `colspan` | 跨列合并（横向合并） | 合并的列数 |

```html
<!-- rowspan 示例：跨 2 行合并 -->
<table border="1">
    <tr>
        <td rowspan="2">合并列</td>
        <td>行1-列2</td>
        <td>行1-列3</td>
    </tr>
    <tr>
        <td>行2-列2</td>
        <td>行2-列3</td>
    </tr>
</table>

<!-- colspan 示例：跨 3 列合并 -->
<table border="1">
    <tr>
        <td colspan="3">跨三列的表头</td>
    </tr>
    <tr>
        <td>A</td>
        <td>B</td>
        <td>C</td>
    </tr>
</table>
```

> **要点**：合并单元格后，被"吞并"的单元格需要从 HTML 中删除对应的 `<td>` 或 `<th>`，否则表格会错位。

---

## 第二部分：JavaScript 入门

### 1. 内部脚本

JavaScript 代码通过 `<script>` 标签嵌入 HTML 页面。

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>JS 入门</title>
</head>
<body>
    <h1>Hello</h1>
    <p id="demo">这是一个段落。</p>

    <!-- 内部脚本：放在 body 最底部 -->
    <script>
        // 此时页面 DOM 已全部加载完毕，可以安全操作元素
        document.getElementById("demo").innerHTML = "内容已被 JS 修改！";
    </script>
</body>
</html>
```

**为什么 `<script>` 要放在 `<body>` 最底部？**

浏览器解析 HTML 是从上到下的。如果 `<script>` 放在 `<head>` 或 `<body>` 前面，脚本执行时底下的 DOM 元素可能还未加载，导致操作失败。放在底部能确保所有页面元素都已就绪，脚本可以安全操作任意 DOM 节点。

> 另一种方案是使用 `defer` 属性：`<script src="app.js" defer></script>`，脚本会在 DOM 解析完成后执行，此时可放在 `<head>` 中。

### 2. 外部脚本

将 JavaScript 代码抽离到独立的 `.js` 文件中，通过 `src` 属性引入。

```html
<!-- 在 HTML 中引入外部 JS 文件（同样建议放在 body 底部） -->
<script src="js/main.js"></script>
```

**外部脚本的优势**：
- HTML 与 JS 分离，维护更清晰
- 同一份 JS 可被多个页面复用
- 浏览器可缓存外部脚本，提升加载速度

> **注意**：如果 `<script>` 标签同时设置了 `src` 属性，标签内部写的脚本代码会被忽略，两者不能混用。

---

## 小结

| 模块 | 核心知识点 |
|------|-----------|
| `form` | `action` 指定提交地址，`method` 控制请求方式（get 明文不安全有长度限制，post 更安全无限制） |
| `input` | `type` 决定输入框类型，`name` 是数据采集的必需属性 |
| `select / textarea` | 下拉选择和多行输入，同样依赖 `name` 提交数据 |
| `label` | `for` + `id` 绑定，提升点击体验 |
| `table` | `tr` 行、`td` 单元格、`th` 表头、`rowspan`/`colspan` 合并单元格 |
| JavaScript | `<script>` 放在 `body` 底部确保 DOM 就绪，外部脚本用 `src` 引入 |
