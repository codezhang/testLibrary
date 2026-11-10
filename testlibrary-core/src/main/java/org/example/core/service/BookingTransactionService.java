package org.example.core.service;

import org.example.core.KernelResp;
import org.example.core.auth.annotation.RequireLogin;
import org.example.core.auth.annotation.RequirePriority;
import org.example.core.auth.exception.NotLoggedInException;
import org.example.core.auth.model.SessionUser;
import org.example.core.auth.port.SessionService;
import org.example.core.concurrent.LockHandle;
import org.example.core.concurrent.LockService;
import org.example.core.mapper.BookingTransactionMapper;
import org.example.core.model.BookingItem;
import org.example.core.page.PageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequireLogin
public class BookingTransactionService {

    private static final int CODE_OK = 0;
    private static final int CODE_BUSINESS_ERROR = -1;

    private final LockService lockService;
    private final BookingTransactionMapper bookingTransactionMapper;
    private final PageHelper pageHelper;
    private final SessionService sessionService;

    public BookingTransactionService(LockService lockService,
                                     BookingTransactionMapper bookingTransactionMapper,
                                     PageHelper pageHelper,
                                     SessionService sessionService) {
        this.lockService = lockService;
        this.bookingTransactionMapper = bookingTransactionMapper;
        this.pageHelper = pageHelper;
        this.sessionService = sessionService;
    }

    public KernelResp<Void> requireBookingResource(List<BookingItem> bookingItemList) {
        SessionUser user = currentUser();
        if (bookingItemList == null || bookingItemList.isEmpty()) {
            return KernelResp.error(CODE_BUSINESS_ERROR, "预约条目不能为空");
        }

        LockHandle lock = lockService.acquire();
        try {
            int failed = bookingTransactionMapper.markLent(bookingItemList, user.getUserId());
            if (failed != 0) {
                return KernelResp.error(CODE_BUSINESS_ERROR, "资源不足，失败条目数: " + failed);
            }
        } finally {
            lockService.release(lock);
        }
        return KernelResp.ok(null);
    }

    public KernelResp<Void> releaseBookingResource(List<BookingItem> bookingItemList) {
        SessionUser user = currentUser();
        if (bookingItemList == null || bookingItemList.isEmpty()) {
            return KernelResp.error(CODE_BUSINESS_ERROR, "归还条目不能为空");
        }

        LockHandle lock = lockService.acquire();
        try {
            int failed = bookingTransactionMapper.unmarkLent(bookingItemList, user.getUserId());
            if (failed != 0) {
                return KernelResp.error(CODE_BUSINESS_ERROR, "含有未知条目或不属于当前用户: " + failed);
            }
        } finally {
            lockService.release(lock);
        }
        return KernelResp.ok(null);
    }

    public KernelResp<List<BookingItem>> getUserBorrowed() {
        SessionUser user = currentUser();
        return KernelResp.ok(bookingTransactionMapper.getMarkedByUser(user.getUserId()));
    }

    public KernelResp<List<BookingItem>> getBookingItemList(int pageNum, int pageSize) {
        try {
            List<BookingItem> page = pageHelper.page(
                    bookingTransactionMapper.getAll(), pageNum, pageSize);
            return new KernelResp<>(CODE_OK, "", page);
        } catch (IllegalArgumentException e) {
            return KernelResp.error(CODE_BUSINESS_ERROR, e.getMessage());
        }
    }

    @RequirePriority({"admin"})
    public KernelResp<BookingItem> addNewBookingItem(String title) {
        if (title == null || title.isBlank()) {
            return KernelResp.error(CODE_BUSINESS_ERROR, "条目标题不能为空");
        }

        LockHandle lock = lockService.acquire();
        try {
            return KernelResp.ok(bookingTransactionMapper.addNew(title.trim()));
        } finally {
            lockService.release(lock);
        }
    }

    @RequirePriority({"admin"})
    public KernelResp<Void> deactivateBookingItem(long itemId) {
        LockHandle lock = lockService.acquire();
        try {
            if (!bookingTransactionMapper.deactivate(itemId)) {
                return KernelResp.error(CODE_BUSINESS_ERROR, "条目不存在、已停用或正在借出");
            }
        } finally {
            lockService.release(lock);
        }
        return KernelResp.ok(null);
    }

    @RequirePriority({"admin"})
    public KernelResp<Void> activateBookingItem(long itemId) {
        LockHandle lock = lockService.acquire();
        try {
            if (!bookingTransactionMapper.activate(itemId)) {
                return KernelResp.error(CODE_BUSINESS_ERROR, "条目不存在或未被停用");
            }
        } finally {
            lockService.release(lock);
        }
        return KernelResp.ok(null);
    }

    @RequirePriority({"admin"})
    public KernelResp<Void> removeBookingItem(long itemId) {
        LockHandle lock = lockService.acquire();
        try {
            if (!bookingTransactionMapper.remove(itemId)) {
                return KernelResp.error(CODE_BUSINESS_ERROR, "条目不存在或正在借出");
            }
        } finally {
            lockService.release(lock);
        }
        return KernelResp.ok(null);
    }

    private SessionUser currentUser() {
        SessionUser user = sessionService.currentUser();
        if (user == null) {
            throw new NotLoggedInException("未登录或会话已失效");
        }
        return user;
    }
}
