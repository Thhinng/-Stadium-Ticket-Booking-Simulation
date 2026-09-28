package model;

public class TicketPriceConfig {
    private String configId;
    private String matchId;
    private String section;
    private double price;

    public TicketPriceConfig(String configId, String matchId, String section, double price) {
        this.configId = configId;
        this.matchId = matchId;
        this.section = section;
        this.price = price;
    }

    public void updatePrice(double newPrice) {
        this.price = newPrice;
    }

    public String getConfigId() {
        return configId;

    }

    public String getMatchId() {
        return matchId;
    }

    public String getSection() {
        return section;
    }

    public double getPrice() {
        return price;
    }
}