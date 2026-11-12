package controller;

import model.Event;
import model.Order;
import model.Role;
import model.Section;
import model.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import service.AdminService;
import service.AuthService;
import service.BookingService;
import service.EventService;
import util.TimeUtil;
import view.AdminView;
import view.ConsoleView;
import view.FanView;

/** Menu quản trị. Mỗi chức năng được đặt trong một phương thức riêng cho dễ đọc. */
public class AdminController {
    private final AuthService authService;
    private final EventService eventService;
    private final BookingService bookingService;
    private final AdminService adminService;
    private final AdminView adminView;
    private final FanView fanView;
    private final ConsoleView console;

    public AdminController(AuthService authService, EventService eventService,
                           BookingService bookingService, AdminService adminService,
                           AdminView adminView, FanView fanView, ConsoleView console) {
        this.authService = authService;
        this.eventService = eventService;
        this.bookingService = bookingService;
        this.adminService = adminService;
        this.adminView = adminView;
        this.fanView = fanView;
        this.console = console;
    }

    public void run(User admin) {
        int adminId = admin.getId();

        while (true) {
            try {
                this.authService.requireActive(adminId, Role.ADMIN);
                this.adminView.showMenu();
                int choice = this.console.askInt("Admin > ");

                if (choice == 0) {
                    this.console.info("Đã đăng xuất Admin.");
                    return;
                }

                this.handleChoice(choice, adminId);
            } catch (IllegalArgumentException | IllegalStateException e) {
                this.console.error(e.getMessage());
            }
        }
    }

    private void handleChoice(int choice, int adminId) {
        switch (choice) {
            case 1:
                this.adminView.showDashboard(this.adminService.dashboard(adminId));
                break;
            case 2:
                this.adminView.showEvents(this.eventService.listAll(adminId));
                break;
            case 3:
                this.createMatch(adminId);
                break;
            case 4:
                this.editMatch(adminId);
                break;
            case 5:
                this.changeSaleStatus(adminId);
                break;
            case 6:
                this.cancelMatch(adminId);
                break;
            case 7:
                this.adminView.showSections(this.eventService.sections());
                break;
            case 8:
                this.createSection(adminId);
                break;
            case 9:
                this.editSection(adminId);
                break;
            case 10:
                this.updateTicketPrice(adminId);
                break;
            case 11:
                this.adminView.showFans(this.adminService.fans(adminId, ""));
                break;
            case 12:
                this.searchFans(adminId);
                break;
            case 13:
                this.viewFanTickets(adminId);
                break;
            case 14:
                this.changeFanStatus(adminId);
                break;
            case 15:
                this.resetFanPassword(adminId);
                break;
            case 16:
                this.showOrders(this.adminService.allOrders(adminId), true);
                break;
            case 17:
                this.viewSeatMap(adminId);
                break;
            case 18:
                this.refundOrder(adminId);
                break;
            case 19:
                this.adminView.showPayments(this.adminService.allPayments(adminId));
                break;
            case 20:
                this.manageHeldSeats(adminId);
                break;
            case 21:
                this.changeAdminPassword(adminId);
                break;
            default:
                this.console.error("Vui lòng chọn chức năng có trong menu.");
        }
    }

    // ========== QUẢN LÝ TRẬN ĐẤU ==========

    private void createMatch(int adminId) {
        this.console.title("TẠO TRẬN ĐẤU");
        String homeTeam = this.console.ask("Đội chủ nhà: ");
        String awayTeam = this.console.ask("Đội khách: ");
        String competition = this.console.ask("Giải đấu: ");
        String time = this.console.ask("Giờ đá (dd/MM/yyyy HH:mm): ");
        long kickoff = TimeUtil.parse(time);

        Event match = this.eventService.add(adminId, homeTeam, awayTeam, competition, kickoff);
        this.console.success("Đã tạo trận đấu #" + match.getId() + " và mở bán vé.");
    }

    private void editMatch(int adminId) {
        this.adminView.showEvents(this.eventService.listAll(adminId));
        int matchId = this.console.askInt("Mã trận cần sửa: ");
        Event match = this.eventService.find(matchId);
        this.adminView.showKeepExistingInfoHint();

        String homeTeam = this.console.ask("Đội chủ nhà [" + match.getHome() + "]: ");
        String awayTeam = this.console.ask("Đội khách [" + match.getAway() + "]: ");
        String competition = this.console.ask("Giải đấu [" + match.getCompetition() + "]: ");
        String time = this.console.ask("Thời gian [" + TimeUtil.format(match.getStartsAt()) + "]: ");

        if (homeTeam.isEmpty()) {
            homeTeam = match.getHome();
        }
        if (awayTeam.isEmpty()) {
            awayTeam = match.getAway();
        }
        if (competition.isEmpty()) {
            competition = match.getCompetition();
        }

        long kickoff = time.isEmpty() ? match.getStartsAt() : TimeUtil.parse(time);
        this.eventService.edit(adminId, matchId, homeTeam, awayTeam, competition, kickoff);
        this.console.success("Đã cập nhật trận đấu #" + matchId);
    }

    private void changeSaleStatus(int adminId) {
        this.adminView.showEvents(this.eventService.listAll(adminId));
        int matchId = this.console.askInt("Mã trận: ");
        int choice = this.console.askInt("1 = Mở bán, 2 = Đóng bán: ");

        if (choice != 1 && choice != 2) {
            this.console.error("Vui lòng chọn 1 hoặc 2.");
            return;
        }

        this.eventService.setSaleStatus(adminId, matchId, choice == 1);
        this.console.success("Đã cập nhật trạng thái bán vé.");
    }

    private void cancelMatch(int adminId) {
        this.adminView.showEvents(this.eventService.listAll(adminId));
        int matchId = this.console.askInt("Mã trận cần hủy: ");
        Event match = this.eventService.find(matchId);

        if (this.console.confirm("Hủy trận " + match.title() + " và hoàn vé DEMO?")) {
            int refundedOrders = this.adminService.cancelEvent(adminId, matchId);
            this.console.success("Đã hủy trận và hoàn tiền mô phỏng " + refundedOrders + " đơn.");
        }
    }

    // ========== QUẢN LÝ KHÁN ĐÀI VÀ GIÁ VÉ ==========

    private void createSection(int adminId) {
        this.adminView.showSections(this.eventService.sections());
        String code = this.console.ask("Mã khu mới (ví dụ D): ");
        String name = this.console.ask("Tên khu: ");
        int capacity = this.console.askInt("Sức chứa (1-99 ghế): ");
        long basePrice = this.console.askLong("Giá cơ bản (VND): ");

        Section section = this.adminService.addSection(adminId, code, name, capacity, basePrice);
        this.console.success("Đã tạo khán đài " + section.getCode() + ".");
    }

    private void editSection(int adminId) {
        this.adminView.showSections(this.eventService.sections());
        String code = this.console.ask("Mã khu cần sửa: ");
        Section section = this.eventService.section(code);
        this.adminView.showKeepExistingInfoHint();

        String name = this.console.ask("Tên khu [" + section.getName() + "]: ");
        String capacityText = this.console.ask("Số ghế [" + section.getCapacity() + "]: ");
        String priceText = this.console.ask("Giá vé [" + section.getBasePrice() + "]: ");
        String activeText = this.console.ask("Hoạt động (1/0) [" + (section.getActive() ? "1" : "0") + "]: ");

        if (name.isEmpty()) {
            name = section.getName();
        }
        int capacity = capacityText.isEmpty() ? section.getCapacity() : Integer.parseInt(capacityText);
        long basePrice = priceText.isEmpty() ? section.getBasePrice()
                : Long.parseLong(priceText.replace(".", "").replace(",", ""));

        boolean active = section.getActive();
        if (!activeText.isEmpty()) {
            if (!activeText.equals("1") && !activeText.equals("0")) {
                this.console.error("Trạng thái hoạt động phải là 1 hoặc 0.");
                return;
            }
            active = activeText.equals("1");
        }

        this.adminService.editSection(adminId, code, name, capacity, basePrice, active);
        this.console.success("Đã cập nhật khán đài " + code + ".");
    }

    private void updateTicketPrice(int adminId) {
        this.adminView.showEvents(this.eventService.listAll(adminId));
        int matchId = this.console.askInt("Mã trận: ");
        this.adminView.showSections(this.eventService.sections());
        String sectionCode = this.console.ask("Mã khán đài: ");
        long newPrice = this.console.askLong("Giá vé mới (VND): ");

        this.eventService.updatePrice(adminId, matchId, sectionCode, newPrice);
        this.console.success("Đã thay giá vé cho lượt đặt mới của trận #" + matchId);
    }

    // ========== QUẢN LÝ FAN ==========

    private void searchFans(int adminId) {
        String keyword = this.console.ask("Tìm theo tên hoặc tài khoản: ");
        this.adminView.showFans(this.adminService.fans(adminId, keyword));
    }

    private void viewFanTickets(int adminId) {
        this.adminView.showFans(this.adminService.fans(adminId, ""));
        int fanId = this.console.askInt("Mã Fan muốn xem: ");
        User fan = this.authService.getById(fanId);

        if (fan.getRole() != Role.FAN) {
            throw new IllegalArgumentException("Người dùng này không phải Fan.");
        }

        this.fanView.showProfile(fan);
        this.showOrders(this.adminService.fanOrders(adminId, fanId), false);
    }

    private void changeFanStatus(int adminId) {
        this.adminView.showFans(this.adminService.fans(adminId, ""));
        int fanId = this.console.askInt("Mã Fan: ");
        int choice = this.console.askInt("1 = Mở khóa, 2 = Khóa: ");

        if (choice != 1 && choice != 2) {
            this.console.error("Vui lòng chọn 1 hoặc 2.");
            return;
        }

        boolean active = choice == 1;
        String action = active ? "Mở khóa" : "Khóa";
        if (this.console.confirm(action + " Fan #" + fanId + "?")) {
            this.adminService.setFanActive(adminId, fanId, active);
            this.console.success("Đã cập nhật trạng thái Fan.");
        }
    }

    private void resetFanPassword(int adminId) {
        this.adminView.showFans(this.adminService.fans(adminId, ""));
        int fanId = this.console.askInt("Mã Fan cần đặt lại mật khẩu: ");
        String newPassword = this.console.askPassword("Mật khẩu tạm mới: ");

        if (this.console.confirm("Đặt lại mật khẩu Fan #" + fanId + "?")) {
            this.adminService.resetFanPassword(adminId, fanId, newPassword);
            this.console.success("Đã đặt lại mật khẩu Fan.");
        }
    }

    // ========== QUẢN LÝ VÉ VÀ THANH TOÁN ==========

    private void viewSeatMap(int adminId) {
        this.adminView.showEvents(this.eventService.listAll(adminId));
        int matchId = this.console.askInt("Mã trận muốn xem sơ đồ: ");
        Event match = this.eventService.find(matchId);

        List<Section> sections = this.eventService.sections();
        this.fanView.showSeatMap(match, sections,
                this.bookingService.seatMapAdmin(adminId, matchId), this.eventService.pricesForSections(match, sections));
    }

    private void showOrders(List<Order> orders, boolean adminMode) {
        Map<Integer, Event> eventsById = new HashMap<Integer, Event>();
        for (Order order : orders) {
            eventsById.put(order.getEventId(), this.eventService.find(order.getEventId()));
        }
        this.adminView.showOrders(orders, eventsById, adminMode);
    }

    private void refundOrder(int adminId) {
        this.showOrders(this.adminService.allOrders(adminId), true);
        int orderId = this.console.askInt("Mã đơn cần hoàn tiền: ");

        if (this.console.confirm("Hoàn tiền DEMO cho đơn #" + orderId + "?")) {
            this.adminService.refundOrder(adminId, orderId);
            this.console.success("Đã hoàn tiền mô phỏng và giải phóng ghế.");
        }
    }

    private void manageHeldSeats(int adminId) {
        this.adminView.showHolds(this.adminService.allHolds(adminId));
        int holdId = this.console.askInt("Mã lượt giữ ghế cần hủy (0 = quay lại): ");

        if (holdId == 0) {
            return;
        }
        if (this.console.confirm("Giải phóng lượt giữ ghế #" + holdId + "?")) {
            this.adminService.releaseHold(adminId, holdId);
            this.console.success("Đã giải phóng ghế.");
        }
    }

    private void changeAdminPassword(int adminId) {
        String oldPassword = this.console.askPassword("Mật khẩu Admin hiện tại: ");
        String newPassword = this.console.askPassword("Mật khẩu mới: ");

        this.authService.changePassword(adminId, oldPassword, newPassword);
        this.console.success("Đổi mật khẩu Admin thành công.");
    }
}
