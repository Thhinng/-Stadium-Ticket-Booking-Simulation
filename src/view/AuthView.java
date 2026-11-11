package view;

public class AuthView {
    private final ConsoleView ui;
    public AuthView(ConsoleView ui) { this.ui = ui; }

    public void showWelcome() {
        ui.title("STADIUM TICKETING - HỆ THỐNG QUẢN LÝ VÉ SÂN VẬN ĐỘNG");
        System.out.println("1. Vào sảnh với tư cách GUEST (không cần tài khoản)");
        System.out.println("2. Đăng nhập FAN");
        System.out.println("3. Đăng ký FAN");
        System.out.println("4. Đăng nhập ADMIN");
        System.out.println("0. Thoát");
    }

    public String[] readLogin(String role) {
        ui.title("ĐĂNG NHẬP " + role);
        String username = ui.ask("Tên đăng nhập: ");
        String password = ui.askPassword("Mật khẩu: ");
        return new String[]{username, password};
    }

    public String[] readRegistration() {
        ui.title("ĐĂNG KÝ TÀI KHOẢN FAN");
        System.out.println("Tên đăng nhập: 3-24 ký tự, a-z, 0-9, dấu _; mật khẩu ít nhất 6 ký tự.");
        String username = ui.ask("Tên đăng nhập mới: ");
        String name = ui.ask("Họ và tên: ");
        String password = ui.askPassword("Mật khẩu mới: ");
        String confirm = ui.askPassword("Nhập lại mật khẩu: ");
        if (!password.equals(confirm)) throw new IllegalArgumentException("Hai lần nhập mật khẩu không khớp.");
        return new String[]{username, name, password};
    }
}
