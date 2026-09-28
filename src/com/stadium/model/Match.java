package com.stadium.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Basic Match Model representing stadium events/matches.
 */
public class Match implements Serializable {
    private static final long serialVersionUID = 1L;

    private String matchId;
    private String title;
    private String stadiumName;
    private LocalDateTime matchTime;

    public Match(String matchId, String title, String stadiumName, LocalDateTime matchTime) {
        this.matchId = matchId;
        this.title = title;
        this.stadiumName = stadiumName;
        this.matchTime = matchTime;
    }

    public String getMatchId() { return matchId; }
    public String getTitle() { return title; }
    public String getStadiumName() { return stadiumName; }
    public LocalDateTime getMatchTime() { return matchTime; }
}
