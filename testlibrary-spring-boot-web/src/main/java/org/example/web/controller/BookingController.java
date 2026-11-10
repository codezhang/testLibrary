package org.example.web.controller;

import org.example.core.KernelResp;
import org.example.core.auth.annotation.RequireLogin;
import org.example.core.auth.annotation.RequirePriority;
import org.example.core.generated.model.BookingAddRequest;
import org.example.core.generated.model.BookingBatchRequest;
import org.example.core.generated.model.BookingItem;
import org.example.core.generated.model.BookingItemIdRequest;
import org.example.core.service.BookingTransactionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/kernel")
@RequireLogin
public class BookingController {

    private final BookingTransactionService bookingTransactionService;

    public BookingController(BookingTransactionService bookingTransactionService) {
        this.bookingTransactionService = bookingTransactionService;
    }

    @PostMapping("/booking/borrow")
    public KernelResp<Void> borrow(@RequestBody BookingBatchRequest request) {
        return bookingTransactionService.requireBookingResource(toItems(request.getItemIds()));
    }

    @PostMapping("/booking/return")
    public KernelResp<Void> release(@RequestBody BookingBatchRequest request) {
        return bookingTransactionService.releaseBookingResource(toItems(request.getItemIds()));
    }

    @GetMapping("/books/userBorrowed")
    public KernelResp<List<org.example.core.model.BookingItem>> getUserBorrowed() {
        return bookingTransactionService.getUserBorrowed();
    }

    @GetMapping("/books/list")
    public KernelResp<List<org.example.core.model.BookingItem>> getBookingItemList(
            @RequestParam(name = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {
        return bookingTransactionService.getBookingItemList(pageNum, pageSize);
    }

    @PostMapping("/books/add")
    @RequirePriority({"admin"})
    public KernelResp<org.example.core.model.BookingItem> add(@RequestBody BookingAddRequest request) {
        return bookingTransactionService.addNewBookingItem(request.getTitle());
    }

    @PostMapping("/books/remove")
    @RequirePriority({"admin"})
    public KernelResp<Void> remove(@RequestBody BookingItemIdRequest request) {
        if (request.getItemId() == null) {
            return KernelResp.error(-1, "itemId 不能为空");
        }
        return bookingTransactionService.removeBookingItem(request.getItemId());
    }

    @PostMapping("/books/activate")
    @RequirePriority({"admin"})
    public KernelResp<Void> activate(@RequestBody BookingItemIdRequest request) {
        if (request.getItemId() == null) {
            return KernelResp.error(-1, "itemId 不能为空");
        }
        return bookingTransactionService.activateBookingItem(request.getItemId());
    }

    @PostMapping("/books/deactivate")
    @RequirePriority({"admin"})
    public KernelResp<Void> deactivate(@RequestBody BookingItemIdRequest request) {
        if (request.getItemId() == null) {
            return KernelResp.error(-1, "itemId 不能为空");
        }
        return bookingTransactionService.deactivateBookingItem(request.getItemId());
    }

    private List<org.example.core.model.BookingItem> toItems(List<Long> itemIds) {
        if (itemIds == null) {
            return List.of();
        }
        return itemIds.stream()
                .map(id -> {
                    org.example.core.model.BookingItem item = new org.example.core.model.BookingItem();
                    item.setItemId(id);
                    return item;
                })
                .toList();
    }
}
