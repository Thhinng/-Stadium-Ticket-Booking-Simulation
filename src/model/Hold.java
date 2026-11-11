package model;

import java.util.ArrayList;
import java.util.List;

public class Hold {
    public int id;
    public int userId;
    public int eventId;
    public List<String> seatIds;
    public long total;
    public long createdAt;
    public long expiresAt;

    public Hold(int id, int userId, int eventId, List<String> seatIds, long total, long createdAt, long expiresAt) {
        this.id = id;
        this.userId = userId;
        this.eventId = eventId;
        this.seatIds = new ArrayList<String>(seatIds);
        this.total = total;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }
}
