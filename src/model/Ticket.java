/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

public class Ticket extends BaseEntity {
    private String fanId;
    private String matchId;
    private String seatId;
    private String status;
    private double price;

    public Ticket() {
    }

    public Ticket(String id, String fanId, String matchId, String seatId, String status, double price) {
        super(id);
        this.fanId = fanId;
        this.matchId = matchId;
        this.seatId = seatId;
        this.status = status;
        this.price = price;
    }

    public String getFanId() {
        return fanId;
    }

    public void setFanId(String fanId) {
        this.fanId = fanId;
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public String getSeatId() {
        return seatId;
    }

    public void setSeatId(String seatId) {
        this.seatId = seatId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
    
    
}
