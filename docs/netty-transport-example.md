# Netty 传输替代示例

`spring-boot-web` 只是传输适配器。替换为 Netty 时，core 的业务服务、认证和持久化都不需要改，
只需要新增一个传输模块，并在 wrapper 中导入它的配置。

## 路由声明

```java
public final class NettyBookingTransport {

    public void register(KernelRoutes routes,
                         BookingTransactionService booking) {

        routes
            .group("/kernel", group -> group
                .post("/booking/borrow", ctx -> booking.requireBookingResource(
                        toItems(ctx.body(BookingBatchRequest.class).getItemIds())))
                .post("/booking/return", ctx -> booking.releaseBookingResource(
                        toItems(ctx.body(BookingBatchRequest.class).getItemIds())))
                .get("/books/userBorrowed", ctx -> booking.getUserBorrowed())
                .get("/books/list", ctx -> booking.getBookingItemList(
                        ctx.query("pageNum", 1), ctx.query("pageSize", 10)))
                .post("/books/add", ctx -> booking.addNewBookingItem(
                        ctx.body(BookingAddRequest.class).getTitle()))
                .post("/books/deactivate", ctx -> booking.deactivateBookingItem(
                        ctx.body(BookingItemIdRequest.class).getItemId()))
                .post("/books/activate", ctx -> booking.activateBookingItem(
                        ctx.body(BookingItemIdRequest.class).getItemId()))
                .post("/books/remove", ctx -> booking.removeBookingItem(
                        ctx.body(BookingItemIdRequest.class).getItemId()))
            );
    }

    private static List<BookingItem> toItems(List<Long> itemIds) {
        if (itemIds == null) {
            return List.of();
        }
        return itemIds.stream()
                .map(id -> new BookingItem(id, null))
                .toList();
    }
}
```

## wrapper 切换

只引入 Netty 传输模块，不再导入 `WebModuleConfiguration`：

```java
@SpringBootApplication
@Import({
        WrapperCommonConfiguration.class,
        AuthCoreConfiguration.class,
        CoreServiceConfiguration.class,
        NettyTransportConfiguration.class
})
public class NettyWrapperApplication {
    public static void main(String[] args) {
        SpringApplication.run(NettyWrapperApplication.class, args);
    }
}
```

对应的 Maven 依赖替换：

```xml
<dependency>
    <groupId>org.example</groupId>
    <artifactId>testlibrary-netty-transport</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

Netty 传输负责 HTTP 解析、cookie/session、登录和错误状态码；core 不感知底层是 Spring MVC 还是 Netty。
