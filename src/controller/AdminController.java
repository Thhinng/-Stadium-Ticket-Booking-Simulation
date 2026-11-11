package controller;

import model.Event;
import model.Role;
import model.Section;
import model.User;
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
        int adminId = admin.id;

        while (true) {
            try {
                authService.requireActive(adminId, Role.ADMIN);
                adminView.showMenu();
                int choice = console.askInt("Admin > ");

                if (choice == 0) {
                    console.info("Đã đăng xuất Admin.");
                    return;
                }

                handleChoice(choice, adminId);
            } catch (IllegalArgumentException | IllegalStateException e) {
                console.error(e.getMessage());
            }
        }
    }

    private void handleChoice(int choice, int adminId) {
        switch (choice) {
            case 1:
                adminView.showDashboard(adminService.dashboard(adminId));
                break;
            case 2:
                adminView.showEvents(eventService.listAll(adminId));
                break;
            case 3:
                createMatch(adminId);
                break;
            case 4:
                editMatch(adminId);
                break;
            case 5:
                changeSaleStatus(adminId);
                break;
            case 6:
                cancelMatch(adminId);
                break;
            case 7:
                adminView.showSections(eventService.sections());
                break;
            case 8:
                createSection(adminId);
                break;
            case 9:
                editSection(adminId);
                break;
            case 10:
                updateTicketPrice(adminId);
                break;
            case 11:
                adminView.showFans(adminService.fans(adminId, ""));
                break;
            case 12:
                searchFans(adminId);
                break;
            case 13:
                viewFanTickets(adminId);
                break;
            case 14:
                changeFanStatus(adminId);
                break;
            case 15:
                resetFanPassword(adminId);
                break;
            case 16:
                adminView.showOrders(adminService.allOrders(adminId), eventService, true);
                break;
            case 17:
                viewSeatMap(adminId);
                break;
            case 18:
                refundOrder(adminId);
                break;
            case 19:
                adminView.showPayments(adminService.allPayments(adminId));
                break;
            case 20:
                manageHeldSeats(adminId);
                break;
            case 21:
                changeAdminPassword(adminId);
                break;
            default:
                console.error("Vui lòng chọn chức năng có trong menu.");
        }
    }

    // ========== QUẢN LÝ TRẬN ĐẤU ==========

    private void createMatch(int adminId) {
        console.title("TẠO TRẬN ĐẤU");
        String homeTeam = console.ask("Đội chủ nhà: ");
        String awayTeam = console.ask("Đội khách: ");
        String competition = console.ask("Giải đấu: ");
        String time = console.ask("Giờ đá (dd/MM/yyyy HH:mm): ");
        long kickoff = TimeUtil.parse(time);

        Event match = eventService.add(adminId, homeTeam, awayTeam, competition, kickoff);
        console.success("Đã tạo trận đấu #" + match.id + " và mở bán vé.");
    }

    private void editMatch(int adminId) {
        adminView.showEvents(eventService.listAll(adminId));
        int matchId = console.askInt("Mã trận cần sửa: ");
        Event match = eventService.find(matchId);
        System.out.println("Nhấn Enter để giữ nguyên thông tin.");

        String homeTeam = console.ask("Đội chủ nhà [" + match.home + "]: ");
        String awayTeam = console.ask("Đội khách [" + match.away + "]: ");
        String competition = console.ask("Giải đấu [" + match.competition + "]: ");
        String time = console.ask("Thời gian [" + TimeUtil.format(match.startsAt) + "]: ");

        if (homeTeam.isEmpty()) {
            homeTeam = match.home;
        }
        if (awayTeam.isEmpty()) {
            awayTeam = match.away;
        }
        if (competition.isEmpty()) {
            competition = match.competition;
        }

        long kickoff = time.isEmpty() ? match.startsAt : TimeUtil.parse(time);
        eventService.edit(adminId, matchId, homeTeam, awayTeam, competition, kickoff);
        console.success("Đã cập nhật trận đấu #" + matchId);
    }

    private void changeSaleStatus(int adminId) {
        adminView.showEvents(eventService.listAll(adminId));
        int matchId = console.askInt("Mã trận: ");
        int choice = console.askInt("1 = Mở bán, 2 = Đóng bán: ");

        if (choice != 1 && choice != 2) {
            console.error("Vui lòng chọn 1 hoặc 2.");
            return;
        }

        eventService.setSaleStatus(adminId, matchId, choice == 1);
        console.success("Đã cập nhật trạng thái bán vé.");
    }

    private void cancelMatch(int adminId) {
        adminView.showEvents(eventService.listAll(adminId));
        int matchId = console.askInt("Mã trận cần hủy: ");
        Event match = eventService.find(matchId);

        if (console.confirm("Hủy trận " + match.title() + " và hoàn vé DEMO?")) {
            int refundedOrders = adminService.cancelEvent(adminId, matchId);
            console.success("Đã hủy trận và hoàn tiền mô phỏng " + refundedOrders + " đơn.");
        }
    }

    // ========== QUẢN LÝ KHÁN ĐÀI VÀ GIÁ VÉ ==========

    private void createSection(int adminId) {
        adminView.showSections(eventService.sections());
        String code = console.ask("Mã khu mới (ví dụ D): ");
        String name = console.ask("Tên khu: ");
        int capacity = console.askInt("Sức chứa (1-99 ghế): ");
        long basePrice = console.askLong("Giá cơ bản (VND): ");

        Section section = adminService.addSection(adminId, code, name, capacity, basePrice);
        console.success("Đã tạo khán đài " + section.code + ".");
    }

    private void editSection(int adminId) {
        adminView.showSections(eventService.sections());
        String code = console.ask("Mã khu cần sửa: ");
        Section section = eventService.section(code);
        System.out.println("Nhấn Enter để giữ nguyên thông tin.");

        String name = console.ask("Tên khu [" + section.name + "]: ");
        String capacityText = console.ask("Số ghế [" + section.capacity + "]: ");
        String priceText = console.ask("Giá vé [" + section.basePrice + "]: ");
        String activeText = console.ask("Hoạt động (1/0) [" + (section.active ? "1" : "0") + "]: ");

        if (name.isEmpty()) {
            name = section.name;
        }
        int capacity = capacityText.isEmpty() ? section.capacity : Integer.parseInt(capacityText);
        long basePrice = priceText.isEmpty() ? section.basePrice
                : Long.parseLong(priceText.replace(".", "").replace(",", ""));

        boolean active = section.active;
        if (!activeText.isEmpty()) {
            if (!activeText.equals("1") && !activeText.equals("0")) {
                console.error("Trạng thái hoạt động phải là 1 hoặc 0.");
                return;
            }
            active = activeText.equals("1");
        }

        adminService.editSection(adminId, code, name, capacity, basePrice, active);
        console.success("Đã cập nhật khán đài " + code + ".");
    }

    private void updateTicketPrice(int adminId) {
        adminView.showEvents(eventService.listAll(adminId));
        int matchId = console.askInt("Mã trận: ");
        adminView.showSections(eventService.sections());
        String sectionCode = console.ask("Mã khán đài: ");
        long newPrice = console.askLong("Giá vé mới (VND): ");

        eventService.updatePrice(adminId, matchId, sectionCode, newPrice);
        console.success("Đã thay giá vé cho lượt đặt mới của trận #" + matchId);
    }

    // ========== QUẢN LÝ FAN ==========

    private void searchFans(int adminId) {
        String keyword = console.ask("Tìm theo tên hoặc tài khoản: ");
        adminView.showFans(adminService.fans(adminId, keyword));
    }

    private void viewFanTickets(int adminId) {
        adminView.showFans(adminService.fans(adminId, ""));
        int fanId = console.askInt("Mã Fan muốn xem: ");
        User fan = authService.getById(fanId);

        if (fan.role != Role.FAN) {
            throw new IllegalArgumentException("Người dùng này không phải Fan.");
        }

        fanView.showProfile(fan);
        adminView.showOrders(adminService.fanOrders(adminId, fanId), eventService, false);
    }

    private void changeFanStatus(int adminId) {
        adminView.showFans(adminService.fans(adminId, ""));
        int fanId = console.askInt("Mã Fan: ");
        int choice = console.askInt("1 = Mở khóa, 2 = Khóa: ");

        if (choice != 1 && choice != 2) {
            console.error("Vui lòng chọn 1 hoặc 2.");
            return;
        }

        boolean active = choice == 1;
        String action = active ? "Mở khóa" : "Khóa";
        if (console.confirm(action + " Fan #" + fanId + "?")) {
            adminService.setFanActive(adminId, fanId, active);
            console.success("Đã cập nhật trạng thái Fan.");
        }
    }

    private void resetFanPassword(int adminId) {
        adminView.showFans(adminService.fans(adminId, ""));
        int fanId = console.askInt("Mã Fan cần đặt lại mật khẩu: ");
        String newPassword = console.askPassword("Mật khẩu tạm mới: ");

        if (console.confirm("Đặt lại mật khẩu Fan #" + fanId + "?")) {
            adminService.resetFanPassword(adminId, fanId, newPassword);
            console.success("Đã đặt lại mật khẩu Fan.");
        }
    }

    // ========== QUẢN LÝ VÉ VÀ THANH TOÁN ==========

    private void viewSeatMap(int adminId) {
        adminView.showEvents(eventService.listAll(adminId));
        int matchId = console.askInt("Mã trận muốn xem sơ đồ: ");
        Event match = eventService.find(matchId);

        fanView.showSeatMap(match, eventService.sections(),
                bookingService.seatMapAdmin(adminId, matchId), eventService);
    }

    private void refundOrder(int adminId) {
        adminView.showOrders(adminService.allOrders(adminId), eventService, true);
        int orderId = console.askInt("Mã đơn cần hoàn tiền: ");

        if (console.confirm("Hoàn tiền DEMO cho đơn #" + orderId + "?")) {
            adminService.refundOrder(adminId, orderId);
            console.success("Đã hoàn tiền mô phỏng và giải phóng ghế.");
        }
    }

    private void manageHeldSeats(int adminId) {
        adminView.showHolds(adminService.allHolds(adminId));
        int holdId = console.askInt("Mã lượt giữ ghế cần hủy (0 = quay lại): ");

        if (holdId == 0) {
            return;
        }
        if (console.confirm("Giải phóng lượt giữ ghế #" + holdId + "?")) {
            adminService.releaseHold(adminId, holdId);
            console.success("Đã giải phóng ghế.");
        }
    }

    private void changeAdminPassword(int adminId) {
        String oldPassword = console.askPassword("Mật khẩu Admin hiện tại: ");
        String newPassword = console.askPassword("Mật khẩu mới: ");

        authService.changePassword(adminId, oldPassword, newPassword);
        console.success("Đổi mật khẩu Admin thành công.");
    }
}
