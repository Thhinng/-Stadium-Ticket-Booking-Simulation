package view;

import model.*;
import service.EventService;
import util.MoneyUtil;
import util.TimeUtil;
import java.util.List;

public class AdminView {
    private final ConsoleView ui;
    public AdminView(ConsoleView ui) { this.ui = ui; }

    public void showMenu() {
        ui.title("BẢNG ĐIỀU KHIỂN ADMIN - TOÀN QUYỀN QUẢN LÝ");
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
        ui.title("THỐNG KÊ HỆ THỐNG");
        System.out.println("Fan: " + d.totalFans + " (đang hoạt động: " + d.activeFans + ")");
        System.out.println("Trận đấu: " + d.totalEvents + " (đang mở bán: " + d.openEvents + ")");
        System.out.println("Đơn đã thanh toán còn hiệu lực: " + d.paidOrders);
        System.out.println("Đơn đã hoàn tiền: " + d.refundedOrders);
        System.out.println("Ghế đang giữ (lượt giữ): " + d.activeHolds);
        System.out.println("Doanh thu ròng mô phỏng: " + MoneyUtil.format(d.netRevenue));
    }

    public void showEvents(List<Event> list) {
        ui.title("TẤT CẢ TRẬN ĐẤU");
        if (list.isEmpty()) { System.out.println("Chưa có trận nào."); return; }
        for (Event e : list) {
            System.out.printf("#%-3d | %-34s | %-16s | %-10s | %s%n", e.id, e.title(),
                    TimeUtil.format(e.startsAt), e.status, e.competition);
        }
    }

    public void showSections(List<Section> sections) {
        ui.title("QUẢN LÝ KHÁN ĐÀI / KHU GHẾ");
        for (Section s : sections) {
            System.out.printf("%-3s | %-28s | %2d ghế | %14s | %s%n", s.code, s.name,
                    s.capacity, MoneyUtil.format(s.basePrice), s.active ? "MỞ" : "ĐÓNG");
        }
        System.out.println("Giá cơ bản dùng cho trận mới. Giá trận đang có chỉ đổi tại mục 10.");
    }

    public void showFans(List<User> fans) {
        ui.title("DANH SÁCH FAN");
        if (fans.isEmpty()) { System.out.println("Không tìm thấy Fan."); return; }
        for (User f : fans) {
            System.out.printf("#%-4d | %-20s | %-30s | %s%n", f.id, f.username, f.name,
                    f.active ? "Đang hoạt động" : "Đã khóa");
        }
    }

    public void showOrders(List<Order> orders, EventService events, boolean adminMode) {
        ui.title("QUẢN LÝ ĐƠN VÉ" + (adminMode ? " - TẤT CẢ FAN" : ""));
        if (orders.isEmpty()) { System.out.println("Chưa có đơn vé."); return; }
        for (Order o : orders) {
            Event e = events.find(o.eventId);
            System.out.printf("#%-4d | Fan #%-4d | Trận #%-3d | %-8s | %s%n", o.id, o.userId,
                    o.eventId, o.status, e.title());
            System.out.println("       Ghế: " + String.join(", ", o.seatIds) + " | " + MoneyUtil.format(o.total)
                    + " | Mua: " + TimeUtil.format(o.purchasedAt));
            System.out.println("       Tham chiếu: " + o.paymentReference);
        }
    }

    public void showPayments(List<PaymentRecord> payments) {
        ui.title("NHẬT KÝ GIAO DỊCH - DEMO");
        if (payments.isEmpty()) { System.out.println("Chưa có giao dịch."); return; }
        for (PaymentRecord p : payments) {
            System.out.printf("GD #%-4d | Fan #%-4d | Đơn #%-4d | %-7s | %-7s | %14s%n", p.id,
                    p.userId, p.orderId, p.type, p.result, MoneyUtil.format(p.amount));
            System.out.println("       " + TimeUtil.format(p.createdAt) + " | " + p.method + " | " + p.reference);
        }
    }

    public void showHolds(List<Hold> holds) {
        ui.title("GHẾ ĐANG GIỮ (CHƯA THANH TOÁN)");
        if (holds.isEmpty()) { System.out.println("Không có lượt giữ ghế nào."); return; }
        for (Hold h : holds) {
            System.out.printf("Giữ #%-4d | Fan #%-4d | Trận #%-3d | %-18s | %s%n", h.id,
                    h.userId, h.eventId, String.join(", ", h.seatIds), TimeUtil.format(h.expiresAt));
        }
    }
}
