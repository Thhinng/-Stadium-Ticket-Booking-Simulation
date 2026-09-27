package com.stadium.model;

/**
 * Encapsulates the lifecycle status of a Booking transaction.
 * Member 3: Lê Bách - Booking & Payment Module
 */
public enum BookingStatus {
    PENDING_PAYMENT("Chờ thanh toán"),
    PAID("Đã thanh toán thành công"),
    EXPIRED("Hết hạn giữ chỗ"),
    CANCELLED("Đã hủy bởi người dùng"),
    REFUNDED("Đã trả vé & hoàn tiền");

    private final String description;

    BookingStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
