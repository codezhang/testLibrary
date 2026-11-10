package org.example.wrapper.persistence;

import org.example.core.mapper.BookingTransactionMapper;
import org.example.core.model.BookingItem;
import org.example.core.model.BookingStatus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryBookingTransactionMapper implements BookingTransactionMapper {

    private final Map<Long, BookingItem> items = new LinkedHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    public InMemoryBookingTransactionMapper() {
        seed("Java 编程思想",
                "本书从编程思想出发，系统讲解 Java 语言的核心概念、面向对象设计、集合、并发与 JVM 基础。适合希望打牢 Java 基础并理解语言设计哲学的读者。");
        seed("设计模式",
                "通过经典设计模式讲解如何组织可复用、可扩展的面向对象代码。书中结合大量示例说明创建型、结构型和行为型模式的适用场景，是提升代码设计能力的常备参考。");
        seed("重构：改善既有代码的设计",
                "围绕如何在不改变外部行为的前提下改善代码结构展开，介绍重构手法、代码坏味道与测试驱动的小步改进方式，适合有经验的开发者持续优化代码质量。");
    }

    private void seed(String title, String previewContent) {
        BookingItem item = addNew(title);
        item.setPreviewContent(previewContent);
    }

    @Override
    public synchronized int markLent(List<BookingItem> requests, String userId) {
        List<BookingItem> targets = new ArrayList<>();
        int failed = collectTargets(requests, targets, BookingItem::availableForBorrow, new HashSet<>());
        if (failed > 0) {
            return failed;
        }
        for (BookingItem item : targets) {
            item.setStatus(BookingStatus.LENT);
            item.setBorrowedBy(userId);
        }
        return 0;
    }

    @Override
    public synchronized int unmarkLent(List<BookingItem> requests, String userId) {
        List<BookingItem> targets = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        int failed = 0;
        for (BookingItem request : safe(requests)) {
            Long itemId = request.getItemId();
            if (itemId == null || !seen.add(itemId)) {
                failed++;
                continue;
            }
            BookingItem item = items.get(itemId);
            if (item == null
                    || item.getStatus() != BookingStatus.LENT
                    || !userId.equals(item.getBorrowedBy())) {
                failed++;
                continue;
            }
            targets.add(item);
        }
        if (failed > 0) {
            return failed;
        }
        for (BookingItem item : targets) {
            item.setStatus(BookingStatus.AVAILABLE);
            item.setBorrowedBy(null);
        }
        return 0;
    }

    @Override
    public synchronized List<BookingItem> getMarkedByUser(String userId) {
        return items.values().stream()
                .filter(item -> item.getStatus() == BookingStatus.LENT)
                .filter(item -> userId.equals(item.getBorrowedBy()))
                .sorted(Comparator.comparing(BookingItem::getItemId))
                .toList();
    }

    @Override
    public synchronized List<BookingItem> getAll() {
        return items.values().stream()
                .sorted(Comparator.comparing(BookingItem::getItemId))
                .toList();
    }

    @Override
    public synchronized BookingItem addNew(String title) {
        long itemId = sequence.getAndIncrement();
        BookingItem item = new BookingItem(itemId, title);
        item.setPreviewContent("《" + title + "》暂无预览内容。");
        item.setStatus(BookingStatus.AVAILABLE);
        items.put(itemId, item);
        return item;
    }

    @Override
    public synchronized boolean deactivate(long itemId) {
        BookingItem item = items.get(itemId);
        if (item == null || item.getStatus() == BookingStatus.LENT
                || item.getStatus() == BookingStatus.INACTIVE) {
            return false;
        }
        item.setStatus(BookingStatus.INACTIVE);
        return true;
    }

    @Override
    public synchronized boolean activate(long itemId) {
        BookingItem item = items.get(itemId);
        if (item == null || item.getStatus() != BookingStatus.INACTIVE) {
            return false;
        }
        item.setStatus(BookingStatus.AVAILABLE);
        return true;
    }

    @Override
    public synchronized boolean remove(long itemId) {
        BookingItem item = items.get(itemId);
        if (item == null || item.getStatus() == BookingStatus.LENT) {
            return false;
        }
        items.remove(itemId);
        return true;
    }

    private int collectTargets(List<BookingItem> requests,
                               List<BookingItem> targets,
                               java.util.function.Predicate<BookingItem> allowed,
                               Set<Long> seen) {
        int failed = 0;
        for (BookingItem request : safe(requests)) {
            Long itemId = request.getItemId();
            if (itemId == null || !seen.add(itemId)) {
                failed++;
                continue;
            }
            BookingItem item = items.get(itemId);
            if (item == null || !allowed.test(item)) {
                failed++;
                continue;
            }
            targets.add(item);
        }
        return failed;
    }

    private static List<BookingItem> safe(List<BookingItem> requests) {
        return requests == null ? List.of() : requests;
    }
}
