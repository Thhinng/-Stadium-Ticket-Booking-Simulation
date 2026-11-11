package model;

import java.util.ArrayList;
import java.util.List;

public class Order {
    public int id;
    public int userId;
    public int eventId;
    public List<String> seatIds;
    public long total;
    public long purchasedAt;
    public OrderStatus status;
    public String paymentMethod;
    public String paymentReference;

    public Order(int id, int userId, int eventId, List<String> seatIds, long total,
                 long purchasedAt, OrderStatus status, String paymentMethod, String paymentReference) {
        this.id = id;
        this.userId = userId;
        this.eventId = eventId;
        this.seatIds = new ArrayList<String>(seatIds);
        this.total = total;
        this.purchasedAt = purchasedAt;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.paymentReference = paymentReference;
    }
}
