package model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public class Match {
    private String matchId;
    private String homeTeam;
    private String awayTeam;
    private LocalDateTime matchDate;
    private MatchStatus status;
    private List<Seat> seats = new ArrayList<>();

    public Match(String matchId, String homeTeam, String awayTeam, LocalDateTime matchDate, MatchStatus status) {
        this.matchId = matchId;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.matchDate = matchDate;
        this.status = status;
    }

    public void updateMatch(String homeTeam, String awayTeam, LocalDateTime matchDate) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.matchDate = matchDate;
    }

    public void cancelMatch() {
        this.status = MatchStatus.CANCELLED;

    }

    public List<Seat> getAvailableSeats() {
        List<Seat> availableList = new ArrayList<>();
        for (Seat seat : seats) {
            if (seat.isAvailable()) {
                availableList.add(seat);
            }
        }
        return availableList;
    }

    public String getMatchId() {
        return matchId;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public String getAwayTeam() {
        return awayTeam;
    }

    public LocalDateTime getMatchDate() {
        return matchDate;

    }

    public MatchStatus getStatus() {
        return status;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
    }
}
