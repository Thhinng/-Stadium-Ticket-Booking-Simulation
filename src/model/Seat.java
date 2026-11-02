/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

public class Seat extends BaseEntity {
    private String sectionId;
    private String matchId;
    private String seatNumber;
    private String status;
    private int version;

    public Seat() {
    }

    public Seat(String id, String sectionId, String matchId, String seatNumber, String status, int version) {
        super(id);
        this.sectionId = sectionId;
        this.matchId = matchId;
        this.seatNumber = seatNumber;
        this.status = status;
        this.version = version;
    }

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
    
}
