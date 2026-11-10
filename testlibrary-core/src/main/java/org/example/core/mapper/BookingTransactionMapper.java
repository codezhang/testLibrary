package org.example.core.mapper;

import org.example.core.model.BookingItem;

import java.util.List;

public interface BookingTransactionMapper {

    int markLent(List<BookingItem> items, String userId);

    int unmarkLent(List<BookingItem> items, String userId);

    List<BookingItem> getMarkedByUser(String userId);

    List<BookingItem> getAll();

    BookingItem addNew(String title);

    boolean deactivate(long itemId);

    boolean activate(long itemId);

    boolean remove(long itemId);
}
