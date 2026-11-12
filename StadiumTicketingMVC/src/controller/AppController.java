package controller;

import model.Role;
import model.User;
import service.AdminService;
import service.AuthService;
import service.BookingService;
import service.EventService;
import view.AdminView;
import view.AuthView;
import view.ConsoleView;
import view.FanView;
import view.GuestView;

/** Điều hướng từ menu chính đến các chức năng dành cho từng loại người dùng. */
public class AppController {
    private final ConsoleView console;
    private final AuthView authView;
    private final AuthController authController;
    private final GuestController guestController;
    private final FanController fanController;
    private final AdminController adminController;

    public AppController(AuthService authService, EventService eventService,
                         BookingService bookingService, AdminService adminService) {
        this.console = new ConsoleView();
        this.authView = new AuthView(this.console);
        FanView fanView = new FanView(this.console);

        this.authController = new AuthController(authService, this.authView, this.console);
        this.guestController = new GuestController(eventService, new GuestView(this.console), this.console);
        this.fanController = new FanController(authService, eventService, bookingService, fanView, this.console);
        this.adminController = new AdminController(authService, eventService, bookingService,
                adminService, new AdminView(this.console), fanView, this.console);
    }

    public void run() {
        while (true) {
            this.authView.showWelcome();
            int choice = this.console.askInt("Chọn chức năng > ");

            try {
                switch (choice) {
                    case 1:
                        this.guestController.run();
                        break;
                    case 2:
                        User fan = this.authController.login(Role.FAN);
                        this.fanController.run(fan);
                        break;
                    case 3:
                        this.authController.registerFan();
                        break;
                    case 4:
                        User admin = this.authController.login(Role.ADMIN);
                        this.adminController.run(admin);
                        break;
                    case 0:
                        this.console.info("Cảm ơn bạn đã sử dụng Stadium Ticketing!");
                        return;
                    default:
                        this.console.error("Vui lòng chọn chức năng có trong menu.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                this.console.error(e.getMessage());
            }
        }
    }
}
