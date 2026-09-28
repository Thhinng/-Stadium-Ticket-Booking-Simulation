package com.stadium.test;

import com.stadium.controller.BookingController;
import com.stadium.model.Booking;
import com.stadium.model.BookingStatus;
import com.stadium.model.Ticket;
import com.stadium.model.TicketStatus;
import com.stadium.repository.BookingRepository;

import java.util.Arrays;
import java.util.List;

/**
 * Automated Verification Test for Member 3 (Lê Bách): Booking & Payment Module.
 */
public class BookingModuleTest {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  KIỂM THỬ TỰ ĐỘNG KHÓA VÙNG THÀNH VIÊN 3 (LÊ BÁCH)");
        System.out.println("=================================================");

        BookingRepository repository = new BookingRepository();
        BookingController controller = new BookingController(repository);
        String userId = "TEST_USER_BACH";
        String matchId = "M001";

        // Test Case 1: Đặt vé & Giữ chỗ (Hold Seat)
        System.out.println("\n[TEST 1] Thực hiện Use Case Đặt vé & Giữ chỗ...");
        List<String> seatsToBook = Arrays.asList("M001-SVIP-A1", "M001-SVIP-A2");
        BookingController.BookingResult bookingResult = controller.createBooking(userId, matchId, seatsToBook);
        assert bookingResult.isSuccess() : "Đặt vé thất bại!";
        Booking booking = bookingResult.getBooking();
        System.out.println(" -> Kết quả: " + bookingResult.getMessage());
        System.out.println(" -> Trạng thái Đơn hàng: " + booking.getStatus().getDescription());
        System.out.println(" -> Thời gian đếm ngược TTL: " + booking.getFormattedRemainingTime());

        // Test Case 2: Chống Đặt trùng ghế (Anti-Double Booking)
        System.out.println("\n[TEST 2] Kiểm tra Chống giữ trùng ghế (Anti-Double Booking)...");
        BookingController.BookingResult duplicateResult = controller.createBooking("OTHER_USER", matchId, seatsToBook);
        System.out.println(" -> Đặt lại ghế đã giữ: " + duplicateResult.getMessage());
        assert !duplicateResult.isSuccess() : "Lỗi: Hệ thống cho phép đặt trùng ghế!";

        // Test Case 3: Ràng buộc số lượng vé tối đa (Max 4 tickets)
        System.out.println("\n[TEST 3] Kiểm tra Ràng buộc số lượng vé tối đa (> 4 vé)...");
        List<String> tooManySeats = Arrays.asList("M001-VIP-B1", "M001-VIP-B2", "M001-VIP-B3", "M001-VIP-B4", "M001-VIP-B5");
        BookingController.BookingResult overLimitResult = controller.createBooking(userId, matchId, tooManySeats);
        System.out.println(" -> Kết quả chọn 5 vé: " + overLimitResult.getMessage());
        assert !overLimitResult.isSuccess() : "Lỗi: Hệ thống không chặn quá số lượng vé tối đa!";

        // Test Case 4: Thanh toán vé (Payment)
        System.out.println("\n[TEST 4] Thực hiện Use Case Thanh toán vé qua VNPay QR...");
        BookingController.PaymentResult payResult = controller.processPayment(booking.getBookingId(), "VNPay QR", "TXN_OK");
        assert payResult.isSuccess() : "Thanh toán thất bại!";
        System.out.println(" -> Kết quả: " + payResult.getMessage());
        System.out.println(" -> Mã Giao dịch: " + payResult.getBooking().getTransactionId());
        System.out.println(" -> Trạng thái Đơn hàng: " + payResult.getBooking().getStatus().getDescription());

        // Test Case 5: Xem vé đã mua & Mã QR
        System.out.println("\n[TEST 5] Thực hiện Use Case Xem vé đã mua...");
        List<Ticket> userTickets = controller.getUserTickets(userId);
        System.out.println(" -> Số lượng vé của user: " + userTickets.size());
        for (Ticket t : userTickets) {
            System.out.println("    + Vé: " + t.getTicketId() + " | Trạng thái: " + t.getStatus().getDescription());
            System.out.println("      Mã QR: " + t.getQrCodeData());
            assert t.getStatus() == TicketStatus.PAID : "Trạng thái vé chưa được chuyển sang PAID!";
        }

        // Test Case 6: Trả vé & Hoàn tiền (Refund)
        System.out.println("\n[TEST 6] Thực hiện Use Case Trả vé & Hoàn tiền...");
        BookingController.BookingResult refundResult = controller.refundBooking(booking.getBookingId(), userId);
        assert refundResult.isSuccess() : "Trả vé thất bại!";
        System.out.println(" -> Kết quả: " + refundResult.getMessage());
        System.out.println(" -> Trạng thái Đơn hàng sau khi trả vé: " + refundResult.getBooking().getStatus().getDescription());

        // Verify seats are returned to AVAILABLE
        for (String seatId : seatsToBook) {
            assert repository.getSeat(seatId).getStatus() == TicketStatus.AVAILABLE : "Ghế chưa được trả về AVAILABLE!";
        }

        // Test Case 7: Hủy vé chưa thanh toán (Cancel Pending Booking)
        System.out.println("\n[TEST 7] Thực hiện Use Case Hủy đơn đặt vé chưa thanh toán...");
        BookingController.BookingResult b2Result = controller.createBooking(userId, matchId, Arrays.asList("M001-Standard-C1"));
        Booking b2 = b2Result.getBooking();
        BookingController.BookingResult cancelResult = controller.cancelBooking(b2.getBookingId(), userId);
        assert cancelResult.isSuccess() : "Hủy vé thất bại!";
        System.out.println(" -> Kết quả: " + cancelResult.getMessage());

        System.out.println("\n=================================================");
        System.out.println(" [SUCCESS] TẤT CẢ 7 TEST CASE CỦA LÊ BÁCH ĐÃ ĐẠT!");
        System.out.println("=================================================");
    }
}
