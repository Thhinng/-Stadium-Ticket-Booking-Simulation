import controller.ReportController;
import model.TransactionLog;
import model.TransactionType;
import service.ReportService;
import view.AnalyticsView;
import view.LogView;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Main — chương trình minh hoạ (demo) CHỈ cho phần việc của Người 4 (Analytics & Logs).
 *
 * Toàn bộ dữ liệu bên dưới là SỐ LIỆU MẪU tự tạo ra để test độc lập
 * (KHÔNG phụ thuộc class Ticket/Match của các thành viên khác). Khi ghép nhóm,
 * thay vì gõ tay các List/Map này, Người 2 và Người 3 (hoặc lớp Main chung của cả nhóm)
 * sẽ lấy dữ liệu thật từ danh sách Ticket/Match qua getter rồi truyền vào ReportController
 * tương tự như dưới đây.
 */
public class Main {
    public static void main(String[] args) {
        ReportService reportService = new ReportService();
        ReportController reportController = new ReportController(reportService);
        AnalyticsView analyticsView = new AnalyticsView();
        LogView logView = new LogView();

        // ----- 1. Dữ liệu mẫu: giá các vé ĐÃ THANH TOÁN, gộp theo mã trận -----
        // (Trong thực tế: Người 3 lọc Ticket có status == PAID rồi lấy getPrice())
        Map<String, List<Double>> paidPricesByMatch = new LinkedHashMap<>();
        paidPricesByMatch.put("M001", Arrays.asList(200000.0, 200000.0)); // trận M001: 2 vé đã thanh toán
        paidPricesByMatch.put("M002", Arrays.asList(250000.0));           // trận M002: 1 vé đã thanh toán

        // ----- 2. Dữ liệu mẫu: số ghế đã lấp đầy & tổng số ghế của mỗi trận -----
        // (Trong thực tế: Người 2 cung cấp qua Match.getTotalSeats() + đếm ghế đã đặt)
        Map<String, Integer> occupiedSeatsByMatch = new LinkedHashMap<>();
        occupiedSeatsByMatch.put("M001", 3);
        occupiedSeatsByMatch.put("M002", 1);

        Map<String, Integer> totalSeatsByMatch = new LinkedHashMap<>();
        totalSeatsByMatch.put("M001", 100);
        totalSeatsByMatch.put("M002", 80);

        // ----- 3. Ghi vài dòng nhật ký minh hoạ (bình thường do Controller khác gọi) -----
        reportController.logTransaction("fan01", TransactionType.BOOK, "Đặt ghế A1 - trận M001", 0);
        reportController.logTransaction("fan01", TransactionType.PAYMENT, "Thanh toán vé T001", 200000);
        reportController.logTransaction("fan02", TransactionType.BOOK, "Đặt ghế A2 - trận M002", 0);
        reportController.logTransaction("fan02", TransactionType.CANCEL, "Hủy vé T005", 0);

        // ----- 4. Use case: Xem báo cáo doanh thu -----
        double totalRevenue = reportController.getTotalRevenue(Arrays.asList(200000.0, 200000.0, 250000.0));
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
