package model;

public class PaymentRecord {
    private int id;
    private int userId;
    private int orderId;  // 0 means unsuccessful attempt without an order
    private String type;  // PAYMENT or REFUND
    private String method;
    private String result; // SUCCESS or FAILED
    private long amount;
    private String reference;
    private long createdAt;

    public PaymentRecord(int id, int userId, int orderId, String type, String method, String result,
                         long amount, String reference, long createdAt) {
        this.id = id;
        this.userId = userId;
        this.orderId = orderId;
        this.type = type;
        this.method = method;
        this.result = result;
        this.amount = amount;
        this.reference = reference;
        this.createdAt = createdAt;
    }

    public int getId() { return this.id; }
    public int getUserId() { return this.userId; }
    public int getOrderId() { return this.orderId; }
    public String getType() { return this.type; }
    public String getMethod() { return this.method; }
    public String getResult() { return this.result; }
    public long getAmount() { return this.amount; }
    public String getReference() { return this.reference; }
    public long getCreatedAt() { return this.createdAt; }
}
