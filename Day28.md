
# Day28 - 苍穹外卖（微信登录与商品浏览）

## 一、httpclient

**是什么？**

一个在java中发送get或者post请求的工具类

项目在 `sky-common` 模块的 `HttpClientUtil` 中封装了 `doGet`、`doPost`、`doPost4Json` 三个方法，底层基于 Apache HttpClient 实现，统一处理了参数拼接、超时配置与响应读取。

**拿来干什么？**

可以获取到请求后的响应数据

`doGet` 发送请求后先判断响应状态码是否为 200，是则将响应体以 UTF-8 编码转为字符串返回，方便上层直接解析 JSON 数据。

**在这个项目中有什么用？**

调用微信第三方接口，获取用户openid

`UserServiceImpl.wxLogin` 中把 appid、secret、js_code、grant_type 组装成参数，调用 `HttpClientUtil.doGet` 请求微信 `jscode2session` 接口，再解析返回的 JSON 取出 openid 完成登录。

## 二、小程序开发的项目结构

- .js：负责页面逻辑与交互处理

- .json：负责页面配置，如窗口样式、导航栏、组件配置等，全局配置在 app.json

- .wxml：负责页面结构，即标签布局

- .wxss：负责页面样式

## 三、使用微信开发者助手在获取用户昵称和头像时一些问题

因为调试基础库版本太高，抛弃了原来的getUserInfo和getUserProfile接口

解决方案：

可以降低调试基础库版本，或者参照微信官方文档使用最新头像昵称填写功能

新版基础库已不再支持通过 getUserInfo 直接获取头像昵称，推荐改用头像选择组件与昵称输入框的组合方案实现用户信息填写。

## 四、微信登录功能用到的一些技术

- jwt令牌：登录成功后 `UserController` 用 `JwtUtil.createJWT` 生成令牌，claims 中放入当前用户 id，返回给小程序端供后续请求携带校验。
- httpclient：`UserServiceImpl` 用它调用微信接口换取 openid。
- 主键回显：新用户注册时执行 `userMapper.insert(user)`，插入后 `user.getId()` 能直接拿到数据库自增生成的主键，无需再单独查询。

整个登录流程如下图所示

![[Pasted image 20260823183540.png|476]]

## 五、商品浏览功能开发

重要的能力是接口设计和分析

菜品接口查询前会先设置菜品状态为起售中，保证只展示在售商品；接口统一按分类 id 条件查询，便于小程序端按分类逐级展示。

## 今日总结

| 知识点        | 要点说明                                                   |
| ---------- | ------------------------------------------------------ |
| httpclient | HttpClientUtil 封装 doGet/doPost/doPost4Json，发送请求并获取响应数据 |
| 项目结构       | 小程序端由 .js、.json、.wxml、.wxss 四类文件构成，分别负责逻辑、配置、结构、样式     |
| 头像昵称问题     | 基础库版本过高导致 getUserInfo/getUserProfile 失效，可降版本或改用新版填写功能  |
| 微信登录技术     | jwt令牌、httpclient、主键回显三者配合完成登录与身份标识                     |
| 商品浏览       | 接口设计与分析是核心，分类/菜品/套餐接口按条件查询在售商品                         |

