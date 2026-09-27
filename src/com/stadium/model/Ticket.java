package com.stadium.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Class Ticket (Quản lý trạng thái giữ ghế, mã vé, giá tiền, thời gian thanh toán, trạng thái vé).
 * Member 3: Lê Bách - Booking & Payment Module
 */
public class Ticket implements Serializable {
    private static final long serialVersionUID = 1L;

    private String ticketId;
    private String bookingId;
    private String matchId;
    private String matchName;
    private String seatId;
    private String seatName;
    private String zone;
    private double price;
    private TicketStatus status;
    private String userId;
    private LocalDateTime purchaseTime;
    private String qrCodeData;

    public Ticket() {
        this.status = TicketStatus.AVAILABLE;
    }

    public Ticket(String ticketId, String matchId, String matchName, String seatId, String seatName, String zone, double price) {
        this.ticketId = ticketId;
        this.matchId = matchId;
        this.matchName = matchName;
        this.seatId = seatId;
        this.seatName = seatName;
        this.zone = zone;
        this.price = price;
        this.status = TicketStatus.AVAILABLE;
    }

    // Getters and Setters
    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
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

    public String getSeatId() {
        return seatId;
    }

    public void setSeatId(String seatId) {
        this.seatId = seatId;
    }

    public String getSeatName() {
        return seatName;
    }

    public void setSeatName(String seatName) {
        this.seatName = seatName;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getPurchaseTime() {
        return purchaseTime;
    }

    public void setPurchaseTime(LocalDateTime purchaseTime) {
        this.purchaseTime = purchaseTime;
    }

    public String getQrCodeData() {
        return qrCodeData;
    }

    public void setQrCodeData(String qrCodeData) {
        this.qrCodeData = qrCodeData;
    }

    public String getFormattedPurchaseTime() {
        if (purchaseTime == null) return "N/A";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
        return purchaseTime.format(formatter);
    }

    @Override
    public String toString() {
        return String.format("Vé [%s] | Trận: %s | Ghế: %s (%s) | Giá: %,.0f VNĐ | Trạng thái: %s",
                ticketId, matchName, seatName, zone, price, status.getDescription());
    }
}
