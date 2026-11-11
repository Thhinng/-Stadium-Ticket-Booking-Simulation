package model;

import java.util.ArrayList;
import java.util.List;

public class Database {
    public List<User> users = new ArrayList<User>();
    public List<Section> sections = new ArrayList<Section>();
    public List<Event> events = new ArrayList<Event>();
    public List<Hold> holds = new ArrayList<Hold>();
    public List<Order> orders = new ArrayList<Order>();
    public List<PaymentRecord> payments = new ArrayList<PaymentRecord>();
    public int nextUserId = 1;
    public int nextEventId = 1;
    public int nextHoldId = 1;
    public int nextOrderId = 1;
    public int nextPaymentId = 1;
}
