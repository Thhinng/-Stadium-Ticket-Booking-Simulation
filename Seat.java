package model;

public class Seat {
    private String seatId;
    private String section;
    private String row;
    private String number;
    private SeatStatus status;

    public Seat(String seatId, String section, String row, String number, SeatStatus status) {
        this.seatId = seatId;
        this.section = section;
        this.row = row;
        this.number = number;
        this.status = status;
    }

    public boolean isAvailable() {
        return this.status == SeatStatus.AVAILABLE;
    }
    public void lock() {
        if (this.status == SeatStatus.AVAILABLE) {
            this.status = SeatStatus.LOCKED;
        }
    }
    public void unlock() {
        if (this.status == SeatStatus.LOCKED) {
            this.status = SeatStatus.AVAILABLE;
        }
    }
    public void setStatus(SeatStatus status) {
        this.status = status;
    }
    public String getSeatId() { return seatId;}
    public String getSection() { return section;}
    public String getRow() { return row;}
    public String getNumber() { return number;}
    public SeatStatus getStatus() { return status;}

}