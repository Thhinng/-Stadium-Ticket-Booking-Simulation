package app.view;

import java.util.Map;

/**
 * AnalyticsView — "Bảng biểu doanh thu & tỷ lệ lấp đầy" (Người 4).
 * Chỉ nhận dữ liệu thô (Map<String,Double>...), không phụ thuộc class của
 * thành viên khác. Hiển thị dạng bảng trên console.
 */
public class AnalyticsView {

    public void showTotalRevenue(double totalRevenue) {
        System.out.println();
        System.out.println("========== BÁO CÁO TỔNG DOANH THU ==========");
        System.out.printf("Tổng doanh thu: %,.0f đ%n", totalRevenue);
    }

    public void showRevenueByMatch(Map<String, Double> revenueByMatch) {
        System.out.println();
        System.out.println("========== DOANH THU THEO TỪNG TRẬN ĐẤU ==========");
        System.out.printf("%-12s | %15s%n", "Mã trận", "Doanh thu (đ)");
        System.out.println("-".repeat(32));
        double total = 0;
        for (Map.Entry<String, Double> e : revenueByMatch.entrySet()) {
            System.out.printf("%-12s | %,15.0f%n", e.getKey(), e.getValue());
            total += e.getValue();
        }
        System.out.println("-".repeat(32));
        System.out.printf("%-12s | %,15.0f%n", "TỔNG", total);
    }

    /**
     * @param totalSeatsByMatch    key = mã trận, value = tổng số ghế
     * @param occupancyByMatch     key = mã trận, value = tỷ lệ lấp đầy (%)
     */
    public void showOccupancyReport(Map<String, Integer> totalSeatsByMatch,
                                     Map<String, Double> occupancyByMatch) {
        System.out.println();
        System.out.println("========== BÁO CÁO TỶ LỆ LẤP ĐẦY SÂN ==========");
        System.out.printf("%-12s | %10s | %12s%n", "Mã trận", "Tổng ghế", "Lấp đầy (%)");
        System.out.println("-".repeat(42));
        for (Map.Entry<String, Double> e : occupancyByMatch.entrySet()) {
            String matchId = e.getKey();
            int totalSeats = totalSeatsByMatch.getOrDefault(matchId, 0);
            System.out.printf("%-12s | %10d | %11.1f%%%n", matchId, totalSeats, e.getValue());
        }
    }
}
