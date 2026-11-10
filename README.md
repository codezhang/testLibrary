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

一个完整初始化序列：

```bash
java -jar testlibrary-wrapper/target/testlibrary-wrapper-shell.jar \
  script testlibrary-wrapper/scripts/setup-user-book.shell
```

`createDuckDb` 会创建一个空 DuckDB 文件并执行 `schema.sql` 中的 DDL，不写入种子数据。
`addUser` 创建账号和用户；密码使用 BCrypt 哈希存储。

Shell 的 `loginAs` / `logout` 仅用于测试时切换身份，日常使用默认就是 admin。

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

`BookingTransactionService` 只依赖接口，切换到 Redis 或数据库锁不需要修改业务代码。

### 传输层

`testlibrary-spring-boot-web` 是传输适配器之一，不是业务核心。Netty 等实现可以替换它，
只需要复用 core 的业务服务和会话/认证接口。wrapper 负责导入对应传输配置。

链式路由示例见 [docs/netty-transport-example.md](docs/netty-transport-example.md)。

## 说明

- `技术说明.md` 和 `格式说明.md` 是早期架构资料，保留作参考。
- 内存模式账号在进程内存中；JDBC 模式账号存储在 `user_account` 表。
