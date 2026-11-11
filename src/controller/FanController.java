package controller;

import java.util.Arrays;
import java.util.List;
import model.Event;
import model.Hold;
import model.Order;
import model.Role;
import model.User;
import service.AuthService;
import service.BookingService;
import service.EventService;
import util.MoneyUtil;
import view.ConsoleView;
import view.FanView;

/** Các menu của Fan; xử lý nghiệp vụ đặt vé được giao cho BookingService. */
public class FanController {
    private final AuthService authService;
    private final EventService eventService;
    private final BookingService bookingService;
    private final FanView fanView;
    private final ConsoleView console;

    public FanController(AuthService authService, EventService eventService,
                         BookingService bookingService, FanView fanView, ConsoleView console) {
        this.authService = authService;
        this.eventService = eventService;
        this.bookingService = bookingService;
        this.fanView = fanView;
        this.console = console;
    }

    public void run(User fan) {
        int fanId = fan.id;

        try {
            while (true) {
                try {
                    User currentFan = authService.requireActive(fanId, Role.FAN);
                    fanView.showMenu(currentFan);
                    int choice = console.askInt("Fan > ");

                    if (choice == 0) {
                        console.info("Đã đăng xuất Fan.");
                        return;
                    }

                    handleChoice(choice, fanId);
                } catch (IllegalArgumentException | IllegalStateException e) {
                    console.error(e.getMessage());
                }
            }
        } finally {
            // Không giữ ghế sau khi người dùng đăng xuất.
            bookingService.releaseOnLogout(fanId);
        }
    }

    private void handleChoice(int choice, int fanId) {
        switch (choice) {
            case 1:
                fanView.showMatches(eventService.listPublic());
                break;
            case 2:
                String keyword = console.ask("Nhập tên đội hoặc giải đấu: ");
                fanView.showMatches(eventService.searchPublic(keyword));
                break;
            case 3:
                showSeatMap(fanId);
                break;
            case 4:
                reserveSeats(fanId);
                break;
            case 5:
                checkout(fanId);
                break;
            case 6:
                fanView.showOrders(bookingService.ordersForFan(fanId), eventService);
                break;
            case 7:
                fanView.showPayments(bookingService.paymentsForFan(fanId));
                break;
            case 8:
                fanView.showProfile(authService.getById(fanId));
                break;
            case 9:
                changeName(fanId);
                break;
            case 10:
                changePassword(fanId);
                break;
            case 11:
                requestRefund(fanId);
                break;
            default:
                console.error("Vui lòng chọn chức năng có trong menu.");
        }
    }

    private void showSeatMap(int fanId) {
        fanView.showMatches(eventService.listPublic());
        int matchId = console.askInt("Mã trận muốn xem sơ đồ: ");
        Event match = findPublicMatch(matchId);

        fanView.showSeatMap(match, eventService.sections(),
                bookingService.seatMap(fanId, matchId), eventService);
    }

    private void reserveSeats(int fanId) {
        fanView.showMatches(eventService.listPublic());
        int matchId = console.askInt("Mã trận muốn mua vé: ");
        Event match = findPublicMatch(matchId);

        fanView.showSeatMap(match, eventService.sections(),
                bookingService.seatMap(fanId, matchId), eventService);

        String text = console.ask("Chọn ghế, ví dụ A-01,A-02: ");
        List<String> seats = Arrays.asList(text.split(","));
        Hold hold = bookingService.reserve(fanId, matchId, seats);

        console.success("Đã giữ ghế trong 5 phút. Người khác không thể đặt các ghế này.");
        fanView.showHold(hold, match);
    }

    private Event findPublicMatch(int matchId) {
        for (Event match : eventService.listPublic()) {
            if (match.id == matchId) {
                return match;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy trận đấu sắp tới #" + matchId);
    }

    private void checkout(int fanId) {
        Hold hold = bookingService.currentHold(fanId);
        if (hold == null) {
            console.info("Chưa có ghế được giữ. Hãy vào mục 4 để chọn ghế.");
            return;
        }

        fanView.showHold(hold, eventService.find(hold.eventId));
        fanView.showCheckoutMenu();
        int choice = console.askInt("Thanh toán > ");

        if (choice == 0) {
            return;
        }
        if (choice == 4) {
            cancelHold(fanId);
            return;
        }
        if (choice < 1 || choice > 3) {
            console.error("Phương thức thanh toán không hợp lệ.");
            return;
        }

        String method = choice == 2 ? "CHUYEN_KHOAN_DEMO" : "VI_DIEN_TU_DEMO";
        boolean paymentSuccess = choice != 3;
        String message = "Xác nhận giao dịch DEMO với số tiền "
                + MoneyUtil.format(hold.total) + "?";
        if (!console.confirm(message)) {
            return;
        }

        // Khi mô phỏng thất bại, service báo lỗi và vẫn giữ ghế đến khi hết hạn.
        Order order = bookingService.checkout(fanId, method, paymentSuccess);
        console.success("Thanh toán mô phỏng thành công! Đơn vé #" + order.id);
        System.out.println("Mã vé: STADIUM-" + order.id);
        System.out.println("Ghế: " + String.join(", ", order.seatIds));
        System.out.println("Mã giao dịch: " + order.paymentReference);
        System.out.println("Đây là bản DEMO, không thu tiền thật.");
    }

    private void cancelHold(int fanId) {
        if (console.confirm("Bạn muốn hủy các ghế đang giữ?")) {
            bookingService.release(fanId);
            console.success("Đã trả ghế về trạng thái trống.");
        }
    }

    private void changeName(int fanId) {
        String newName = console.ask("Họ tên mới: ");
        authService.changeName(fanId, newName);
        console.success("Đã cập nhật họ tên.");
    }

    private void changePassword(int fanId) {
        String currentPassword = console.askPassword("Mật khẩu hiện tại: ");
        String newPassword = console.askPassword("Mật khẩu mới: ");
        authService.changePassword(fanId, currentPassword, newPassword);
        console.success("Đổi mật khẩu thành công.");
    }

    private void requestRefund(int fanId) {
        fanView.showOrders(bookingService.ordersForFan(fanId), eventService);
        int orderId = console.askInt("Mã đơn cần hủy vé: ");
        if (console.confirm("Hoàn tiền mô phỏng cho đơn #" + orderId + "?")) {
            bookingService.requestRefund(fanId, orderId);
            console.success("Đã hoàn vé và giải phóng ghế trong đơn #" + orderId);
        }
    }
}
