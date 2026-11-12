package model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Collections;

public class Event {
    private int id;
    private String home;
    private String away;
    private String competition;
    private long startsAt;
    private EventStatus status;
    private Map<String, Long> zonePrices = new LinkedHashMap<String, Long>();

    public Event(int id, String home, String away, String competition, long startsAt, EventStatus status) {
        this.id = id;
        this.home = home;
        this.away = away;
        this.competition = competition;
        this.startsAt = startsAt;
        this.status = status;
    }

    public String title() { return this.home + " vs " + this.away; }

    public int getId() { return this.id; }
    public String getHome() { return this.home; }
    public void setHome(String value) { this.home = value; }
    public String getAway() { return this.away; }
    public void setAway(String value) { this.away = value; }
    public String getCompetition() { return this.competition; }
    public void setCompetition(String value) { this.competition = value; }
    public long getStartsAt() { return this.startsAt; }
    public void setStartsAt(long value) { this.startsAt = value; }
    public EventStatus getStatus() { return this.status; }
    public void setStatus(EventStatus value) { this.status = value; }
    public Map<String, Long> getZonePrices() { return Collections.unmodifiableMap(this.zonePrices); }
    public void putZonePrice(String code, long price) { this.zonePrices.put(code, price); }
}
