package model;

import java.util.ArrayList;
import java.util.List;

public class Database {
    private List<User> users = new ArrayList<User>();
    private List<Section> sections = new ArrayList<Section>();
    private List<Event> events = new ArrayList<Event>();
    private List<Hold> holds = new ArrayList<Hold>();
    private List<Order> orders = new ArrayList<Order>();
    private List<PaymentRecord> payments = new ArrayList<PaymentRecord>();
    private int nextUserId = 1;
    private int nextEventId = 1;
    private int nextHoldId = 1;
    private int nextOrderId = 1;
    private int nextPaymentId = 1;

    public List<User> getUsers() { return this.users; }
    public List<Section> getSections() { return this.sections; }
    public List<Event> getEvents() { return this.events; }
    public List<Hold> getHolds() { return this.holds; }
    public List<Order> getOrders() { return this.orders; }
    public List<PaymentRecord> getPayments() { return this.payments; }
    public int getNextUserId() { return this.nextUserId; }
    public void setNextUserId(int value) { this.nextUserId = value; }
    public int getNextEventId() { return this.nextEventId; }
    public void setNextEventId(int value) { this.nextEventId = value; }
    public int getNextHoldId() { return this.nextHoldId; }
    public void setNextHoldId(int value) { this.nextHoldId = value; }
    public int getNextOrderId() { return this.nextOrderId; }
    public void setNextOrderId(int value) { this.nextOrderId = value; }
    public int getNextPaymentId() { return this.nextPaymentId; }
    public void setNextPaymentId(int value) { this.nextPaymentId = value; }
    public int takeNextUserId() { return this.nextUserId++; }
    public int takeNextEventId() { return this.nextEventId++; }
    public int takeNextHoldId() { return this.nextHoldId++; }
    public int takeNextOrderId() { return this.nextOrderId++; }
    public int takeNextPaymentId() { return this.nextPaymentId++; }
}
