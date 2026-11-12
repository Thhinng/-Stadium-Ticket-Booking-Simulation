package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class Hold {
    private int id;
    private int userId;
    private int eventId;
    private List<String> seatIds;
    private long total;
    private long createdAt;
    private long expiresAt;

    public Hold(int id, int userId, int eventId, List<String> seatIds, long total, long createdAt, long expiresAt) {
        this.id = id;
        this.userId = userId;
        this.eventId = eventId;
        this.seatIds = new ArrayList<String>(seatIds);
        this.total = total;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public int getId() { return this.id; }
    public int getUserId() { return this.userId; }
    public int getEventId() { return this.eventId; }
    public List<String> getSeatIds() { return Collections.unmodifiableList(this.seatIds); }
    public long getTotal() { return this.total; }
    public long getCreatedAt() { return this.createdAt; }
    public long getExpiresAt() { return this.expiresAt; }
}
