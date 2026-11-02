/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

public class SimulationResult extends BaseEntity {
    private String mechanism;
    private String executedAt;
    private String result;
    
    private int totalRequests;
    private int successCount;
    private int failedCount;
    private int doubleBookingCount;
    private long durationMs;
    private double tps;

    public SimulationResult() {
    }

    public SimulationResult(String id, String mechanism, String executedAt, String result) {
        super(id);
        this.mechanism = mechanism;
        this.executedAt = executedAt;
        this.result = result;
    }

    public SimulationResult(String id, String mechanism, String executedAt, int totalRequests,
                            int successCount, int failedCount, int doubleBookingCount,
                            long durationMs, double tps, String result) {
        super(id);
        this.mechanism = mechanism;
        this.executedAt = executedAt;
        this.totalRequests = totalRequests;
        this.successCount = successCount;
        this.failedCount = failedCount;
        this.doubleBookingCount = doubleBookingCount;
        this.durationMs = durationMs;
        this.tps = tps;
        this.result = result;
    }

    public String getMechanism() {
        return mechanism;
    }

    public void setMechanism(String mechanism) {
        this.mechanism = mechanism;
    }

    public String getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(String executedAt) {
        this.executedAt = executedAt;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public int getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(int totalRequests) {
        this.totalRequests = totalRequests;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }

    public int getDoubleBookingCount() {
        return doubleBookingCount;
    }

    public void setDoubleBookingCount(int doubleBookingCount) {
        this.doubleBookingCount = doubleBookingCount;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public double getTps() {
        return tps;
    }

    public void setTps(double tps) {
        this.tps = tps;
    }

    @Override
    public String toString() {
        return String.format(
            "=== SIMULATION RESULT [%s] ===\n" +
            "  - Mechanism:           %s\n" +
            "  - Execution Time:      %s\n" +
            "  - Total Requests:      %d\n" +
            "  - Successful Bookings: %d\n" +
            "  - Failed / Conflicted: %d\n" +
            "  - Double Booked Seats: %d\n" +
            "  - Elapsed Time:        %d ms\n" +
            "  - Throughput (TPS):    %.2f req/s\n" +
            "  - Summary:             %s\n" +
            "==================================",
            getId(), mechanism, executedAt, totalRequests, successCount,
            failedCount, doubleBookingCount, durationMs, tps, result
        );
    }
}
