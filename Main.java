package app;

import app.controller.ReportController;
import app.model.TransactionLog;
import app.model.TransactionType;
import app.service.ReportService;
import app.view.AnalyticsView;
import app.view.LogView;

import java.util.List;
import java.util.Map;

/**
 * Main — chương trình minh hoạ (demo) CHỈ cho phần việc của Người 4.
 *
 * Toàn bộ dữ liệu bên dưới là SỐ LIỆU MẪU tự bịa ra để test độc lập
 * (KHÔNG dùng class Ticket/Match của ai cả). Khi ghép nhóm, thay vì gõ tay
 * các List/Map này, Người 2 và Người 3 (hoặc lớp Main chung của cả nhóm) sẽ
 * lấy dữ liệu thật ra từ danh sách Ticket/Match của họ bằng vài dòng
 * getter rồi truyền vào ReportController y như dưới đây — code trong
 * ReportService/ReportController/View của Người 4 không cần sửa gì cả.
 */
public class Main {
    public static void main(String[] args) {
        ReportService reportService = new ReportService();
        ReportController reportController = new ReportController(reportService);
        AnalyticsView analyticsView = new AnalyticsView();
        LogView logView = new LogView();

        // ----- 1. Dữ liệu mẫu: giá các vé ĐÃ THANH TOÁN, gộp theo mã trận -----
        // (Trong thực tế: Người 3 lọc Ticket có status == PAID rồi lấy getPrice())
        Map<String, List<Double>> paidPricesByMatch = Map.of(
                "M001", List.of(200000.0, 200000.0),   // trận M001: 2 vé đã thanh toán
                "M002", List.of(250000.0)               // trận M002: 1 vé đã thanh toán
        );

        // ----- 2. Dữ liệu mẫu: số ghế đã lấp đầy & tổng số ghế của mỗi trận -----
        // (Trong thực tế: Người 2 cung cấp qua Match.getTotalSeats() + đếm ghế đã đặt)
        Map<String, Integer> occupiedSeatsByMatch = Map.of("M001", 3, "M002", 1);
        Map<String, Integer> totalSeatsByMatch = Map.of("M001", 100, "M002", 80);

        // ----- 3. Ghi vài dòng nhật ký minh hoạ (bình thường do Controller khác gọi) -----
        reportController.logTransaction("fan01", TransactionType.BOOK, "Đặt ghế A1 - trận M001", 0);
        reportController.logTransaction("fan01", TransactionType.PAYMENT, "Thanh toán vé T001", 200000);
        reportController.logTransaction("fan02", TransactionType.BOOK, "Đặt ghế A2 - trận M002", 0);
        reportController.logTransaction("fan02", TransactionType.CANCEL, "Hủy vé T005", 0);

        // ----- 4. Use case: Xem báo cáo doanh thu -----
        double totalRevenue = reportController.getTotalRevenue(List.of(200000.0, 200000.0, 250000.0));
        analyticsView.showTotalRevenue(totalRevenue);

        Map<String, Double> revenueByMatch = reportController.getRevenueByMatch(paidPricesByMatch);
        analyticsView.showRevenueByMatch(revenueByMatch);

        // ----- 5. Use case: Xem báo cáo tỷ lệ lấp đầy sân -----
        Map<String, Double> occupancyReport =
                reportController.getOccupancyReport(occupiedSeatsByMatch, totalSeatsByMatch);
        analyticsView.showOccupancyReport(totalSeatsByMatch, occupancyReport);

        // ----- 6. Use case: Xem nhật ký giao dịch -----
        logView.showLogs(reportController.getTransactionLogs());

        // Ví dụ lọc nhật ký theo người dùng
        List<TransactionLog> fan01Logs =
                reportController.searchTransactionLogs("fan01", null, null, null);
        System.out.println("\n--- Nhật ký riêng của fan01 ---");
        logView.showLogs(fan01Logs);
    }
}
