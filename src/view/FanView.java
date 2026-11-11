package view;

import model.*;
import service.BookingService;
import service.EventService;
import util.MoneyUtil;
import util.TimeUtil;
import java.util.*;

public class FanView {
    private final ConsoleView ui;
    public FanView(ConsoleView ui) { this.ui = ui; }

    public void showMenu(User fan) {
        ui.title("FAN - XIN CHÀO " + fan.name);
        System.out.println(" 1. Danh sách trận đấu             2. Tìm trận đấu theo từ khóa");
        System.out.println(" 3. Xem sơ đồ chỗ ngồi             4. Chọn và giữ ghế (5 phút)");
        System.out.println(" 5. Thanh toán / hủy ghế đang giữ  6. Vé đã mua & lịch sử đơn");
        System.out.println(" 7. Lịch sử giao dịch              8. Thông tin tài khoản");
        System.out.println(" 9. Đổi tên hiển thị              10. Đổi mật khẩu");
        System.out.println("11. Yêu cầu hủy vé/hoàn tiền");
        System.out.println(" 0. Đăng xuất (tự giải phóng ghế đang giữ)");
    }

    public void showMatches(List<Event> events) {
        ui.title("TRẬN ĐẤU SẮP DIỄN RA");
        if (events.isEmpty()) { System.out.println("Không tìm thấy trận đấu."); return; }
        for (Event e : events) {
            System.out.printf("#%-3d %-31s | %-16s | %-7s | %s%n", e.id, e.title(),
                    TimeUtil.format(e.startsAt), e.status, e.competition);
        }
    }

    public void showSeatMap(Event event, List<Section> sections, Map<String, Character> status, EventService events) {
        ui.title("SƠ ĐỒ SÂN - TRẬN #" + event.id + " " + event.title());
        System.out.println("Thời gian: " + TimeUtil.format(event.startsAt) + " | Mở bán: " + event.status);
        System.out.println("                           [ SÂN THI ĐẤU ]");
        System.out.println("O: Trống | L: Đang giữ | Y: Bạn đang giữ | B: Đã bán | -: Khu đóng");
        for (Section s : sections) {
            System.out.println();
            System.out.println("-- " + s.name + " [" + s.code + "] | Giá: " + MoneyUtil.format(events.price(event, s))
                    + " | " + s.capacity + " ghế | " + (s.active ? "HOẠT ĐỘNG" : "TẠM ĐÓNG") + " --");
            for (int number = 1; number <= s.capacity; number++) {
                String id = BookingService.seatId(s.code, number);
                char flag = status.getOrDefault(id, '?');
                System.out.printf("[%02d:%s] ", number, flag);
                if (number % 8 == 0 || number == s.capacity) System.out.println();
            }
        }
        System.out.println("Nhập mã ghế theo khu và số, ví dụ A-01,B-03 (tối đa 4 ghế).");
    }

    public void showHold(Hold h, Event e) {
        ui.title("GHẾ ĐANG ĐƯỢC GIỮ - #" + h.id);
        System.out.println("Trận: " + e.title() + " | " + TimeUtil.format(e.startsAt));
        System.out.println("Ghế: " + String.join(", ", h.seatIds));
        System.out.println("Tổng thanh toán: " + MoneyUtil.format(h.total));
        System.out.println("Hết hạn giữ ghế: " + TimeUtil.format(h.expiresAt));
        System.out.println("Khi hết hạn, vé trở về trạng thái trống; thanh toán sẽ bị từ chối.");
    }

    public void showCheckoutMenu() {
        System.out.println("1. Thanh toán bằng ví điện tử (DEMO - thành công)");
        System.out.println("2. Chuyển khoản ngân hàng (DEMO - thành công)");
        System.out.println("3. Giả lập thanh toán THẤT BẠI (thử chức năng)");
        System.out.println("4. Hủy giữ ghế");
        System.out.println("0. Quay lại");
    }

    public void showOrders(List<Order> orders, EventService events) {
        ui.title("VÉ ĐÃ MUA - LỊCH SỬ ĐƠN HÀNG");
        if (orders.isEmpty()) { System.out.println("Chưa có đơn hàng nào."); return; }
        for (Order o : orders) {
            Event e = events.find(o.eventId);
            System.out.printf("Đơn #%d | %-9s | %s%n", o.id, o.status, e.title());
            System.out.println("  Ghế: " + String.join(", ", o.seatIds)
                    + " | Tổng: " + MoneyUtil.format(o.total));
            System.out.println("  Mua lúc: " + TimeUtil.format(o.purchasedAt)
                    + " | Trận: " + TimeUtil.format(e.startsAt));
            System.out.println("  Thanh toán: " + o.paymentMethod + " | Mã GD: " + o.paymentReference);
        }
    }

    public void showPayments(List<PaymentRecord> records) {
        ui.title("LỊCH SỬ GIAO DỊCH (MÔ PHỎNG)");
        if (records.isEmpty()) { System.out.println("Chưa có giao dịch."); return; }
        for (PaymentRecord r : records) {
            System.out.printf("#%-4d | %-7s | %-7s | Đơn #%-4d | %-14s | %s%n", r.id,
                    r.type, r.result, r.orderId, MoneyUtil.format(r.amount), TimeUtil.format(r.createdAt));
            System.out.println("       " + r.method + " | " + r.reference);
        }
    }

    public void showProfile(User user) {
        ui.title("HỒ SƠ TÀI KHOẢN");
        System.out.println("Mã người dùng: " + user.id);
        System.out.println("Tài khoản: " + user.username);
        System.out.println("Họ tên: " + user.name);
        System.out.println("Vai trò: " + user.role);
        System.out.println("Trạng thái: " + (user.active ? "Hoạt động" : "Bị khóa"));
        System.out.println("Ngày tạo: " + TimeUtil.format(user.createdAt));
    }
}
