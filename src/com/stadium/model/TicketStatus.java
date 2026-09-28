package com.stadium.model;

/**
 * Encapsulates the lifecycle status of an individual Ticket in the Stadium Booking System.
 * Member 3: Lê Bách - Booking & Payment Module
 */
public enum TicketStatus {
    AVAILABLE("Còn trống"),
    HOLD("Đang giữ chỗ (Chờ thanh toán)"),
    PAID("Đã thanh toán"),
    CANCELLED("Đã hủy"),
    REFUNDED("Đã trả vé (Hoàn tiền)");

    private final String description;

    TicketStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
