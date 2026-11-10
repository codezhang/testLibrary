package org.example.shell.command;

import org.example.core.KernelResp;
import org.example.core.auth.annotation.RequireLogin;
import org.example.core.auth.annotation.RequirePriority;
import org.example.core.model.BookingItem;
import org.example.core.service.BookingTransactionService;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Command
@Component
@RequireLogin
public class BookingCommands {

    private final BookingTransactionService bookingTransactionService;

    public BookingCommands(BookingTransactionService bookingTransactionService) {
        this.bookingTransactionService = bookingTransactionService;
    }

    @Command(command = "borrowBooking", description = "Borrow booking items, itemIds=1,2")
    public String borrowBooking(@Option(longNames = "itemIds", required = true) String itemIds) {
        return text(bookingTransactionService.requireBookingResource(parseItems(itemIds)));
    }

    @Command(command = "releaseBooking", description = "Release booking items, itemIds=1,2")
    public String releaseBooking(@Option(longNames = "itemIds", required = true) String itemIds) {
        return text(bookingTransactionService.releaseBookingResource(parseItems(itemIds)));
    }

    @Command(command = "myBookings", description = "Show my borrowed booking items")
    public String myBookings() {
        return text(bookingTransactionService.getUserBorrowed());
    }

    @Command(command = "bookingList", description = "List booking items")
    public String bookingList(
            @Option(longNames = "pageNum", defaultValue = "1") int pageNum,
            @Option(longNames = "pageSize", defaultValue = "10") int pageSize) {
        return text(bookingTransactionService.getBookingItemList(pageNum, pageSize));
    }

    @Command(command = "addBooking", alias = {"addBookingItem"}, description = "Add a booking item (admin)")
    @RequirePriority({"admin"})
    public String addBooking(@Option(longNames = "title", required = true) String title) {
        return text(bookingTransactionService.addNewBookingItem(title));
    }

    @Command(command = "removeBooking", description = "Remove a booking item (admin)")
    @RequirePriority({"admin"})
    public String removeBooking(@Option(longNames = "itemId", required = true) long itemId) {
        return text(bookingTransactionService.removeBookingItem(itemId));
    }

    @Command(command = "activateBooking", description = "Activate a booking item (admin)")
    @RequirePriority({"admin"})
    public String activateBooking(@Option(longNames = "itemId", required = true) long itemId) {
        return text(bookingTransactionService.activateBookingItem(itemId));
    }

    @Command(command = "deactivateBooking", description = "Deactivate a booking item (admin)")
    @RequirePriority({"admin"})
    public String deactivateBooking(@Option(longNames = "itemId", required = true) long itemId) {
        return text(bookingTransactionService.deactivateBookingItem(itemId));
    }

    private List<BookingItem> parseItems(String itemIds) {
        List<BookingItem> items = new ArrayList<>();
        for (String part : itemIds.split(",")) {
            String value = part.trim();
            if (value.isEmpty()) {
                continue;
            }
            BookingItem item = new BookingItem();
            item.setItemId(Long.parseLong(value));
            items.add(item);
        }
        return items;
    }

    private String text(KernelResp<?> response) {
        Object data = response.getData();
        return "code=" + response.getCode()
                + ", msg=" + response.getMsg()
                + ", data=" + format(data);
    }

    private String format(Object data) {
        if (data == null) {
            return "null";
        }
        if (data instanceof List<?> list) {
            return list.stream().map(String::valueOf).collect(Collectors.joining(", ", "[", "]"));
        }
        return String.valueOf(data);
    }
}
