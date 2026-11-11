package model;

public class PaymentRecord {
    public int id;
    public int userId;
    public int orderId;  // 0 means unsuccessful attempt without an order
    public String type;  // PAYMENT or REFUND
    public String method;
    public String result; // SUCCESS or FAILED
    public long amount;
    public String reference;
    public long createdAt;

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
}
