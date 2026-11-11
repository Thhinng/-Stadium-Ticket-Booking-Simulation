package model;

public class Section {
    public String code;
    public String name;
    public int capacity;
    public long basePrice;
    public boolean active;

    public Section(String code, String name, int capacity, long basePrice, boolean active) {
        this.code = code;
        this.name = name;
        this.capacity = capacity;
        this.basePrice = basePrice;
        this.active = active;
    }
}
