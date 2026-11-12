package controller;

import model.Role;
import model.User;
import service.AuthService;
import view.AuthView;
import view.ConsoleView;

/** Xử lý đăng ký và đăng nhập; phần kiểm tra tài khoản nằm ở AuthService. */
public class AuthController {
    private final AuthService authService;
    private final AuthView authView;
    private final ConsoleView console;

    public AuthController(AuthService authService, AuthView authView, ConsoleView console) {
        this.authService = authService;
        this.authView = authView;
        this.console = console;
    }

    public User login(Role role) {
        String[] information = this.authView.readLogin(role.name());
        String username = information[0];
        String password = information[1];

        User user = this.authService.login(username, password, role);
        this.console.success("Đăng nhập " + role + " thành công: " + user.getName());
        return user;
    }

    public void registerFan() {
        String[] information = this.authView.readRegistration();
        String username = information[0];
        String fullName = information[1];
        String password = information[2];

        User user = this.authService.register(username, fullName, password);
        this.console.success("Đăng ký Fan #" + user.getId() + " thành công. Chọn mục 2 để đăng nhập.");
    }
}
