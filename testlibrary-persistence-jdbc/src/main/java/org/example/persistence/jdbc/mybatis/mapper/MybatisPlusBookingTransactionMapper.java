package org.example.persistence.jdbc.mybatis.mapper;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.core.mapper.BookingTransactionMapper;
import org.example.core.model.BookingItem;
import org.example.core.model.BookingStatus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mapper
public interface MybatisPlusBookingTransactionMapper extends BookingTransactionMapper {

    int updateLent(@Param("ids") List<Long> ids,
                   @Param("userId") String userId,
                   @Param("count") int count);

    int updateAvailable(@Param("ids") List<Long> ids,
                        @Param("userId") String userId,
                        @Param("count") int count);

    List<BookingItem> getMarkedByUser(@Param("userId") String userId);

    List<BookingItem> getAll();

    int insertItem(@Param("itemId") long itemId,
                   @Param("title") String title,
                   @Param("previewContent") String previewContent);

    boolean deactivate(@Param("itemId") long itemId);

    boolean activate(@Param("itemId") long itemId);

    boolean remove(@Param("itemId") long itemId);

    @Override
    default int markLent(List<BookingItem> items, String userId) {
        BatchIds batch = collectIds(items);
        if (batch.failed > 0 || batch.ids.isEmpty()) {
            return batch.failed;
        }
        int updated = updateLent(batch.ids, userId, batch.ids.size());
        return updated == batch.ids.size() ? 0 : batch.ids.size();
    }

    @Override
    default int unmarkLent(List<BookingItem> items, String userId) {
        BatchIds batch = collectIds(items);
        if (batch.failed > 0 || batch.ids.isEmpty()) {
            return batch.failed;
        }
        int updated = updateAvailable(batch.ids, userId, batch.ids.size());
        return updated == batch.ids.size() ? 0 : batch.ids.size();
    }

    @Override
    default BookingItem addNew(String title) {
        long itemId = IdWorker.getId();
        String previewContent = defaultPreview(title);
        insertItem(itemId, title, previewContent);
        BookingItem item = new BookingItem(itemId, title);
        item.setPreviewContent(previewContent);
        item.setStatus(BookingStatus.AVAILABLE);
        return item;
    }

    private static String defaultPreview(String title) {
        return "《" + title + "》暂无预览内容。";
    }

    private static BatchIds collectIds(List<BookingItem> items) {
        List<Long> ids = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        int failed = 0;
        if (items != null) {
            for (BookingItem item : items) {
                if (item == null || item.getItemId() == null || !seen.add(item.getItemId())) {
                    failed++;
                } else {
                    ids.add(item.getItemId());
                }
            }
        }
        return new BatchIds(ids, failed);
    }

    final class BatchIds {
        private final List<Long> ids;
        private final int failed;

        private BatchIds(List<Long> ids, int failed) {
            this.ids = ids;
            this.failed = failed;
        }
    }
}
