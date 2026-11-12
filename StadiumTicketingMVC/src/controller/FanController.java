package controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import model.Event;
import model.Hold;
import model.Order;
import model.Role;
import model.Section;
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
        int fanId = fan.getId();

        try {
            while (true) {
                try {
                    User currentFan = this.authService.requireActive(fanId, Role.FAN);
                    this.fanView.showMenu(currentFan);
                    int choice = this.console.askInt("Fan > ");

                    if (choice == 0) {
                        this.console.info("Đã đăng xuất Fan.");
                        return;
                    }

                    this.handleChoice(choice, fanId);
                } catch (IllegalArgumentException | IllegalStateException e) {
                    this.console.error(e.getMessage());
                }
            }
        } finally {
            // Không giữ ghế sau khi người dùng đăng xuất.
            this.bookingService.releaseOnLogout(fanId);
        }
    }

    private void handleChoice(int choice, int fanId) {
        switch (choice) {
            case 1:
                this.fanView.showMatches(this.eventService.listPublic());
                break;
            case 2:
                String keyword = this.console.ask("Nhập tên đội hoặc giải đấu: ");
                this.fanView.showMatches(this.eventService.searchPublic(keyword));
                break;
            case 3:
                this.showSeatMap(fanId);
                break;
            case 4:
                this.reserveSeats(fanId);
                break;
            case 5:
                this.checkout(fanId);
                break;
            case 6:
                this.showOrders(fanId);
                break;
            case 7:
                this.fanView.showPayments(this.bookingService.paymentsForFan(fanId));
                break;
            case 8:
                this.fanView.showProfile(this.authService.getById(fanId));
                break;
            case 9:
                this.changeName(fanId);
                break;
            case 10:
                this.changePassword(fanId);
                break;
            case 11:
                this.requestRefund(fanId);
                break;
            default:
                this.console.error("Vui lòng chọn chức năng có trong menu.");
        }
    }

    private void showSeatMap(int fanId) {
        this.fanView.showMatches(this.eventService.listPublic());
        int matchId = this.console.askInt("Mã trận muốn xem sơ đồ: ");
        Event match = this.findPublicMatch(matchId);

        List<Section> sections = this.eventService.sections();
        this.fanView.showSeatMap(match, sections,
                this.bookingService.seatMap(fanId, matchId), this.eventService.pricesForSections(match, sections));
    }

    private void reserveSeats(int fanId) {
        this.fanView.showMatches(this.eventService.listPublic());
        int matchId = this.console.askInt("Mã trận muốn mua vé: ");
        Event match = this.findPublicMatch(matchId);

        List<Section> sections = this.eventService.sections();
        this.fanView.showSeatMap(match, sections,
                this.bookingService.seatMap(fanId, matchId), this.eventService.pricesForSections(match, sections));

        String text = this.console.ask("Chọn ghế, ví dụ A-01,A-02: ");
        List<String> seats = Arrays.asList(text.split(","));
        Hold hold = this.bookingService.reserve(fanId, matchId, seats);

        this.console.success("Đã giữ ghế trong 5 phút. Người khác không thể đặt các ghế này.");
        this.fanView.showHold(hold, match);
    }

    private Event findPublicMatch(int matchId) {
        for (Event match : this.eventService.listPublic()) {
            if (match.getId() == matchId) {
                return match;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy trận đấu sắp tới #" + matchId);
    }

    private void checkout(int fanId) {
        Hold hold = this.bookingService.currentHold(fanId);
        if (hold == null) {
            this.console.info("Chưa có ghế được giữ. Hãy vào mục 4 để chọn ghế.");
            return;
        }

        this.fanView.showHold(hold, this.eventService.find(hold.getEventId()));
        this.fanView.showCheckoutMenu();
        int choice = this.console.askInt("Thanh toán > ");

        if (choice == 0) {
            return;
        }
        if (choice == 4) {
            this.cancelHold(fanId);
            return;
        }
        if (choice < 1 || choice > 3) {
            this.console.error("Phương thức thanh toán không hợp lệ.");
            return;
        }

        String method = choice == 2 ? "CHUYEN_KHOAN_DEMO" : "VI_DIEN_TU_DEMO";
        boolean paymentSuccess = choice != 3;
        String message = "Xác nhận giao dịch DEMO với số tiền "
                + MoneyUtil.format(hold.getTotal()) + "?";
        if (!this.console.confirm(message)) {
            return;
        }

        // Khi mô phỏng thất bại, service báo lỗi và vẫn giữ ghế đến khi hết hạn.
        Order order = this.bookingService.checkout(fanId, method, paymentSuccess);
        this.fanView.showTicket(order);
    }

    private void showOrders(int fanId) {
        List<Order> orders = this.bookingService.ordersForFan(fanId);
        Map<Integer, Event> eventsById = new HashMap<Integer, Event>();
        for (Order order : orders) {
            eventsById.put(order.getEventId(), this.eventService.find(order.getEventId()));
        }
        this.fanView.showOrders(orders, eventsById);
    }

    private void cancelHold(int fanId) {
        if (this.console.confirm("Bạn muốn hủy các ghế đang giữ?")) {
            this.bookingService.release(fanId);
            this.console.success("Đã trả ghế về trạng thái trống.");
        }
    }

    private void changeName(int fanId) {
        String newName = this.console.ask("Họ tên mới: ");
        this.authService.changeName(fanId, newName);
        this.console.success("Đã cập nhật họ tên.");
    }

    private void changePassword(int fanId) {
        String currentPassword = this.console.askPassword("Mật khẩu hiện tại: ");
        String newPassword = this.console.askPassword("Mật khẩu mới: ");
        this.authService.changePassword(fanId, currentPassword, newPassword);
        this.console.success("Đổi mật khẩu thành công.");
    }

    private void requestRefund(int fanId) {
        this.showOrders(fanId);
        int orderId = this.console.askInt("Mã đơn cần hủy vé: ");
        if (this.console.confirm("Hoàn tiền mô phỏng cho đơn #" + orderId + "?")) {
            this.bookingService.requestRefund(fanId, orderId);
            this.console.success("Đã hoàn vé và giải phóng ghế trong đơn #" + orderId);
        }
    }
}
