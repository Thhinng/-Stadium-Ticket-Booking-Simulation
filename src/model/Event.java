package model;

import java.util.LinkedHashMap;
import java.util.Map;

public class Event {
    public int id;
    public String home;
    public String away;
    public String competition;
    public long startsAt;
    public EventStatus status;
    public Map<String, Long> zonePrices = new LinkedHashMap<String, Long>();

    public Event(int id, String home, String away, String competition, long startsAt, EventStatus status) {
        this.id = id;
        this.home = home;
        this.away = away;
        this.competition = competition;
        this.startsAt = startsAt;
        this.status = status;
    }

    public String title() { return home + " vs " + away; }
}
