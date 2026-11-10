package org.example.core.model;

import java.io.Serializable;

public class BookingItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long itemId;
    private String title;
    private String previewContent;
    private BookingStatus status = BookingStatus.AVAILABLE;
    private String borrowedBy;

    public BookingItem() {
    }

    public BookingItem(Long itemId, String title) {
        this.itemId = itemId;
        this.title = title;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPreviewContent() {
        return previewContent;
    }

    public void setPreviewContent(String previewContent) {
        this.previewContent = previewContent;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public String getBorrowedBy() {
        return borrowedBy;
    }

    public void setBorrowedBy(String borrowedBy) {
        this.borrowedBy = borrowedBy;
    }

    public boolean availableForBorrow() {
        return status == BookingStatus.AVAILABLE;
    }

    @Override
    public String toString() {
        return "BookingItem{"
                + "itemId=" + itemId
                + ", title='" + title + '\''
                + ", previewLength=" + (previewContent == null ? 0 : previewContent.length())
                + ", status=" + status
                + ", borrowedBy='" + borrowedBy + '\''
                + '}';
    }
}
