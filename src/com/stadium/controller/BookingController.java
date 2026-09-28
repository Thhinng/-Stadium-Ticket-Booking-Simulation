package com.stadium.controller;

import com.stadium.model.*;
import com.stadium.repository.BookingRepository;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Controller: BookingController (Điều hướng luồng đặt vé, thanh toán, nhả ghế khi hết hạn, hủy và trả vé).
 * Member 3: Lê Bách - Booking & Payment Module
 */
public class BookingController {
    public static final int MAX_TICKETS_PER_TRANSACTION = 4; // Ràng buộc số lượng vé tối đa/giao dịch
    public static final int DEFAULT_HOLD_MINUTES = 5; // Khóa ghế tạm thời (TTL 5 phút)

    private final BookingRepository repository;

    public BookingController(BookingRepository repository) {
        this.repository = repository;
    }

    /**
     * Use Case: Đặt vé & Giữ chỗ tạm thời (TTL Countdown)
     */
    public BookingResult createBooking(String userId, String matchId, List<String> seatIds) {
        // 1. Kiểm tra đầu vào
        if (seatIds == null || seatIds.isEmpty()) {
            return new BookingResult(false, "Vui lòng chọn ít nhất 1 ghế để đặt vé.", null);
        }

        // 2. Ràng buộc tối đa 4 vé/giao dịch (chống phe vé)
        if (seatIds.size() > MAX_TICKETS_PER_TRANSACTION) {
            return new BookingResult(false, 
                String.format("Mỗi lượt đặt vé chỉ được chọn tối đa %d vé. Bạn đã chọn %d vé.", 
                MAX_TICKETS_PER_TRANSACTION, seatIds.size()), null);
        }

        Match match = repository.getMatch(matchId);
        if (match == null) {
            return new BookingResult(false, "Không tìm thấy trận đấu với mã: " + matchId, null);
        }

        // 3. Cơ chế khóa ghế nguyên tử (Anti-Double Booking)
        boolean lockedSuccess = repository.lockSeatsForBooking(seatIds, userId);
        if (!lockedSuccess) {
            return new BookingResult(false, "Một hoặc nhiều ghế bạn chọn đã bị người khác đặt hoặc đang giữ chỗ!", null);
        }

        // 4. Tạo mã Đơn đặt vé (Booking ID) duy nhất
        String bookingId = "BK-" + System.currentTimeMillis() % 1000000;
        Booking booking = new Booking(bookingId, userId, matchId, match.getTitle(), DEFAULT_HOLD_MINUTES);

        // 5. Khởi tạo danh sách vé (Tickets)
        for (String seatId : seatIds) {
            Seat seat = repository.getSeat(seatId);
            String ticketId = "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            
            Ticket ticket = new Ticket(
                ticketId, 
                matchId, 
                match.getTitle(), 
                seat.getSeatId(), 
                seat.getSeatName(), 
                seat.getZone(), 
                seat.getPrice()
            );
            ticket.setStatus(TicketStatus.HOLD);
            booking.addTicket(ticket);
        }

        // 6. Lưu thông tin đơn vị đặt vé
        repository.saveBooking(booking);

        return new BookingResult(true, 
            String.format("Đặt vé thành công! Đơn hàng %s được giữ chỗ trong %d phút. Vui lòng thanh toán trước khi hết hạn.", 
            bookingId, DEFAULT_HOLD_MINUTES), booking);
    }

    /**
     * Use Case: Thanh toán vé (VNPay / Card / MoMo E-Wallet)
     */
    public PaymentResult processPayment(String bookingId, String paymentMethod, String paymentAccountDetails) {
        // Tự động giải phóng các ghế hết hạn trước khi xử lý
        releaseExpiredBookings();

        Booking booking = repository.getBooking(bookingId);
        if (booking == null) {
            return new PaymentResult(false, "Không tìm thấy đơn đặt vé: " + bookingId, null);
        }

        if (booking.getStatus() == BookingStatus.PAID) {
            return new PaymentResult(false, "Đơn đặt vé này đã được thanh toán thành công trước đó!", booking);
        }

        if (booking.getStatus() == BookingStatus.EXPIRED) {
            return new PaymentResult(false, "Đơn đặt vé đã hết thời hạn giữ chỗ và bị hủy tự động!", booking);
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return new PaymentResult(false, "Đơn đặt vé đã bị hủy!", booking);
        }

        if (booking.isExpired()) {
            // Nhả ghế ngay
            booking.setStatus(BookingStatus.EXPIRED);
            for (Ticket t : booking.getTickets()) {
                t.setStatus(TicketStatus.CANCELLED);
            }
            List<String> seatIds = new ArrayList<>();
            for (Ticket t : booking.getTickets()) {
                seatIds.add(t.getSeatId());
            }
            repository.releaseSeats(seatIds);
            repository.saveBooking(booking);

            return new PaymentResult(false, "Hết thời gian giữ chỗ! Ghế đã được nhả lại cho hệ thống.", booking);
        }

        // Giả lập xử lý cổng thanh toán thành công
        String transactionId = "TXN-" + System.currentTimeMillis();
        LocalDateTime now = LocalDateTime.now();

        booking.setStatus(BookingStatus.PAID);
        booking.setPaymentMethod(paymentMethod);
        booking.setPaymentTime(now);
        booking.setTransactionId(transactionId);

        // Cập nhật trạng thái từng vé và tạo mã QR điện tử duy nhất
        for (Ticket ticket : booking.getTickets()) {
            ticket.setStatus(TicketStatus.PAID);
            ticket.setPurchaseTime(now);
            
            // Generate digital QR barcode payload for stadium entry gate
            String qrPayload = String.format("STADIUM-TICKET|ID:%s|MATCH:%s|SEAT:%s|USER:%s|TXN:%s",
                    ticket.getTicketId(), ticket.getMatchId(), ticket.getSeatId(), ticket.getUserId(), transactionId);
            ticket.setQrCodeData(qrPayload);

            // Update seat state to PAID
            Seat seat = repository.getSeat(ticket.getSeatId());
            if (seat != null) {
                seat.setStatus(TicketStatus.PAID);
            }
        }

        repository.saveBooking(booking);

        return new PaymentResult(true, 
            String.format("Thanh toán thành công qua %s! Mã giao dịch: %s", paymentMethod, transactionId), 
            booking);
    }

    /**
     * Task tự động giải phóng ghế khi hết thời gian giữ chỗ (Timeout/TTL)
     */
    public synchronized int releaseExpiredBookings() {
        int releasedCount = 0;
        List<Booking> allBookings = repository.getAllBookings();

        for (Booking booking : allBookings) {
            if (booking.getStatus() == BookingStatus.PENDING_PAYMENT && booking.isExpired()) {
                booking.setStatus(BookingStatus.EXPIRED);
                
                List<String> seatIds = new ArrayList<>();
                for (Ticket ticket : booking.getTickets()) {
                    ticket.setStatus(TicketStatus.CANCELLED);
                    seatIds.add(ticket.getSeatId());
                }

                repository.releaseSeats(seatIds);
                repository.saveBooking(booking);
                releasedCount++;
            }
        }
        return releasedCount;
    }

    /**
     * Use Case: Hủy đơn đặt vé chưa thanh toán
     */
    public BookingResult cancelBooking(String bookingId, String userId) {
        Booking booking = repository.getBooking(bookingId);
        if (booking == null) {
            return new BookingResult(false, "Không tìm thấy đơn đặt vé!", null);
        }

        if (!booking.getUserId().equalsIgnoreCase(userId)) {
            return new BookingResult(false, "Bạn không có quyền hủy đơn đặt vé này!", null);
        }

        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            return new BookingResult(false, "Chỉ có thể hủy các đơn đặt vé đang ở trạng thái Chờ thanh toán!", null);
        }

        booking.setStatus(BookingStatus.CANCELLED);
        List<String> seatIds = new ArrayList<>();
        for (Ticket t : booking.getTickets()) {
            t.setStatus(TicketStatus.CANCELLED);
            seatIds.add(t.getSeatId());
        }

        repository.releaseSeats(seatIds);
        repository.saveBooking(booking);

        return new BookingResult(true, "Đã hủy đơn đặt vé " + bookingId + " thành công!", booking);
    }

    /**
     * Use Case: Trả vé & Hoàn tiền (Refund paid ticket/booking)
     */
    public BookingResult refundBooking(String bookingId, String userId) {
        Booking booking = repository.getBooking(bookingId);
        if (booking == null) {
            return new BookingResult(false, "Không tìm thấy đơn đặt vé!", null);
        }

        if (!booking.getUserId().equalsIgnoreCase(userId)) {
            return new BookingResult(false, "Bạn không có quyền yêu cầu hoàn trả vé của đơn này!", null);
        }

        if (booking.getStatus() != BookingStatus.PAID) {
            return new BookingResult(false, "Chỉ có thể trả vé đối với các đơn đã thanh toán thành công!", null);
        }

        booking.setStatus(BookingStatus.REFUNDED);
        List<String> seatIds = new ArrayList<>();
        for (Ticket t : booking.getTickets()) {
            t.setStatus(TicketStatus.REFUNDED);
            seatIds.add(t.getSeatId());
        }

        repository.releaseSeats(seatIds);
        repository.saveBooking(booking);

        return new BookingResult(true, 
            String.format("Yêu cầu trả vé và hoàn tiền %,.0f VNĐ cho đơn %s thành công!", 
            booking.getTotalAmount(), bookingId), booking);
    }

    /**
     * Use Case: Xem danh sách vé đã mua của người dùng
     */
    public List<Ticket> getUserTickets(String userId) {
        releaseExpiredBookings();
        return repository.getTicketsByUser(userId);
    }

    /**
     * Use Case: Xem lịch sử danh sách đơn đặt vé của người dùng
     */
    public List<Booking> getUserBookings(String userId) {
        releaseExpiredBookings();
        return repository.getBookingsByUser(userId);
    }

    public Booking getBookingDetails(String bookingId) {
        return repository.getBooking(bookingId);
    }

    // Helper classes for result payloads
    public static class BookingResult {
        private final boolean success;
        private final String message;
        private final Booking booking;

        public BookingResult(boolean success, String message, Booking booking) {
            this.success = success;
            this.message = message;
            this.booking = booking;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Booking getBooking() { return booking; }
    }

    public static class PaymentResult {
        private final boolean success;
        private final String message;
        private final Booking booking;

        public PaymentResult(boolean success, String message, Booking booking) {
            this.success = success;
            this.message = message;
            this.booking = booking;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Booking getBooking() { return booking; }
    }
}
