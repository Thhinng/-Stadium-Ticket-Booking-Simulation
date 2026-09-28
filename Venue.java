package model;

public class Venue {
    private String venueId;
    private String name;
    private String address;
    private int capacity;

    public Venue(String venueId, String name, String address, int capacity) {
        this.venueId = venueId;
        this.name = name;
        this.address = address;
        this.capacity = capacity;
    }

    public String getInfo() {
        return "Sân" + name + "| Địa chỉ: " + address + "| Sức chứa: " + capacity + " người";
    }

    public void updateInfo(String name, String address, int capacity) {
        this.name = name;
        this.address = address;
        this.capacity = capacity;
    }

    public String getVenueId() {
        return venueId;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public int getCapacity() {
        return capacity;
    }

}
