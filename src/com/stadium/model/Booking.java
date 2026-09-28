package com.stadium.model;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Class Booking (Quản lý luồng đặt vé, danh sách vé, thời gian giữ chỗ TTL, thanh toán, trạng thái).
 * Member 3: Lê Bách - Booking & Payment Module
 */
public class Booking implements Serializable {
    private static final long serialVersionUID = 1L;

    private String bookingId;
    private String userId;
    private String matchId;
    private String matchName;
    private List<Ticket> tickets;
    private double totalAmount;
    private LocalDateTime bookingTime;
    private LocalDateTime expirationTime; // TTL timestamp (giữ ghế)
    private LocalDateTime paymentTime;
    private BookingStatus status;
    private String paymentMethod;
    private String transactionId;

    public Booking() {
        this.tickets = new ArrayList<>();
        this.status = BookingStatus.PENDING_PAYMENT;
        this.bookingTime = LocalDateTime.now();
    }

    public Booking(String bookingId, String userId, String matchId, String matchName, int holdDurationMinutes) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.matchId = matchId;
        this.matchName = matchName;
        this.tickets = new ArrayList<>();
        this.status = BookingStatus.PENDING_PAYMENT;
        this.bookingTime = LocalDateTime.now();
        this.expirationTime = this.bookingTime.plusMinutes(holdDurationMinutes);
    }

    public void addTicket(Ticket ticket) {
        if (ticket != null) {
            ticket.setBookingId(this.bookingId);
            ticket.setUserId(this.userId);
            this.tickets.add(ticket);
            recalculateTotalAmount();
        }
    }

    public void recalculateTotalAmount() {
        this.totalAmount = 0.0;
        for (Ticket t : tickets) {
            this.totalAmount += t.getPrice();
        }
    }

    public boolean isExpired() {
        if (status != BookingStatus.PENDING_PAYMENT) {
            return false;
        }
        return LocalDateTime.now().isAfter(expirationTime);
    }

    public long getRemainingSeconds() {
        if (status != BookingStatus.PENDING_PAYMENT || expirationTime == null) {
            return 0;
        }
        long seconds = Duration.between(LocalDateTime.now(), expirationTime).getSeconds();
        return Math.max(0, seconds);
    }

    public String getFormattedRemainingTime() {
        long totalSecs = getRemainingSeconds();
        long minutes = totalSecs / 60;
        long seconds = totalSecs % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    // Getters and Setters
    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public String getMatchName() {
        return matchName;
    }

    public void setMatchName(String matchName) {
        this.matchName = matchName;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
        recalculateTotalAmount();
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(LocalDateTime bookingTime) {
        this.bookingTime = bookingTime;
    }

    public LocalDateTime getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(LocalDateTime expirationTime) {
        this.expirationTime = expirationTime;
    }

    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(LocalDateTime paymentTime) {
        this.paymentTime = paymentTime;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getFormattedBookingTime() {
        if (bookingTime == null) return "N/A";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
        return bookingTime.format(formatter);
    }

    public String getFormattedPaymentTime() {
        if (paymentTime == null) return "N/A";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
        return paymentTime.format(formatter);
    }

    @Override
    public String toString() {
        return String.format("Đơn đặt vé [%s] | Trận: %s | Số vé: %d | Tổng tiền: %,.0f VNĐ | Trạng thái: %s",
                bookingId, matchName, tickets.size(), totalAmount, status.getDescription());
    }
}
