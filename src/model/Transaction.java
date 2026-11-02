/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;


public class Transaction extends BaseEntity {
    private String fanId;
    private String matchId;
    private String type;
    private double amount;
    private String timestamp;

    public Transaction() {
    }

    public Transaction(String id, String fanId, String matchId, String type, double amount, String timestamp) {
        super(id);
        this.fanId = fanId;
        this.matchId = matchId;
        this.type = type;
        this.amount = amount;
        this.timestamp = timestamp;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    
}
