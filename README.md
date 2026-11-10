# TestLibrary

图书预约与借阅示例，采用契约/业务核心 + 可替换持久化 + 可替换传输 + wrapper 组合的模块化结构。

## 模块

```
TestLibrary
├── testlibrary-core                 业务核心、OpenAPI 生成代码、认证/会话、锁/分页
├── testlibrary-persistence-jdbc     持久化实现之一：PostgreSQL 方言 + DuckDB/PostgreSQL
├── testlibrary-spring-shell         Spring Shell 传输适配
├── testlibrary-spring-boot-web      Spring MVC 传输适配
└── testlibrary-wrapper              组合与打包入口，支持 web / shell 两种构建
```

依赖方向：

```
core ← persistence-jdbc
core ← spring-shell
core ← spring-boot-web
以上全部 ← wrapper
```

`wrapper` 是唯一可运行项目，`spring-shell` 和 `spring-boot-web` 不能单独启动。

## 数据模型

核心实体是 `user_account` 和 `booking_item`。

`user_account`：

- `user_id`：用户主键
- `username`：登录名，唯一
- `password_hash`：BCrypt 哈希
- `roles`：逗号分隔的角色列表

`booking_item`：

- `item_id`：书籍/预约条目主键
- `title`：标题
- `preview_content`：预览内容，最长 2000 字
- `status`：`AVAILABLE` / `LENT` / `INACTIVE`
- `borrowed_by`：当前借出人的 `user_id`，指向 `user_account`
- `version`：更新版本号

关系：一个用户可以借出多条 `booking_item`，一条 `booking_item` 同一时间最多只有一个借出人。
`status='LENT'` 时 `borrowed_by` 有值；归还后 `borrowed_by` 置空。当前 DDL 使用逻辑关联，未加数据库外键约束。

简化 DDL：

```sql
CREATE TABLE user_account (
    user_id       VARCHAR PRIMARY KEY,
    username      VARCHAR NOT NULL UNIQUE,
    password_hash VARCHAR NOT NULL,
    roles         VARCHAR NOT NULL
);

CREATE TABLE booking_item (
    item_id         BIGINT PRIMARY KEY,
    title           VARCHAR NOT NULL,
    preview_content VARCHAR(2000),
    status          VARCHAR NOT NULL DEFAULT 'AVAILABLE',
    borrowed_by     VARCHAR,
    version         BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT booking_item_status_chk
        CHECK (status IN ('AVAILABLE', 'LENT', 'INACTIVE'))
);
```

完整 DDL 和兼容迁移在 `testlibrary-persistence-jdbc/src/main/resources/db/postgresql/schema.sql`。

## 构建

```bash
mvn clean install
```

Reactors 会按依赖顺序构建全部模块。

### 两种打包模式

`testlibrary-wrapper` 通过 Maven profile 生成两个可执行包：

```bash
mvn -f testlibrary-wrapper/pom.xml -Pweb package
mvn -f testlibrary-wrapper/pom.xml -Pshell package
```

产物：

```
testlibrary-wrapper/target/testlibrary-wrapper-web.jar
testlibrary-wrapper/target/testlibrary-wrapper-shell.jar
```

运行：

```bash
java -jar testlibrary-wrapper/target/testlibrary-wrapper-web.jar
java -jar testlibrary-wrapper/target/testlibrary-wrapper-shell.jar
```

- web 模式：只启动 Web 传输，提供 HTTP 接口和 Vue 页面
- shell 模式：只启动 Spring Shell，不启动 Web 容器

两种模式共享 core 的业务、认证和持久化选择逻辑。

### 按场景组装 wrapper

wrapper 的 `pom.xml` 是组装清单，实际打包时可以只保留当前场景需要的模块和启动配置：

- 依赖：`testlibrary-core` 必选，传输和持久化按需引入
- 启动类：只 `@Import` 对应传输配置
- 持久化：用 `library.persistence` 选择 memory / jdbc

常见组合：

| 场景 | wrapper 依赖 | 启动配置 |
| --- | --- | --- |
| Web + 内存 | core、spring-boot-web | `WebWrapperApplication` + 内存持久化配置 |
| Web + JDBC | core、spring-boot-web、persistence-jdbc | `WebWrapperApplication` + jdbc profile |
| Shell + 内存 | core、spring-shell | `ShellWrapperApplication` |
| Shell + JDBC | core、spring-shell、persistence-jdbc | `ShellWrapperApplication` + jdbc profile |
| Web + Netty | core、netty 传输、persistence-jdbc | Netty 启动类 + Netty 传输配置 |

示例 Maven 结构：

```xml
<dependencies>
    <dependency>
        <groupId>org.example</groupId>
        <artifactId>testlibrary-core</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
</dependencies>

<profiles>
    <profile>
        <id>web-jdbc</id>
        <dependencies>
            <dependency>
                <groupId>org.example</groupId>
                <artifactId>testlibrary-spring-boot-web</artifactId>
                <version>1.0-SNAPSHOT</version>
            </dependency>
            <dependency>
                <groupId>org.example</groupId>
                <artifactId>testlibrary-persistence-jdbc</artifactId>
                <version>1.0-SNAPSHOT</version>
            </dependency>
        </dependencies>
    </profile>
    <profile>
        <id>shell-memory</id>
        <dependencies>
            <dependency>
                <groupId>org.example</groupId>
                <artifactId>testlibrary-spring-shell</artifactId>
                <version>1.0-SNAPSHOT</version>
            </dependency>
        </dependencies>
    </profile>
</profiles>
```

当前示例 wrapper 为了便于演示，两个传输依赖都放在基础依赖里，通过 profile 切换主类和打包名；
正式项目可以按上表裁剪依赖，避免把不用的传输和中间件打进产物。

### 持久化模式

默认是内存模式，不需要数据库：

```bash
java -jar testlibrary-wrapper/target/testlibrary-wrapper-web.jar
```

JDBC 模式使用本地 DuckDB 文件（SQL 同时兼容 PostgreSQL）：

```bash
SPRING_PROFILES_ACTIVE=jdbc \
  java -jar testlibrary-wrapper/target/testlibrary-wrapper-web.jar
```

JDBC 配置在 `testlibrary-wrapper/src/main/resources/application-jdbc.properties`：

```
library.persistence=jdbc
library.jdbc.url=jdbc:duckdb:./data/testlibrary.duckdb
library.jdbc.driver-class-name=org.duckdb.DuckDBDriver
library.jdbc.pool-size=1
library.jdbc.init-schema=true
```

DDL 和种子数据：

```
testlibrary-persistence-jdbc/src/main/resources/db/postgresql/schema.sql
testlibrary-persistence-jdbc/src/main/resources/db/postgresql/data.sql
```

`booking_item` 包含 `preview_content VARCHAR(2000)`。schema 脚本带有列级兼容迁移，旧库启动时会补齐该字段。
`user_account` 保存用户、BCrypt 密码哈希和角色。

`testlibrary-persistence-jdbc` 只是 `BookingTransactionMapper` 的一种实现，使用 PostgreSQL 方言 SQL 跑在 DuckDB/PostgreSQL 上。接入其他数据库或存储时，实现 `org.example.core.mapper.BookingTransactionMapper`，由 wrapper 选择对应 Bean 即可，core 和传输层不需要改动。

### 跨进程共享数据

web 模式与 shell 模式是两个独立进程。内存模式下数据不共享；需要“Shell 建用户/书，再在 Web 登录借阅”时，使用同一份 JDBC 数据：

```bash
DB=/tmp/testlibrary.duckdb

java -Dspring.profiles.active=jdbc \
     -Dlibrary.jdbc.url=jdbc:duckdb:$DB \
     -jar testlibrary-wrapper/target/testlibrary-wrapper-shell.jar \
     script testlibrary-wrapper/scripts/setup-user-book.shell

java -Dspring.profiles.active=jdbc \
     -Dlibrary.jdbc.url=jdbc:duckdb:$DB \
     -jar testlibrary-wrapper/target/testlibrary-wrapper-web.jar
```

这样用户、书籍、预约状态都通过 DuckDB 在两种模式之间共享。

## OpenAPI 与代码生成

接口模型和路由描述类由 OpenAPI 定义自动生成，不手工维护：

- 契约：`testlibrary-core/src/main/resources/openapi/kernel-user.openapi.yaml`
- 模板：`testlibrary-core/src/main/resources/templates/*.mustache`
- 生成器：`openapi-generator-maven-plugin`，绑定 `generate-sources`
- 产物：`testlibrary-core/target/generated-sources/openapi/...`

生成内容包括：

- `BookingItem`、`BookingBatchRequest` 等模型
- 每个接口对应的描述类，如 `RequireBookingResourceApi`、`GetBookingItemListApi`
- 自定义 Mustache 模板输出 `KernelResp<T>` 包装、路径常量和嵌套 RequestBody/ResponseBody

在 IDE 中可以使用 OpenAPI Specifications / Swagger 插件编辑和校验 YAML；修改契约后执行 Maven
`generate-sources`，或开启 IDEA 的 Delegate build to Maven，生成源码会同步刷新。
生成目录是源码根，不应手工编辑。

## Shell

Shell 默认就是管理员，不需要登录。

常用命令：

```
createDuckDb --path ./data/library.duckdb
addUser --userId 2001 --username alice --password alice123 --roles user
addBookingItem --title "领域驱动设计"
bookingList --pageNum 1 --pageSize 10
borrowBooking --itemIds 1,2
releaseBooking --itemIds 1,2
myBookings
deactivateBooking --itemId 4
activateBooking --itemId 4
removeBooking --itemId 4
loginAs --username user --roles user
logout
whoami
```

完整流程：

```text
# 1. 默认就是管理员，无需登录
createDuckDb --path ./data/library.duckdb

addUser --userId 2001 --username alice --password alice123 --roles user

addBookingItem --title "领域驱动设计"

bookingList --pageNum 1 --pageSize 10

# 2. 切换到普通用户
loginAs --username alice --userId 2001 --roles user
whoami

# 3. 普通用户借书
borrowBooking --itemIds <addBookingItem 返回的 itemId>
myBookings

# 4. 普通用户归还
releaseBooking --itemIds <itemId>
myBookings

# 5. 回到管理员
logout
whoami
```

也可以直接执行初始化脚本：

```bash
java -jar testlibrary-wrapper/target/testlibrary-wrapper-shell.jar \
  script testlibrary-wrapper/scripts/setup-user-book.shell
```

说明：

- `createDuckDb` 创建空 DuckDB 文件并执行 `schema.sql` 中的 DDL，不写种子数据
- 若希望后续命令使用该库，启动 shell 时加 `-Dspring.profiles.active=jdbc -Dlibrary.jdbc.url=jdbc:duckdb:<path>`
- `addUser` 同时创建账号和用户；密码使用 BCrypt 哈希
- `addBookingItem`、`deactivateBooking`、`activateBooking`、`removeBooking` 需要 admin 角色
- 普通用户可以查看列表、借出和归还；管理员命令在 user 身份下会返回缺少角色错误
- Shell 的 `loginAs` / `logout` 仅用于测试时切换身份，日常使用默认就是 admin
- web 与 shell 跨进程共享数据时，使用同一 DuckDB 和 `SPRING_PROFILES_ACTIVE=jdbc`

## Web

打开 `http://127.0.0.1:8080/`。

内置账号：

- 管理员：`admin / admin123`
- 通过 Shell `addUser` 创建的账号，例如：`alice / alice123`

页面功能：

- 选择「已借书籍」查看当前用户借出的条目，勾选后归还
- 选择「全部书籍」查看可借条目，勾选后借出；已借出/停用条目会在前端过滤
- 每条书籍支持「预览」，查看 `previewContent`（最多 2000 字）

登录后用户信息保存在服务端 session，浏览器只保存 `KERNEL_SESSION` cookie。密码只保存 BCrypt 哈希。

主要接口：

```
POST /api/login
POST /api/logout
GET  /api/me

GET  /kernel/books/list
GET  /kernel/books/userBorrowed
POST /kernel/booking/borrow
POST /kernel/booking/return
POST /kernel/books/add
POST /kernel/books/deactivate
POST /kernel/books/activate
POST /kernel/books/remove
```

## 扩展点

### 锁

core 只定义 `LockService` / `LockHandle` 接口，默认实现为进程内 `LocalLockService`。
外部项目可以提供自己的实现：

```java
@Bean
@Primary
LockService lockService(RedisClient redisClient) {
    return new RedisLockService(redisClient);
}
```

也可以实现基于 etcd 的分布式锁，例如 `EtcdLockService`：

- `acquire()` 使用 etcd lease 创建锁 key，通过事务比较 `create_revision == 0` 抢占
- `LockHandle` 持有 lease id，后台 keep-alive 续约
- `release()` 撤销 lease 或删除锁 key

```java
@Bean
@Primary
LockService etcdLockService(EtcdClient etcd) {
    return new EtcdLockService(etcd, "/testlibrary/locks/booking");
}
```

`BookingTransactionService` 只依赖 `LockService` 接口，切换到 Redis、etcd 或数据库锁不需要修改业务代码。

当前锁粒度说明：

- 目前是一个服务级锁，没有细化到具体的书籍 `itemId` 粒度
- `LocalLockService` 只在单进程内有效；web 和 shell 是独立进程，内存模式下无法互斥
- 即使换成 Redis / etcd 分布式锁，当前实现仍会串行化整个借还/管理操作，而不是按书籍加锁
- shell 的 `activateBooking` / `deactivateBooking` / `removeBooking` 与 web 的借出/归还可能冲突；
  现有 SQL 条件只能覆盖部分情况（停用只允许 `AVAILABLE`，删除排除 `LENT`），不能替代严格的按书锁
- 后续建议按 `booking:{itemId}` 生成锁 key，批量操作按 id 排序后依次获取，避免死锁；
  或使用数据库行级锁 / `version` 乐观锁，让 web 借还与 shell 管理操作使用同一套书籍级保护

### 传输层

`testlibrary-spring-boot-web` 是传输适配器之一，不是业务核心。Netty 等实现可以替换它，
只需要复用 core 的业务服务和会话/认证接口。wrapper 负责导入对应传输配置。

链式路由示例见 [docs/netty-transport-example.md](docs/netty-transport-example.md)。

## 说明
- 内存模式账号在进程内存中；JDBC 模式账号存储在 `user_account` 表。
