
Vue.js 前端框架、Tlias 实战项目、Ajax/Axios、Vue 生命周期、Maven 构建工具、软件测试

---

## 一、Vue.js 框架

### 1.1 什么是 Vue

Vue 是一套**渐进式 JavaScript 框架**，用于构建用户界面。它的核心思想是**数据驱动渲染**——页面的显示由数据决定，当数据发生变化时，页面自动更新，开发者无需手动操作 DOM。

| 特性 | 说明 |
|------|------|
| 渐进式 | 可以逐步引入，从轻量页面增强到完整单页应用 |
| 数据驱动 | 数据和视图双向绑定，数据变了视图自动变 |
| 组件化 | 页面拆分为独立可复用的组件 |
| 虚拟 DOM | 高效的 diff 算法，最小化真实 DOM 操作 |

### 1.2 引入 Vue

**方式一：ES Module 导入（推荐）**

```javascript
import { createApp } from 'https://unpkg.com/vue@3/dist/vue.esm-browser.js'
createApp({ /* 选项 */ }).mount('#app')
```

**方式二：CDN 全局脚本**

```html
<script src="https://unpkg.com/vue@3/dist/vue.global.js"></script>
<script>
  Vue.createApp({ /* 选项 */ }).mount('#app')
</script>
```

### 1.3 核心指令

#### 1.3.1 {{ }} — 插值表达式

在模板中插入数据，自动将 Vue 实例中的数据显示到页面。

```html
<span>{{ message }}</span>
```

> **注意**：插值表达式不能出现在 HTML 标签内部（如 `<div {{ attr }}>`），标签属性绑定请使用 `v-bind`。

#### 1.3.2 v-on / @ — 事件绑定

绑定 DOM 事件（click、input、keyup 等），对应的方法定义在 `methods` 中。

```html
<button v-on:click="handleClick">点击</button>
<!-- 简写形式 -->
<button @click="handleClick">点击</button>
```

```javascript
methods: {
  handleClick() {
    console.log('按钮被点击了')
  }
}
```

#### 1.3.3 v-bind / : — 属性绑定

将 Vue 实例数据动态绑定到 HTML 标签属性上（src、href、class、style 等）。

```html
<img v-bind:src="item.img" :alt="item.name">
<!-- 简写形式 -->
<img :src="item.img" :alt="item.name">
```

```javascript
data() {
  return {
    item: { img: 'https://example.com/avatar.png', name: '张三' }
  }
}
```

#### 1.3.4 v-model — 双向数据绑定

用于表单元素（input、select、textarea），实现**数据 → 视图**和**视图 → 数据**的双向同步。

```html
<input type="text" v-model="searchForm.name" placeholder="请输入姓名">
```

```javascript
data() {
  return {
    searchForm: { name: '', gender: '', job: '' }
  }
}
```

- 用户在输入框输入 → `searchForm.name` 自动更新
- 代码修改 `searchForm.name` → 输入框自动显示新值

#### 1.3.5 v-for — 列表渲染

遍历数组或对象，生成重复的 DOM 结构。`:key` 是必须的，用于 Vue 的 diff 算法高效更新。

```html
<tr v-for="(item, index) in empList" :key="item.id">
  <td>{{ item.name }}</td>
  <td>{{ index }}</td>
</tr>
```

| 参数 | 说明 |
|------|------|
| `item` | 当前遍历到的元素 |
| `index` | 当前索引（从 0 开始） |
| `empList` | 要遍历的数组，必须定义在 `data()` 中 |
| `:key` | 唯一标识符，常用 `item.id`，不可用 `index` 作为稳定 key |

> **注意**：`v-for` 写在**你想让它循环重复的那个标签上**。

#### 1.3.6 v-if / v-else-if / v-else — 条件渲染

根据条件决定元素**是否存在**于 DOM 中。适用于**不频繁切换**的场景。

```html
<div v-if="status === 'A'">状态 A</div>
<div v-else-if="status === 'B'">状态 B</div>
<div v-else>未知状态</div>
```

#### 1.3.7 v-show — 显示/隐藏

根据条件决定元素是否**显示**。元素始终存在于 DOM 中，只是通过 CSS `display:none` 隐藏。适用于**频繁切换**的场景。

```html
<div v-show="isVisible">这段内容可能被隐藏</div>
```

#### 1.3.8 v-if vs v-show 对比

| 对比项 | v-if | v-show |
|--------|------|--------|
| 条件为 false 时 | 元素从 DOM 中移除（不渲染） | 元素仍在 DOM 中，`display:none` 隐藏 |
| 切换开销 | 高（每次销毁/重建 DOM） | 低（仅切换 CSS） |
| 初始渲染开销 | 低（条件为 false 时不渲染） | 高（始终渲染） |
| 适用场景 | 运行时条件很少改变 | 频繁切换显示/隐藏 |
| 配套指令 | v-else-if / v-else | 无 |

### 1.4 Vue 指令速查表

| 指令 | 简写 | 用途 |
|------|------|------|
| `v-on:click` | `@click` | 绑定点击事件 |
| `v-on:submit` | `@submit` | 绑定表单提交事件 |
| `v-bind:src` | `:src` | 绑定 src 属性 |
| `v-bind:class` | `:class` | 动态绑定 class |
| `v-model` | — | 表单双向绑定 |
| `v-for` | — | 列表渲染 |
| `v-if / v-else-if / v-else` | — | 条件渲染（控制 DOM 存在） |
| `v-show` | — | 条件显示（控制 CSS 显示） |
| `{{ }}` | — | 文本插值 |

---

## 二、实战项目：Tlias 智能学习辅助系统

> 以下是一个完整的 Vue 3 单页面应用，综合运用了 `createApp` + `data()` + `methods` + `mount()` 以及上方所有核心指令。

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Tlias智能学习辅助系统</title>
  <style>
    body { margin: 0; }
    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      background-color: #c2c0c0;
      padding: 20px 20px;
      box-shadow: 0 2px 5px rgba(0,0,0,0.1);
    }
    .header h1 { margin: 0; font-size: 24px; font-weight: bold; }
    .header a { text-decoration: none; color: #333; font-size: 16px; }
    .search-form {
      display: flex;
      align-items: center;
      padding: 20px;
      background-color: #f9f9f9;
    }
    .search-form input[type="text"], .search-form select {
      margin-right: 10px;
      padding: 10px 10px;
      border: 1px solid #ccc;
      border-radius: 4px;
      width: 26%;
    }
    .search-form button {
      padding: 10px 15px;
      margin-left: 10px;
      background-color: #007bff;
      color: white;
      border: none;
      border-radius: 4px;
      cursor: pointer;
    }
    .search-form button.clear { background-color: #6c757d; }
    .table { min-width: 100%; border-collapse: collapse; }
    .table td, .table th {
      border: 1px solid #ddd;
      padding: 8px;
      text-align: center;
    }
    .avatar {
      width: 30px;
      height: 30px;
      object-fit: cover;
      border-radius: 50%;
    }
    .footer {
      background-color: #c2c0c0;
      color: white;
      text-align: center;
      padding: 10px 0;
      margin-top: 30px;
    }
    .footer .company-name { font-size: 1.1em; font-weight: bold; }
    .footer .copyright { font-size: 0.9em; }
    #container { width: 80%; margin: 0 auto; }
  </style>
</head>
<body>
  <div id="container">
    <!-- 顶栏 -->
    <div class="header">
      <h1>Tlias智能学习辅助系统</h1>
      <a href="#">退出登录</a>
    </div>

    <!-- 搜索表单区域 -->
    <form class="search-form">
      <input type="text" name="name" placeholder="姓名" v-model="searchEmp.name" />
      <select name="gender" v-model="searchEmp.gender">
        <option value="">性别</option>
        <option value="1">男</option>
        <option value="2">女</option>
      </select>
      <select name="job" v-model="searchEmp.job">
        <option value="">职位</option>
        <option value="1">班主任</option>
        <option value="2">讲师</option>
        <option value="3">学工主管</option>
        <option value="4">教研主管</option>
        <option value="5">咨询师</option>
      </select>
      <button type="button" @click="search">查询</button>
      <button type="button" @click="clear">清空</button>
    </form>

    <!-- 员工列表表格 -->
    <table class="table table-striped table-bordered">
      <thead>
        <tr>
          <th>姓名</th>
          <th>性别</th>
          <th>头像</th>
          <th>职位</th>
          <th>入职日期</th>
          <th>最后操作时间</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(emp, index) in empList" :key="index">
          <td>{{ emp.name }}</td>
          <td>{{ emp.gender === 1 ? '男' : '女' }}</td>
          <td><img :src="emp.image" :alt="emp.name" class="avatar"></td>
          <td>
            <span v-if="emp.job === '1'">班主任</span>
            <span v-else-if="emp.job === '2'">讲师</span>
            <span v-else-if="emp.job === '3'">学工主管</span>
            <span v-else-if="emp.job === '4'">教研主管</span>
            <span v-else-if="emp.job === '5'">咨询师</span>
          </td>
          <td>{{ emp.entrydate }}</td>
          <td>{{ emp.updatetime }}</td>
          <td class="btn-group">
            <button class="edit">编辑</button>
            <button class="delete">删除</button>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- 页脚版权 -->
    <footer class="footer">
      <p class="company-name">江苏传智播客教育科技股份有限公司</p>
      <p class="copyright">版权所有 Copyright 2006-2024 All Rights Reserved</p>
    </footer>

    <!-- Vue 3 入口脚本 -->
    <script type="module">
      import { createApp } from 'https://unpkg.com/vue@3/dist/vue.esm-browser.js'

      createApp({
        data() {
          return {
            // 搜索表单双向绑定对象
            searchEmp: {
              name: '',
              gender: '',
              job: ''
            },
            // 员工列表数据
            empList: [
              {
                "id": 1,
                "name": "谢逊",
                "image": "https://web-framework.oss-cn-hangzhou.aliyuncs.com/2023/4.jpg",
                "gender": 1,
                "job": "1",
                "entrydate": "2023-06-09",
                "updatetime": "2024-07-30T14:59:38"
              },
              {
                "id": 2,
                "name": "韦一笑",
                "image": "https://web-framework.oss-cn-hangzhou.aliyuncs.com/2023/1.jpg",
                "gender": 1,
                "job": "1",
                "entrydate": "2020-05-09",
                "updatetime": "2023-07-01T00:00:00"
              },
              {
                "id": 3,
                "name": "黛绮丝",
                "image": "https://web-framework.oss-cn-hangzhou.aliyuncs.com/2023/2.jpg",
                "gender": 2,
                "job": "2",
                "entrydate": "2021-06-01",
                "updatetime": "2023-07-01T00:00:00"
              }
            ]
          }
        },
        methods: {
          // 查询按钮：打印当前搜索条件
          search() {
            console.log(this.searchEmp)
          },
          // 清空按钮：重置搜索表单
          clear() {
            this.searchEmp = {
              name: '',
              gender: '',
              job: ''
            }
          }
        }
      }).mount('#container')
    </script>
  </div>
</body>
</html>
```

### 项目知识点梳理

| 知识点 | 在项目中的体现 |
|--------|---------------|
| `createApp({...})` | 创建 Vue 应用实例 |
| `data()` | 定义响应式数据 `searchEmp` 和 `empList` |
| `methods` | 定义 `search()` 和 `clear()` 方法 |
| `.mount('#container')` | 将 Vue 挂载到 id 为 container 的 DOM 元素 |
| `v-model` | 搜索表单输入框双向绑定 `searchEmp` |
| `v-for` | 遍历员工列表渲染表格行 |
| `v-if / v-else-if` | 根据职位编码显示对应中文名称 |
| `:src` (v-bind 简写) | 动态绑定员工头像 URL |
| `@click` (v-on 简写) | 查询按钮和清空按钮的事件处理 |
| `{{ }}` 插值表达式 | 显示员工姓名、日期等文本数据 |

---

## 三、Ajax 与 Axios

### 3.1 什么是 Ajax

**Ajax**（Asynchronous JavaScript and XML）是一种在不刷新整个页面的情况下与服务器进行**异步数据交换**的技术。

| 交互方式 | 说明 | 用户体验 |
|----------|------|----------|
| 同步交互 | 客户端发送请求后，必须等待服务器响应完成后才能继续操作 | 页面卡顿，体验差 |
| 异步交互 | 客户端发送请求后，无需等待服务器响应，可继续执行其他代码 | 页面流畅，体验好 |

### 3.2 什么是 Axios

**Axios** 是对原生 Ajax（XMLHttpRequest）的封装库，提供更简洁的 API，支持 Promise，支持请求/响应拦截等。

### 3.3 基本用法

**通用写法：**

```javascript
axios({
  url: 'https://api.example.com/employees',
  method: 'get'   // 或 'post'
}).then((result) => {
  // 请求成功回调
  console.log(result.data)
}).catch((err) => {
  // 请求失败回调
  console.log('请求失败:', err)
})
```

**简化写法：**

```javascript
// GET 请求
axios.get('https://api.example.com/employees')
  .then((result) => {
    console.log(result.data)
  }).catch((err) => {
    console.log(err)
  })

// POST 请求
axios.post('https://api.example.com/employees', {
  name: '张三',
  gender: 1
}).then((result) => {
  console.log(result.data)
}).catch((err) => {
  console.log(err)
})
```

### 3.4 异步特性（重要）

```javascript
console.log('开始请求')
axios.get(url).then((result) => {
  console.log('收到响应:', result.data)
})
console.log('123456')
```

**输出顺序**：

```
开始请求
123456
收到响应: { ... }
```

> **原因**：Axios 发起的是异步请求，执行到 `axios.get()` 时会发出请求但**不等待响应**，继续往下执行 `console.log("123456")`。等服务器返回数据后，才触发 `.then()` 中的回调。

### 3.5 async / await

`async/await` 是 ES2017 引入的语法，让异步代码写起来像同步代码。

```javascript
// 声明一个异步方法
async function fetchData() {
  try {
    // await 会等待 axios 请求完成后再继续
    const result = await axios.get('https://api.example.com/employees')
    console.log(result.data)   // 这行会等上面请求完成才执行
    console.log('请求完成')
  } catch (err) {
    console.log('请求失败:', err)
  }
}
```

| 语法 | 说明 |
|------|------|
| `async` | 声明一个函数是异步函数，返回值会被包装成 Promise |
| `await` | 等待一个 Promise 完成，将异步变为同步等待，只能在 `async` 函数内使用 |

---

## 四、Vue 生命周期

### 4.1 什么是生命周期

Vue 实例从创建到销毁的过程称为**生命周期**。Vue 在这些关键阶段会调用**钩子函数（Hook）**，让我们有机会在特定时机插入自己的代码。

### 4.2 生命周期钩子

```
创建前   →  创建后  →  挂载前  →  挂载后  →  更新前  →  更新后  →  卸载前  →  卸载后
beforeCreate  created  beforeMount  mounted  beforeUpdate  updated  beforeUnmount  unmounted
```

### 4.3 常用钩子函数

| 钩子函数 | 触发时机 | 典型用途 |
|----------|----------|----------|
| `created()` | 实例创建完毕，data 和 methods 已可用，但 DOM 尚未挂载 | 初始化非 DOM 数据、设置定时器 |
| `beforeMount()` | 挂载开始之前，模板已编译但尚未渲染到页面 | 较少使用 |
| `mounted()` | 实例挂载到 DOM 完毕后 | **发送初始数据请求**、操作 DOM 元素 |
| `beforeUpdate()` | 数据变化后、DOM 重新渲染前 | 在更新前获取旧 DOM 状态 |
| `updated()` | 数据变化导致 DOM 重新渲染后 | 依赖更新后 DOM 的操作 |
| `beforeUnmount()` | 实例卸载前，实例功能仍然完整 | 清理定时器、取消事件监听 |
| `unmounted()` | 实例卸载后 | 销毁后的清理工作 |

### 4.4 mounted 使用示例

```javascript
createApp({
  data() {
    return { empList: [] }
  },
  // 挂载完毕后自动执行，发送请求获取初始数据
  mounted() {
    console.log('Vue 挂载完毕，发送请求获取数据...')
    axios.get('https://api.example.com/employees')
      .then((result) => {
        this.empList = result.data
      })
  }
}).mount('#app')
```

> **mounted** 是最常用的钩子之一，页面加载完成后在这里发起数据请求，把后端数据填充到页面。

---

## 五、Maven

### 5.1 什么是 Maven

Maven 是 Java 项目**依赖管理**和**项目构建**工具。它统一了项目结构，让不同 IDE 打开的项目都能正确编译运行。

| 核心作用 | 说明 |
|----------|------|
| 依赖管理 | 自动下载项目需要的 jar 包，无需手动复制 |
| 项目构建 | 一条命令完成编译、测试、打包、部署 |
| 统一项目结构 | 约定优于配置，所有 Maven 项目结构一致 |

### 5.2 仓库层级

```
本地仓库（~/.m2/repository）
    ↓ 本地没有时查找
私服（公司内部 Nexus 等）
    ↓ 私服没有时查找
中央仓库（Maven Central）
```

### 5.3 Maven 坐标

Maven 通过**坐标**唯一标识一个依赖。

| 坐标要素 | 说明 | 示例 |
|----------|------|------|
| `groupId` | 组织域名反写 | `com.alibaba` |
| `artifactId` | 模块名称 | `fastjson` |
| `version` | 版本号 | `2.0.32` |

**版本号后缀含义：**

| 后缀 | 含义 |
|------|------|
| `SNAPSHOT` | 开发中的快照版，不稳定，每次构建都会检查更新 |
| `RELEASE` | 正式发行版，稳定可靠 |

### 5.4 依赖配置

在 `pom.xml` 中声明依赖：

```xml
<dependencies>
    <dependency>
        <groupId>com.alibaba</groupId>    <!-- 域名反写 -->
        <artifactId>fastjson</artifactId>  <!-- 模块名 -->
        <version>2.0.32</version>          <!-- 版本号 -->
    </dependency>
</dependencies>
```

### 5.5 排除依赖

```xml
<dependency>
    <groupId>org.example</groupId>
    <artifactId>module-a</artifactId>
    <version>1.0</version>
    <exclusions>
        <exclusion>
            <!-- 排除 module-a 传递引入的某个冲突依赖 -->
            <groupId>org.conflict</groupId>
            <artifactId>conflict-lib</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

### 5.6 Maven 生命周期

Maven 定义了三套独立的生命周期：

| 生命周期 | 作用 |
|----------|------|
| `clean` | 清理项目（删除 target 目录等） |
| `default` | 核心工作（编译、测试、打包、安装） |
| `site` | 生成项目报告和文档 |

### 5.7 default 生命周期阶段

| 阶段 | 命令 | 说明 |
|------|------|------|
| 编译 | `mvn compile` | 将 Java 源码编译为 `.class` 字节码 |
| 测试 | `mvn test` | 运行单元测试 |
| 打包 | `mvn package` | 打成 jar 包或 war 包 |
| 安装 | `mvn install` | 将 jar 包安装到本地仓库，供其他项目使用 |
| 部署 | `mvn deploy` | 将 jar 包部署到私服 |

> **顺序执行规则**：在同一套生命周期中，运行后面的阶段时，前面的阶段**会自动执行**。例如执行 `mvn install` 时，compile → test → package 都会依次执行。

### 5.8 Maven 标准目录结构

```
项目根目录
├── pom.xml                 # Maven 配置文件
├── src/
│   ├── main/
│   │   ├── java/           # Java 源码
│   │   └── resources/      # 配置文件
│   └── test/
│       ├── java/           # 测试源码
│       └── resources/      # 测试资源
└── target/                 # 构建输出目录（自动生成）
```

---

## 六、软件测试

### 6.1 测试分类

| 分类 | 英文 | 说明 | 适用阶段 |
|------|------|------|----------|
| 白盒测试 | White Box | 清楚软件内部结构，验证代码逻辑和正确性 | 单元测试 |
| 灰盒测试 | Gray Box | 结合内部结构知识和外部功能表现 | 集成测试 |
| 黑盒测试 | Black Box | 完全不了解内部实现，只关注输入和输出 | 系统测试 / 验收测试 |

### 6.2 白盒测试 vs 黑盒测试 vs 灰盒测试

| 对比维度 | 白盒测试 | 灰盒测试 | 黑盒测试 |
|----------|----------|----------|----------|
| 内部结构 | 完全了解 | 部分了解 | 完全不了解 |
| 测试依据 | 代码逻辑 | 代码逻辑 + 功能需求 | 功能需求 |
| 关注点 | 代码覆盖率、逻辑分支 | 接口交互、数据传递 | 功能是否正常、兼容性 |
| 执行者 | 开发人员 | 开发/测试人员 | 测试人员 |
| 典型阶段 | 单元测试 | 集成测试 | 系统测试 / 验收测试 |

### 6.3 测试层级金字塔

```
        ┌─────────────┐
        │ 验收测试     │  ← 黑盒（用户验收）
        ├─────────────┤
        │ 系统测试     │  ← 黑盒（完整系统功能）
        ├─────────────┤
        │ 集成测试     │  ← 灰盒（模块间协作）
        ├─────────────┤
        │ 单元测试     │  ← 白盒（最小功能单元）
        └─────────────┘
```

### 6.4 JUnit 单元测试

JUnit 是 Java 标准单元测试框架，通过 `@Test` 注解标记测试方法。

```java
import org.junit.Test;

public class CalculatorTest {

    @Test
    public void testAdd() {
        Calculator cal = new Calculator();
        int result = cal.add(2, 3);
        // 断言期望值 == 实际值
        org.junit.Assert.assertEquals(5, result);
    }

    @Test
    public void testSubtract() {
        Calculator cal = new Calculator();
        int result = cal.subtract(10, 3);
        org.junit.Assert.assertEquals(7, result);
    }
}
```

**JUnit 关键注解：**

| 注解 | 说明 |
|------|------|
| `@Test` | 标记一个方法为测试方法 |
| `@Before` | 在每个 `@Test` 方法执行前运行 |
| `@After` | 在每个 `@Test` 方法执行后运行 |
| `@BeforeClass` | 在所有测试方法之前运行一次（静态方法） |
| `@AfterClass` | 在所有测试方法之后运行一次（静态方法） |


---

## 今日总结

| 模块 | 核心要点 |
|------|----------|
| Vue.js | 渐进式框架，七大核心指令（插值/v-on/v-bind/v-model/v-for/v-if/v-show） |
| Tlias 项目 | Vue 3 完整单页应用，createApp + data + methods + mount |
| Ajax/Axios | 异步数据交换，Axios 封装，async/await 处理异步 |
| Vue 生命周期 | 8 个钩子函数，mounted 最常用（初始数据请求） |
| Maven | 依赖管理 + 构建工具，三层仓库体系，坐标三要素，生命周期顺序执行 |
| 软件测试 | 白盒/灰盒/黑盒，四层测试金字塔，JUnit @Test |

