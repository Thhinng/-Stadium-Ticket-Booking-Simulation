package com.stadium.model;

import java.io.Serializable;

/**
 * Basic Seat Model representing stadium seating structure and status.
 */
public class Seat implements Serializable {
    private static final long serialVersionUID = 1L;

    private String seatId;
    private String matchId;
    private String zone;      // SVIP, VIP, Standard
    private String rowName;   // A, B, C...
    private int seatNumber;   // 1, 2, 3...
    private double price;
    private TicketStatus status;
    private String lockedByUserId;

    public Seat(String seatId, String matchId, String zone, String rowName, int seatNumber, double price) {
        this.seatId = seatId;
        this.matchId = matchId;
        this.zone = zone;
        this.rowName = rowName;
        this.seatNumber = seatNumber;
        this.price = price;
        this.status = TicketStatus.AVAILABLE;
    }

    public String getSeatName() {
        return "Khán đài " + zone + " - Hàng " + rowName + " - Ghế " + seatNumber;
    }

    public String getSeatId() { return seatId; }
    public String getMatchId() { return matchId; }
    public String getZone() { return zone; }
    public String getRowName() { return rowName; }
    public int getSeatNumber() { return seatNumber; }
    public double getPrice() { return price; }
    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }
    public String getLockedByUserId() { return lockedByUserId; }
    public void setLockedByUserId(String lockedByUserId) { this.lockedByUserId = lockedByUserId; }
}
