/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

public class Match extends BaseEntity{
    private String homeTeam;
    private String awayTeam;
    private String stadiumId;
    private String matchDate;
    private String status;

    public Match() {
    }
    
    public Match(String id, String homeTeam, String awayTeam, String stadiumId, String matchDate, String status) {
        super(id);
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.stadiumId = stadiumId;
        this.matchDate = matchDate;
        this.status = status;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(String homeTeam) {
        this.homeTeam = homeTeam;
    }

    public String getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(String awayTeam) {
        this.awayTeam = awayTeam;
    }

    public String getStadiumId() {
        return stadiumId;
    }

    public void setStadiumId(String stadiumId) {
        this.stadiumId = stadiumId;
    }

    public String getMatchDate() {
        return matchDate;
    }

    public void setMatchDate(String matchDate) {
        this.matchDate = matchDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    
}
