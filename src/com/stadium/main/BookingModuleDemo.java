package com.stadium.main;

import com.stadium.controller.BookingController;
import com.stadium.model.Booking;
import com.stadium.model.Match;
import com.stadium.model.Seat;
import com.stadium.model.Ticket;
import com.stadium.repository.BookingRepository;
import com.stadium.view.BookingView;
import com.stadium.view.PaymentView;
import com.stadium.view.UserTicketsView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Main Demo Application for Member 3: Lê Bách
 * Module: Nghiệp vụ Đặt vé & Thanh toán (Booking & Payment)
 */
public class BookingModuleDemo {
    private static final String DEFAULT_USER_ID = "USER_LEBACH";
    private static ScheduledExecutorService backgroundCleaner;

    public static void main(String[] args) {
        BookingRepository repository = new BookingRepository();
        BookingController controller = new BookingController(repository);

        // Start background auto-cleanup for expired bookings (every 3 seconds)
        backgroundCleaner = Executors.newSingleThreadScheduledExecutor();
        backgroundCleaner.scheduleAtFixedRate(() -> {
            try {
                controller.releaseExpiredBookings();
            } catch (Exception ignored) {}
        }, 3, 3, TimeUnit.SECONDS);

        Scanner scanner = new Scanner(System.in);
        System.out.println("=======================================================================");
        System.out.println(" HỆ THỐNG ĐẶT VÉ SÂN VẬN ĐỘNG - DEMO PHÂN VÙNG THÀNH VIÊN 3 (LÊ BÁCH)");
        System.out.println("=======================================================================");
        System.out.println(" Tài khoản dùng thử: " + DEFAULT_USER_ID);

        boolean running = true;
        while (running) {
            printMenu();
            System.out.print(" Chọn chức năng (0-6): ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    handleViewSeats(repository);
                    break;
                case "2":
                    handleBooking(controller, repository, scanner);
                    break;
                case "3":
                    handlePayment(controller, repository, scanner);
                    break;
                case "4":
                    handleViewUserTickets(controller);
                    break;
                case "5":
                    handleCancelBooking(controller, scanner);
                    break;
                case "6":
                    handleRefundTicket(controller, scanner);
                    break;
                case "0":
                    running = false;
                    System.out.println("\nCảm ơn bạn đã sử dụng hệ thống! Đang đóng ứng dụng...");
                    break;
                default:
                    System.out.println("\n[!] Lựa chọn không hợp lệ, vui lòng thử lại.");
            }
        }

        backgroundCleaner.shutdown();
        scanner.close();
    }

    private static void printMenu() {
        System.out.println("\n-----------------------------------------------------------------------");
        System.out.println("                           MENU CHỨC NĂNG                              ");
        System.out.println("-----------------------------------------------------------------------");
        System.out.println(" 1. Xem sơ đồ ghế & Trạng thái ghế các trận đấu");
        System.out.println(" 2. Đặt vé & Giữ chỗ tạm thời (Khóa ghế với TTL 5 phút, tối đa 4 vé)");
        System.out.println(" 3. Thanh toán đơn đặt vé (VNPay QR / MoMo / Thẻ Visa)");
        System.out.println(" 4. Xem lịch sử đơn đặt vé & Vé điện tử cá nhân (QR Barcode)");
        System.out.println(" 5. Hủy đơn đặt vé chưa thanh toán");
        System.out.println(" 6. Trả vé & Yêu cầu hoàn tiền (Đối với vé đã thanh toán)");
        System.out.println(" 0. Thoát chương trình");
        System.out.println("-----------------------------------------------------------------------");
    }

    private static void handleViewSeats(BookingRepository repository) {
        List<Match> matches = repository.getAllMatches();
        System.out.println("\n--- DANH SÁCH TRẬN ĐẤU ĐANG MỞ BÁN ---");
        for (int i = 0; i < matches.size(); i++) {
            System.out.println((i + 1) + ". [" + matches.get(i).getMatchId() + "] " + matches.get(i).getTitle());
        }
        String matchId = matches.get(0).getMatchId();
        List<Seat> seats = repository.getSeatsForMatch(matchId);
        BookingView.displaySeatMap(seats);
    }

    private static void handleBooking(BookingController controller, BookingRepository repository, Scanner scanner) {
        List<Match> matches = repository.getAllMatches();
        System.out.println("\n--- CHỌN TRẬN ĐẤU ĐỂ ĐẶT VÉ ---");
        for (int i = 0; i < matches.size(); i++) {
            System.out.println((i + 1) + ". [" + matches.get(i).getMatchId() + "] " + matches.get(i).getTitle());
        }
        System.out.print("Nhập Mã Trận Đấu (Mặc định: M001): ");
        String matchId = scanner.nextLine().trim();
        if (matchId.isEmpty()) matchId = "M001";

        List<Seat> availableSeats = repository.getSeatsForMatch(matchId);
        BookingView.displaySeatMap(availableSeats);

        System.out.print("Nhập các Mã Ghế muốn đặt (cách nhau bởi dấu phẩy, ví dụ: M001-SVIP-A1, M001-SVIP-A2): ");
        String inputSeats = scanner.nextLine().trim();
        if (inputSeats.isEmpty()) {
            System.out.println("[!] Chưa nhập mã ghế.");
            return;
        }

        String[] rawSeats = inputSeats.split(",");
        List<String> seatIds = new ArrayList<>();
        for (String s : rawSeats) {
            if (!s.trim().isEmpty()) {
                seatIds.add(s.trim());
            }
        }

        System.out.println("\n[SYSTEM] Đang xử lý kiểm tra chống giữ trùng ghế & khóa ghế tạm thời...");
        BookingController.BookingResult result = controller.createBooking(DEFAULT_USER_ID, matchId, seatIds);

        System.out.println("\n" + (result.isSuccess() ? "[THÀNH CÔNG] " : "[THẤT BẠI] ") + result.getMessage());
        if (result.isSuccess()) {
            BookingView.displayBookingConfirmation(result.getBooking());
        }
    }

    private static void handlePayment(BookingController controller, BookingRepository repository, Scanner scanner) {
        List<Booking> userBookings = controller.getUserBookings(DEFAULT_USER_ID);
        List<Booking> pendingBookings = new ArrayList<>();
        for (Booking b : userBookings) {
            if (b.getStatus() == com.stadium.model.BookingStatus.PENDING_PAYMENT && !b.isExpired()) {
                pendingBookings.add(b);
            }
        }

        if (pendingBookings.isEmpty()) {
            System.out.println("\n[!] Bạn không có đơn đặt vé nào đang chờ thanh toán.");
            return;
        }

        System.out.println("\n--- CÁC ĐƠN ĐẶT VÉ CHỜ THANH TOÁN ---");
        for (Booking b : pendingBookings) {
            System.out.printf("- Mã Đơn: %s | Trận: %s | Tổng: %,.0f VNĐ | TTL Còn lại: %s%n",
                    b.getBookingId(), b.getMatchName(), b.getTotalAmount(), b.getFormattedRemainingTime());
        }

        System.out.print("\nNhập Mã Đơn Hàng cần thanh toán: ");
        String bookingId = scanner.nextLine().trim();

        Booking targetBooking = controller.getBookingDetails(bookingId);
        if (targetBooking == null) {
            System.out.println("[!] Không tìm thấy đơn hàng " + bookingId);
            return;
        }

        PaymentView.displayPaymentMethods(targetBooking);
        System.out.print("Chọn phương thức (1-VNPay QR, 2-MoMo, 3-Visa Card): ");
        String pChoice = scanner.nextLine().trim();
        String method = "VNPay QR";
        if ("2".equals(pChoice)) method = "Ví MoMo";
        else if ("3".equals(pChoice)) method = "Thẻ Visa/Mastercard";

        System.out.println("[SYSTEM] Đang kết nối cổng thanh toán " + method + "...");
        BookingController.PaymentResult pResult = controller.processPayment(bookingId, method, "OK");

        System.out.println("\n" + (pResult.isSuccess() ? "[THÀNH CÔNG] " : "[THẤT BẠI] ") + pResult.getMessage());
        if (pResult.isSuccess()) {
            PaymentView.displayReceipt(pResult.getBooking());
        }
    }

    private static void handleViewUserTickets(BookingController controller) {
        List<Booking> bookings = controller.getUserBookings(DEFAULT_USER_ID);
        UserTicketsView.displayUserBookings(bookings);

        List<Ticket> tickets = controller.getUserTickets(DEFAULT_USER_ID);
        UserTicketsView.displayUserTickets(tickets);

        for (Ticket t : tickets) {
            if (t.getStatus() == com.stadium.model.TicketStatus.PAID) {
                UserTicketsView.displayTicketDigitalDetails(t);
            }
        }
    }

    private static void handleCancelBooking(BookingController controller, Scanner scanner) {
        List<Booking> bookings = controller.getUserBookings(DEFAULT_USER_ID);
        UserTicketsView.displayUserBookings(bookings);

        System.out.print("Nhập Mã Đơn Hàng muốn HỦY: ");
        String bookingId = scanner.nextLine().trim();

        BookingController.BookingResult result = controller.cancelBooking(bookingId, DEFAULT_USER_ID);
        System.out.println("\n" + (result.isSuccess() ? "[THÀNH CÔNG] " : "[THẤT BẠI] ") + result.getMessage());
    }

    private static void handleRefundTicket(BookingController controller, Scanner scanner) {
        List<Booking> bookings = controller.getUserBookings(DEFAULT_USER_ID);
        UserTicketsView.displayUserBookings(bookings);

        System.out.print("Nhập Mã Đơn Hàng đã thanh toán muốn TRẢ VÉ & HOÀN TIỀN: ");
        String bookingId = scanner.nextLine().trim();

        BookingController.BookingResult result = controller.refundBooking(bookingId, DEFAULT_USER_ID);
        System.out.println("\n" + (result.isSuccess() ? "[THÀNH CÔNG] " : "[THẤT BẠI] ") + result.getMessage());
    }
}
