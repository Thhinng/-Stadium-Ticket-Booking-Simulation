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
        console = new ConsoleView();
        authView = new AuthView(console);
        FanView fanView = new FanView(console);

        authController = new AuthController(authService, authView, console);
        guestController = new GuestController(eventService, new GuestView(console), console);
        fanController = new FanController(authService, eventService, bookingService, fanView, console);
        adminController = new AdminController(authService, eventService, bookingService,
                adminService, new AdminView(console), fanView, console);
    }

    public void run() {
        while (true) {
            authView.showWelcome();
            int choice = console.askInt("Chọn chức năng > ");

            try {
                switch (choice) {
                    case 1:
                        guestController.run();
                        break;
                    case 2:
                        User fan = authController.login(Role.FAN);
                        fanController.run(fan);
                        break;
                    case 3:
                        authController.registerFan();
                        break;
                    case 4:
                        User admin = authController.login(Role.ADMIN);
                        adminController.run(admin);
                        break;
                    case 0:
                        console.info("Cảm ơn bạn đã sử dụng Stadium Ticketing!");
                        return;
                    default:
                        console.error("Vui lòng chọn chức năng có trong menu.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                console.error(e.getMessage());
            }
        }
    }
}
