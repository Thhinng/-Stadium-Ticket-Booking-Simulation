package view;

import model.*;
import util.MoneyUtil;
import util.TimeUtil;
import java.util.List;
import java.util.Map;

public class AdminView {
    private final ConsoleView ui;
    public AdminView(ConsoleView ui) { this.ui = ui; }

    public void showMenu() {
        this.ui.title("BẢNG ĐIỀU KHIỂN ADMIN - TOÀN QUYỀN QUẢN LÝ");
        System.out.println(" 1. Thống kê tổng quan           2. Xem tất cả trận đấu");
        System.out.println(" 3. Tạo trận đấu                 4. Sửa trận đấu");
        System.out.println(" 5. Mở / đóng bán vé            6. Hủy trận + hoàn tiền tự động");
        System.out.println(" 7. Xem khán đài                 8. Thêm khán đài");
        System.out.println(" 9. Sửa khán đài                10. Đổi giá vé theo trận");
        System.out.println("11. Danh sách Fan              12. Tìm kiếm Fan");
        System.out.println("13. Xem Fan & vé họ đã mua     14. Khóa / mở khóa Fan");
        System.out.println("15. Đặt lại mật khẩu Fan       16. Quản lý toàn bộ đơn vé");
        System.out.println("17. Xem sơ đồ sân              18. Hoàn tiền một đơn vé");
        System.out.println("19. Tất cả giao dịch           20. Quản lý ghế đang giữ");
        System.out.println("21. Đổi mật khẩu Admin");
        System.out.println(" 0. Đăng xuất Admin");
    }

    public void showDashboard(Dashboard d) {
        this.ui.title("THỐNG KÊ HỆ THỐNG");
        System.out.println("Fan: " + d.getTotalFans() + " (đang hoạt động: " + d.getActiveFans() + ")");
        System.out.println("Trận đấu: " + d.getTotalEvents() + " (đang mở bán: " + d.getOpenEvents() + ")");
        System.out.println("Đơn đã thanh toán còn hiệu lực: " + d.getPaidOrders());
        System.out.println("Đơn đã hoàn tiền: " + d.getRefundedOrders());
        System.out.println("Ghế đang giữ (lượt giữ): " + d.getActiveHolds());
        System.out.println("Doanh thu ròng mô phỏng: " + MoneyUtil.format(d.getNetRevenue()));
    }

    public void showEvents(List<Event> list) {
        this.ui.title("TẤT CẢ TRẬN ĐẤU");
        if (list.isEmpty()) { System.out.println("Chưa có trận nào."); return; }
        for (Event e : list) {
            System.out.printf("#%-3d | %-34s | %-16s | %-10s | %s%n", e.getId(), e.title(),
                    TimeUtil.format(e.getStartsAt()), e.getStatus(), e.getCompetition());
        }
    }

    public void showSections(List<Section> sections) {
        this.ui.title("QUẢN LÝ KHÁN ĐÀI / KHU GHẾ");
        for (Section s : sections) {
            System.out.printf("%-3s | %-28s | %2d ghế | %14s | %s%n", s.getCode(), s.getName(),
                    s.getCapacity(), MoneyUtil.format(s.getBasePrice()), s.getActive() ? "MỞ" : "ĐÓNG");
        }
        System.out.println("Giá cơ bản dùng cho trận mới. Giá trận đang có chỉ đổi tại mục 10.");
    }

    public void showFans(List<User> fans) {
        this.ui.title("DANH SÁCH FAN");
        if (fans.isEmpty()) { System.out.println("Không tìm thấy Fan."); return; }
        for (User f : fans) {
            System.out.printf("#%-4d | %-20s | %-30s | %s%n", f.getId(), f.getUsername(), f.getName(),
                    f.getActive() ? "Đang hoạt động" : "Đã khóa");
        }
    }

    public void showOrders(List<Order> orders, Map<Integer, Event> eventsById, boolean adminMode) {
        this.ui.title("QUẢN LÝ ĐƠN VÉ" + (adminMode ? " - TẤT CẢ FAN" : ""));
        if (orders.isEmpty()) { System.out.println("Chưa có đơn vé."); return; }
        for (Order o : orders) {
            Event e = eventsById.get(o.getEventId());
            System.out.printf("#%-4d | Fan #%-4d | Trận #%-3d | %-8s | %s%n", o.getId(), o.getUserId(),
                    o.getEventId(), o.getStatus(), e.title());
            System.out.println("       Ghế: " + String.join(", ", o.getSeatIds()) + " | " + MoneyUtil.format(o.getTotal())
                    + " | Mua: " + TimeUtil.format(o.getPurchasedAt()));
            System.out.println("       Tham chiếu: " + o.getPaymentReference());
        }
    }

    public void showKeepExistingInfoHint() {
        System.out.println("Nhấn Enter để giữ nguyên thông tin.");
    }

    public void showPayments(List<PaymentRecord> payments) {
        this.ui.title("NHẬT KÝ GIAO DỊCH - DEMO");
        if (payments.isEmpty()) { System.out.println("Chưa có giao dịch."); return; }
        for (PaymentRecord p : payments) {
            System.out.printf("GD #%-4d | Fan #%-4d | Đơn #%-4d | %-7s | %-7s | %14s%n", p.getId(),
                    p.getUserId(), p.getOrderId(), p.getType(), p.getResult(), MoneyUtil.format(p.getAmount()));
            System.out.println("       " + TimeUtil.format(p.getCreatedAt()) + " | " + p.getMethod() + " | " + p.getReference());
        }
    }

    public void showHolds(List<Hold> holds) {
        this.ui.title("GHẾ ĐANG GIỮ (CHƯA THANH TOÁN)");
        if (holds.isEmpty()) { System.out.println("Không có lượt giữ ghế nào."); return; }
        for (Hold h : holds) {
            System.out.printf("Giữ #%-4d | Fan #%-4d | Trận #%-3d | %-18s | %s%n", h.getId(),
                    h.getUserId(), h.getEventId(), String.join(", ", h.getSeatIds()), TimeUtil.format(h.getExpiresAt()));
        }
    }
}
