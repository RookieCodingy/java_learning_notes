
# Day21 - Maven 高级

## 一、分模块设计与开发

### 1.1 为什么需要分模块

单体项目的问题：所有代码在一个模块中，随着业务增长，项目越来越臃肿，编译慢、耦合高、难以维护。

**分模块设计**：将项目按功能边界拆分为多个 Maven 模块，每个模块职责单一。

### 1.2 拆分策略

| 拆分维度 | 示例 |
|------|------|
| 按层次 | controller 模块 / service 模块 / mapper 模块 |
| 按业务 | 用户模块 / 订单模块 / 商品模块 |

### 1.3 实践步骤

#### 1.3.1 分析

以 Tlias 项目为例，可拆分为：

```
tlias-parent（父工程，管理依赖版本）
├── tlias-pojo（实体类模块）
├── tlias-mapper（数据访问层模块）
├── tlias-service（业务逻辑层模块）
└── tlias-web（Web 层模块，含 Controller）
```

#### 1.3.2 实现

**父工程 pom.xml**：

```xml
<packaging>pom</packaging>

<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.0</version>
</parent>

<groupId>com.tianxing</groupId>
<artifactId>tlias-parent</artifactId>
<version>1.0-SNAPSHOT</version>

<modules>
    <module>tlias-pojo</module>
    <module>tlias-mapper</module>
    <module>tlias-service</module>
    <module>tlias-web</module>
</modules>
```

**子模块间依赖关系**：

```
tlias-web → tlias-service → tlias-mapper → tlias-pojo
```

- `tlias-pojo`：独立模块，不依赖其他子模块
- `tlias-mapper`：依赖 `tlias-pojo`
- `tlias-service`：依赖 `tlias-mapper`
- `tlias-web`：依赖 `tlias-service`

子模块 pom.xml 中引入依赖示例：

```xml
<!-- tlias-service 模块的 pom.xml -->
<dependencies>
    <dependency>
        <groupId>com.tianxing</groupId>
        <artifactId>tlias-mapper</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

---

## 二、继承与聚合

### 2.1 继承

#### 2.1.1 继承关系

**问题**：多个子模块的 pom.xml 中存在大量重复配置（如依赖版本、插件配置）。

**解决**：通过父工程统一管理。

**思路**：
- 父工程的 `<packaging>` 必须为 `pom`
- 子工程通过 `<parent>` 标签声明继承

```xml
<!-- 父工程 -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.0</version>
</parent>

<!-- 子工程继承父工程 -->
<parent>
    <groupId>com.tianxing</groupId>
    <artifactId>tlias-parent</artifactId>
    <version>1.0-SNAPSHOT</version>
</parent>
```

#### 2.1.2 版本锁定

**场景**：多个子模块使用了相同的第三方依赖（如 mybatis、fastjson），每个模块都需要写版本号，版本不统一容易冲突。

**方案**：在父工程的 `<dependencyManagement>` 中统一声明版本，子模块引入时无需写版本号。

```xml
<!-- 父工程 pom.xml -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
            <version>3.0.3</version>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>fastjson</artifactId>
            <version>2.0.32</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

```xml
<!-- 子工程 pom.xml：无需写版本号 -->
<dependencies>
    <dependency>
        <groupId>org.mybatis.spring.boot</groupId>
        <artifactId>mybatis-spring-boot-starter</artifactId>
        <!-- 版本由父工程管理，此处省略 -->
    </dependency>
</dependencies>
```

| 标签 | 作用 |
|------|------|
| `<dependencies>` | 直接引入依赖，子工程自动继承 |
| `<dependencyManagement>` | 仅声明版本，子工程需显式引入才生效 |

**属性配置**：使用 `<properties>` 集中管理版本号，避免硬编码。

```xml
<properties>
    <java.version>17</java.version>
    <mybatis.version>3.0.3</mybatis.version>
    <fastjson.version>2.0.32</fastjson.version>
</properties>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
            <version>${mybatis.version}</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 2.2 聚合

**问题**：分模块后，如果逐个模块执行 `mvn install`，非常繁琐。

**解决**：父工程通过 `<modules>` 聚合所有子模块，在父工程根目录执行一次 `mvn install`，所有子模块按依赖顺序自动构建。

```xml
<!-- 父工程 pom.xml -->
<modules>
    <module>tlias-pojo</module>
    <module>tlias-mapper</module>
    <module>tlias-service</module>
    <module>tlias-web</module>
</modules>
```

构建顺序：Maven 自动分析依赖关系，从最底层的 `tlias-pojo` 开始，逐级向上构建。

### 2.3 继承与聚合对比

| 对比维度 | 继承 `<parent>` | 聚合 `<modules>` |
|------|------|------|
| 配置位置 | 子工程中声明 | 父工程中声明 |
| 作用方向 | 从父到子（版本、配置向下传递） | 从父到子（统一构建） |
| 核心作用 | 消除重复配置，统一版本管理 | 统一构建，一键编译所有模块 |
| 依赖关系 | 子 `<parent>` 继承父 | 父 `<modules>` 包含子 |

---

## 三、私服

### 3.1 场景

企业内部开发时：
- 不同项目团队需要共享自研的 jar 包
- 外网下载速度慢或不稳定
- 需要缓存中央仓库的依赖，加速构建

### 3.2 私服介绍

**私服**：架设在内网（局域网）的 Maven 仓库服务器，用于代理中央仓库和托管内部 jar 包。

| 仓库类型 | 说明 |
|------|------|
| **代理仓库** | 代理中央仓库 / 其他远程仓库，缓存下载过的依赖 |
| **宿主仓库** | 存放公司内部开发的 jar 包（Release / Snapshot） |
| **仓库组** | 将多个仓库聚合为一个 URL，简化配置 |

常用私服软件：**Nexus**、**Artifactory**。

### 3.3 资源上传与下载

#### 3.3.1 步骤分析

| 步骤 | 操作 |
|------|------|
| 1 | 搭建 Nexus 私服服务器 |
| 2 | 在 Maven 的 `settings.xml` 中配置私服认证信息 |
| 3 | 在项目的 `pom.xml` 中配置上传地址 |
| 4 | 执行 `mvn deploy` 上传 jar 包 |
| 5 | 其他项目配置私服地址，下载依赖 |

#### 3.3.2 具体配置

**settings.xml（Maven 全局配置）**：

```xml
<servers>
    <server>
        <id>tianxing-releases</id>
        <username>admin</username>
        <password>123456</password>
    </server>
    <server>
        <id>tianxing-snapshots</id>
        <username>admin</username>
        <password>123456</password>
    </server>
</servers>

<mirrors>
    <mirror>
        <id>tianxing-nexus</id>
        <mirrorOf>*</mirrorOf>
        <url>http://192.168.1.100:8081/repository/maven-public/</url>
    </mirror>
</mirrors>
```

**项目 pom.xml（上传配置）**：

```xml
<distributionManagement>
    <repository>
        <id>tianxing-releases</id>
        <url>http://192.168.1.100:8081/repository/maven-releases/</url>
    </repository>
    <snapshotRepository>
        <id>tianxing-snapshots</id>
        <url>http://192.168.1.100:8081/repository/maven-snapshots/</url>
    </snapshotRepository>
</distributionManagement>
```

**上传命令**：

```bash
mvn deploy
```

**下载**：其他项目只需在 settings.xml 中配置私服镜像地址，Maven 自动从私服拉取依赖。

---

## 今日总结

| 模块 | 核心要点 |
|------|----------|
| 分模块设计 | 按层次/业务拆分，父工程 `<packaging>pom</packaging>` + `<modules>` 管理；web → service → mapper → pojo 依赖链 |
| 继承 | 子工程 `<parent>` 继承父工程，消除重复配置；`<dependencyManagement>` 统一版本，`<properties>` 集中管理版本号 |
| 聚合 | 父工程 `<modules>` 管理子模块，一次 `mvn install` 按依赖顺序构建所有模块 |
| 继承 vs 聚合 | 继承是子声明 parent（版本向下传递），聚合是父声明 modules（统一构建） |
| 私服 | Nexus 代理 + 宿主仓库；`settings.xml` 配认证和镜像，`pom.xml` 配 `distributionManagement`，`mvn deploy` 上传 |

