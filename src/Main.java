import controller.AppController;
import repository.FileRepository;
import service.AdminService;
import service.AuthService;
import service.BookingService;
import service.EventService;
import service.Store;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.time.Clock;
import java.util.NoSuchElementException;

public class Main {

    public static void main(String[] args) {
        setupVietnameseConsole();

        try {
            FileRepository repository = new FileRepository(Paths.get("data"));
            Store store = new Store(repository, Clock.systemUTC());

            AuthService authService = new AuthService(store);
            EventService eventService = new EventService(store, authService);
            BookingService bookingService = new BookingService(store, authService, eventService);
            AdminService adminService = new AdminService(store, authService, eventService, bookingService);

            AppController app = new AppController(authService, eventService, bookingService, adminService);
            app.run();
        } catch (NoSuchElementException e) {
            System.out.println("\nĐã đóng chương trình.");
        } catch (Exception e) {
            System.err.println("Không thể chạy ứng dụng: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void setupVietnameseConsole() {
        // Windows PowerShell co the su dung bang ma cu, khong hien dung tieng Viet.
        // Chuyen Terminal sang UTF-8 ngay trong Main, khong can chay file .bat.
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            try {
                Process process = new ProcessBuilder("cmd", "/c", "chcp 65001 >nul")
                        .inheritIO()
                        .start();
                process.waitFor();
            } catch (Exception e) {
                System.err.println("Lưu ý: không thể tự chuyển Terminal sang UTF-8.");
            }
        }

        try {
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, "UTF-8"));
            System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err), true, "UTF-8"));
        } catch (java.io.UnsupportedEncodingException e) {
            throw new IllegalStateException("Java không hỗ trợ UTF-8.", e);
        }
    }
}
