package model;

public class Section {
    private String code;
    private String name;
    private int capacity;
    private long basePrice;
    private boolean active;

    public Section(String code, String name, int capacity, long basePrice, boolean active) {
        this.code = code;
        this.name = name;
        this.capacity = capacity;
        this.basePrice = basePrice;
        this.active = active;
    }

    public String getCode() { return this.code; }
    public String getName() { return this.name; }
    public void setName(String value) { this.name = value; }
    public int getCapacity() { return this.capacity; }
    public void setCapacity(int value) { this.capacity = value; }
    public long getBasePrice() { return this.basePrice; }
    public void setBasePrice(long value) { this.basePrice = value; }
    public boolean getActive() { return this.active; }
    public void setActive(boolean value) { this.active = value; }
}
