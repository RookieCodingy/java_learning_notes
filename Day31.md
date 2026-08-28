# Day31 ：WebSocket、数据统计与报表

## 一、WebSocket

### 1.1 概述
- **WebSocket 是基于 TCP 的一种新的网络协议**，支持浏览器与服务器之间**全双工通信**。
- 核心特点：
  - 一次握手建立连接；
  - 持久性连接；
  - 双向数据传输。

### 1.2 与 HTTP 的对比

| 对比项  | HTTP          | WebSocket  |
| ---- | ------------- | ---------- |
| 连接类型 | 短链接（一次请求一次响应） | 长连接（持久性连接） |
| 通信方向 | 单向，基于请求响应模式   | 双向（全双工）    |
| 底层协议 | 基于 TCP 协议     | 基于 TCP 协议  |

- 相同点：HTTP 与 WebSocket **都基于 TCP 协议**。
- 主要区别：HTTP 是短链接、单向通信；WebSocket 是长连接、双向通信。

### 1.3 应用场景
- 视频弹幕、网页聊天、体育实况更新、股票基金报价实时更新。
- 特点：**不需要浏览器发送请求，服务器可以直接推送消息**。

---

## 二、今日问题与解决方案

### 2.1 问题描述
- 因为我的 80 端口被占用，将 nginx 监听端口改到 90 端口，而 nginx 反向代理 WebSocket 时，WebSocket 中配置的仍然是 80 端口，导致无法进行反向代理。

### 2.2 解决方案
- 修改前端 `app.d0aa4eb3.js` 文件，将 `localhost` 改为 `localhost:90`。

---

## 三、数据统计与报表

### 3.1 功能概述
- 数据统计与报表模块用于管理端查看营业数据，共包含**四项统计功能**：
  1. 营业额统计
  2. 用户统计
  3. 订单统计
  4. 销量排名 Top10
- 接口统一由 `ReportController` 提供，请求路径前缀为 `/admin/report`，时间参数统一为 `begin` / `end`（`LocalDate`，格式 `yyyy-MM-dd`）。


### 3.2 营业额统计
- 统计口径：每天**已完成订单**（status = 5，即 `Orders.COMPLETED`）的金额合计。
- 实现思路（`ReportServiceImpl.getTurnoverStatistics`）：
  1. 生成 begin 到 end 的日期列表（begin 逐天 `plusDays(1)` 直到 end）；
  2. 遍历每一天，用 `LocalDateTime.of(date, LocalTime.MIN)` 与 `LocalDateTime.of(date, LocalTime.MAX)` 取当天 00:00:00 ~ 23:59:59 作为时间范围；
  3. 调用 `OrderMapper.sumByMap`，按 `beginTime` / `endTime` / `status=5` 动态条件求和；
  4. 结果为 null 时置为 0.0，最终以逗号分隔字符串返回。
- 核心 SQL（OrderMapper.xml `sumByMap`）：
  ```xml
  <select id="sumByMap" resultType="java.lang.Double">
      select sum(amount) from orders
      <where>
          <if test="beginTime != null"> and order_time &gt; #{beginTime} </if>
          <if test="endTime != null"> and order_time &lt;= #{endTime} </if>
          <if test="status != null"> and status = #{status} </if>
      </where>
  </select>
  ```

### 3.3 用户统计
- 统计口径：
  - **总用户数**：截止每天结束（`create_time <= endTime`）的用户总数；
  - **新增用户数**：当天注册的用户数（`create_time` 在当天范围内）。
- 实现思路（`ReportServiceImpl.getUserStatistics`）：
  1. 生成日期列表；
  2. 每天先只传 `endTime` 查询总用户数，再传 `beginTime` + `endTime` 查询新增用户数（均调用 `UserMapper.countByMap`）；
  3. 返回 `dateList` / `totalUserList` / `newUserList`。
- 核心 SQL（UserMapper.xml `countByMap`）：
  ```xml
  <select id="countByMap" resultType="java.lang.Integer">
      select count(*) from user
      <where>
          <if test="beginTime != null"> and create_time &gt;= #{beginTime} </if>
          <if test="endTime != null"> and create_time &lt;= #{endTime} </if>
      </where>
  </select>
  ```

### 3.4 订单统计
- 统计口径：
  - **每日订单总数**：当天全部订单；
  - **每日有效订单数**：当天 status = `Orders.COMPLETED`(5) 的订单；
  - **订单完成率** = 区间有效订单总数 / 区间订单总数（总数为 0 时取 0.0）。
- 实现思路（`ReportServiceImpl.getOrderStatistics`）：
  1. 生成日期列表；
  2. 每天用 `OrderMapper.countByMap` 查询订单总数，再附加 `status = Orders.COMPLETED` 查询有效订单数；
  3. 用 `stream().reduce(Integer::sum)` 汇总区间订单总数与有效订单数，计算完成率；
  

### 3.5 销量排名 Top10
- 统计口径：**仅统计已完成订单**（`o.status = 5`），按商品名称分组求和销量，按销量降序取前 10。
- 实现思路（`ReportServiceImpl.getSalesTop10`）：
  1. 计算区间的 `beginTime` / `endTime`；
  2. 调用 `OrderMapper.getSalesTop10(beginTime, endTime)`，返回 `List<GoodsSalesDTO>`（name / number）；
  3. 拆分为 `nameList` / `numberList` 逗号分隔字符串返回。
- 核心 SQL（OrderMapper.xml `getSalesTop10`）：
  ```xml
  <select id="getSalesTop10" resultType="com.sky.dto.GoodsSalesDTO">
      select od.name, sum(od.number) number
      from order_detail od
      left join orders o on od.order_id = o.id
      where o.status = 5
      <if test="beginTime != null"> and o.order_time &gt; #{beginTime} </if>
      <if test="endTime != null"> and o.order_time &lt;= #{endTime} </if>
      group by od.name
      order by number desc
      limit 10
  </select>
  ```

### 3.6 报表实现通用要点
- **日期列表生成**是四个统计接口共用的套路：`dateList.add(begin)`，然后 `while (!begin.equals(end)) { begin = begin.plusDays(1); dateList.add(begin); }`。
- **时间范围**统一用 `LocalDateTime.of(date, LocalTime.MIN)` / `LocalDateTime.of(date, LocalTime.MAX)` 表示当天 00:00:00 ~ 23:59:59。
- **动态条件查询**统一封装为 `Map` 传参（beginTime / endTime / status），配合 Mapper XML 的 `<where>` + `<if>` 动态 SQL。
- **返回格式**：列表字段统一用 `StringUtils.join(list, ",")` 拼接为逗号分隔字符串。


---

## 四、今日总结

| 模块 | 核心知识点 |
| --- | --- |
| WebSocket | 基于 TCP、全双工通信、一次握手、持久性连接、双向数据传输 |
| HTTP 与 WebSocket 对比 | HTTP 短链接、单向请求响应；WebSocket 长连接、双向通信；两者都基于 TCP |
| WebSocket 应用场景 | 视频弹幕、网页聊天、体育实况更新、股票基金报价实时更新；服务器直接推送消息 |
| 今日问题与解决 | nginx 监听端口改为 90 后 WebSocket 代理仍指向 80 → 修改前端 js 中 localhost 为 localhost:90 |
| 数据统计与报表 | 四个统计接口（营业额 / 用户 / 订单 / 销量 Top10）、Map 动态条件查询、sumByMap / countByMap / getSalesTop10、订单完成率 |

