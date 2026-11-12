package view;

import model.*;
import service.BookingService;
import util.MoneyUtil;
import util.TimeUtil;
import java.util.*;

public class FanView {
    private final ConsoleView ui;
    public FanView(ConsoleView ui) { this.ui = ui; }

    public void showMenu(User fan) {
        this.ui.title("FAN - XIN CHÀO " + fan.getName());
        System.out.println(" 1. Danh sách trận đấu             2. Tìm trận đấu theo từ khóa");
        System.out.println(" 3. Xem sơ đồ chỗ ngồi             4. Chọn và giữ ghế (5 phút)");
        System.out.println(" 5. Thanh toán / hủy ghế đang giữ  6. Vé đã mua & lịch sử đơn");
        System.out.println(" 7. Lịch sử giao dịch              8. Thông tin tài khoản");
        System.out.println(" 9. Đổi tên hiển thị              10. Đổi mật khẩu");
        System.out.println("11. Yêu cầu hủy vé/hoàn tiền");
        System.out.println(" 0. Đăng xuất (tự giải phóng ghế đang giữ)");
    }

    public void showMatches(List<Event> events) {
        this.ui.title("TRẬN ĐẤU SẮP DIỄN RA");
        if (events.isEmpty()) { System.out.println("Không tìm thấy trận đấu."); return; }
        for (Event e : events) {
            System.out.printf("#%-3d %-31s | %-16s | %-7s | %s%n", e.getId(), e.title(),
                    TimeUtil.format(e.getStartsAt()), e.getStatus(), e.getCompetition());
        }
    }

    public void showSeatMap(Event event, List<Section> sections, Map<String, Character> status, Map<String, Long> prices) {
        this.ui.title("SƠ ĐỒ SÂN - TRẬN #" + event.getId() + " " + event.title());
        System.out.println("Thời gian: " + TimeUtil.format(event.getStartsAt()) + " | Mở bán: " + event.getStatus());
        System.out.println("                           [ SÂN THI ĐẤU ]");
        System.out.println("O: Trống | L: Đang giữ | Y: Bạn đang giữ | B: Đã bán | -: Khu đóng");
        for (Section s : sections) {
            System.out.println();
            System.out.println("-- " + s.getName() + " [" + s.getCode() + "] | Giá: " + MoneyUtil.format(prices.get(s.getCode()))
                    + " | " + s.getCapacity() + " ghế | " + (s.getActive() ? "HOẠT ĐỘNG" : "TẠM ĐÓNG") + " --");
            for (int number = 1; number <= s.getCapacity(); number++) {
                String id = BookingService.seatId(s.getCode(), number);
                char flag = status.getOrDefault(id, '?');
                System.out.printf("[%02d:%s] ", number, flag);
                if (number % 8 == 0 || number == s.getCapacity()) System.out.println();
            }
        }
        System.out.println("Nhập mã ghế theo khu và số, ví dụ A-01,B-03 (tối đa 4 ghế).");
    }

    public void showHold(Hold h, Event e) {
        this.ui.title("GHẾ ĐANG ĐƯỢC GIỮ - #" + h.getId());
        System.out.println("Trận: " + e.title() + " | " + TimeUtil.format(e.getStartsAt()));
        System.out.println("Ghế: " + String.join(", ", h.getSeatIds()));
        System.out.println("Tổng thanh toán: " + MoneyUtil.format(h.getTotal()));
        System.out.println("Hết hạn giữ ghế: " + TimeUtil.format(h.getExpiresAt()));
        System.out.println("Khi hết hạn, vé trở về trạng thái trống; thanh toán sẽ bị từ chối.");
    }

    public void showCheckoutMenu() {
        System.out.println("1. Thanh toán bằng ví điện tử (DEMO - thành công)");
        System.out.println("2. Chuyển khoản ngân hàng (DEMO - thành công)");
        System.out.println("3. Giả lập thanh toán THẤT BẠI (thử chức năng)");
        System.out.println("4. Hủy giữ ghế");
        System.out.println("0. Quay lại");
    }

    public void showOrders(List<Order> orders, Map<Integer, Event> eventsById) {
        this.ui.title("VÉ ĐÃ MUA - LỊCH SỬ ĐƠN HÀNG");
        if (orders.isEmpty()) { System.out.println("Chưa có đơn hàng nào."); return; }
        for (Order o : orders) {
            Event e = eventsById.get(o.getEventId());
            System.out.printf("Đơn #%d | %-9s | %s%n", o.getId(), o.getStatus(), e.title());
            System.out.println("  Ghế: " + String.join(", ", o.getSeatIds())
                    + " | Tổng: " + MoneyUtil.format(o.getTotal()));
            System.out.println("  Mua lúc: " + TimeUtil.format(o.getPurchasedAt())
                    + " | Trận: " + TimeUtil.format(e.getStartsAt()));
            System.out.println("  Thanh toán: " + o.getPaymentMethod() + " | Mã GD: " + o.getPaymentReference());
        }
    }

    public void showTicket(Order order) {
        this.ui.success("Thanh toán mô phỏng thành công! Đơn vé #" + order.getId());
        System.out.println("Mã vé: STADIUM-" + order.getId());
        System.out.println("Ghế: " + String.join(", ", order.getSeatIds()));
        System.out.println("Mã giao dịch: " + order.getPaymentReference());
        System.out.println("Đây là bản DEMO, không thu tiền thật.");
    }

    public void showPayments(List<PaymentRecord> records) {
        this.ui.title("LỊCH SỬ GIAO DỊCH (MÔ PHỎNG)");
        if (records.isEmpty()) { System.out.println("Chưa có giao dịch."); return; }
        for (PaymentRecord r : records) {
            System.out.printf("#%-4d | %-7s | %-7s | Đơn #%-4d | %-14s | %s%n", r.getId(),
                    r.getType(), r.getResult(), r.getOrderId(), MoneyUtil.format(r.getAmount()), TimeUtil.format(r.getCreatedAt()));
            System.out.println("       " + r.getMethod() + " | " + r.getReference());
        }
    }

    public void showProfile(User user) {
        this.ui.title("HỒ SƠ TÀI KHOẢN");
        System.out.println("Mã người dùng: " + user.getId());
        System.out.println("Tài khoản: " + user.getUsername());
        System.out.println("Họ tên: " + user.getName());
        System.out.println("Vai trò: " + user.getRole());
        System.out.println("Trạng thái: " + (user.getActive() ? "Hoạt động" : "Bị khóa"));
        System.out.println("Ngày tạo: " + TimeUtil.format(user.getCreatedAt()));
    }
}
