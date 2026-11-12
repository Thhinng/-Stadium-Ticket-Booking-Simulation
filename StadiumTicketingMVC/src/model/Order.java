package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class Order {
    private int id;
    private int userId;
    private int eventId;
    private List<String> seatIds;
    private long total;
    private long purchasedAt;
    private OrderStatus status;
    private String paymentMethod;
    private String paymentReference;

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

    public int getId() { return this.id; }
    public int getUserId() { return this.userId; }
    public int getEventId() { return this.eventId; }
    public List<String> getSeatIds() { return Collections.unmodifiableList(this.seatIds); }
    public long getTotal() { return this.total; }
    public long getPurchasedAt() { return this.purchasedAt; }
    public OrderStatus getStatus() { return this.status; }
    public void setStatus(OrderStatus value) { this.status = value; }
    public String getPaymentMethod() { return this.paymentMethod; }
    public String getPaymentReference() { return this.paymentReference; }
}
