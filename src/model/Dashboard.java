package model;

public class Dashboard {
    private int totalFans;
    private int activeFans;
    private int totalEvents;
    private int openEvents;
    private int paidOrders;
    private int refundedOrders;
    private int activeHolds;
    private long netRevenue;

    public int getTotalFans() { return this.totalFans; }
    public void setTotalFans(int value) { this.totalFans = value; }
    public int getActiveFans() { return this.activeFans; }
    public void setActiveFans(int value) { this.activeFans = value; }
    public int getTotalEvents() { return this.totalEvents; }
    public void setTotalEvents(int value) { this.totalEvents = value; }
    public int getOpenEvents() { return this.openEvents; }
    public void setOpenEvents(int value) { this.openEvents = value; }
    public int getPaidOrders() { return this.paidOrders; }
    public void setPaidOrders(int value) { this.paidOrders = value; }
    public int getRefundedOrders() { return this.refundedOrders; }
    public void setRefundedOrders(int value) { this.refundedOrders = value; }
    public int getActiveHolds() { return this.activeHolds; }
    public void setActiveHolds(int value) { this.activeHolds = value; }
    public long getNetRevenue() { return this.netRevenue; }
    public void setNetRevenue(long value) { this.netRevenue = value; }
}
