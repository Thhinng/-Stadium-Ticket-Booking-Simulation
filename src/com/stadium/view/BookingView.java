package com.stadium.view;

import com.stadium.model.Booking;
import com.stadium.model.Seat;
import com.stadium.model.Ticket;

import java.util.List;

/**
 * View: BookingView (Màn hình Xác nhận Đặt vé & Chi tiết đơn hàng).
 * Member 3: Lê Bách - Booking & Payment Module
 */
public class BookingView {

    public static void displaySeatMap(List<Seat> seats) {
        System.out.println("\n=======================================================");
        System.out.println("            DANH SÁCH GHẾ NGỒI SÂN VẬN ĐỘNG            ");
        System.out.println("=======================================================");
        System.out.printf("%-18s | %-12s | %-12s | %-15s%n", "MÃ GHẾ", "KHÁN ĐÀI", "GIÁ VÉ (VNĐ)", "TRẠNG THÁI");
        System.out.println("-------------------------------------------------------");
        for (Seat s : seats) {
            System.out.printf("%-18s | %-12s | %,12.0f | %-15s%n",
                    s.getSeatId(), s.getZone(), s.getPrice(), s.getStatus().getDescription());
        }
        System.out.println("=======================================================\n");
    }

    public static void displayBookingConfirmation(Booking booking) {
        if (booking == null) {
            System.out.println("[LỖI] Không có thông tin đơn đặt vé.");
            return;
        }

        System.out.println("\n=======================================================");
        System.out.println("            XÁC NHẬN THÔNG TIN ĐẶT VÉ                 ");
        System.out.println("=======================================================");
        System.out.println(" Mã Đơn Hàng  : " + booking.getBookingId());
        System.out.println(" Trận Đấu     : " + booking.getMatchName());
        System.out.println(" Thời Gian Đặt: " + booking.getFormattedBookingTime());
        System.out.println(" Trạng Thái   : " + booking.getStatus().getDescription());
        System.out.println(" Thời Gian Giữ: " + booking.getFormattedRemainingTime() + " (Đếm ngược TTL)");
        System.out.println("-------------------------------------------------------");
        System.out.println(" CHI TIẾT CÁC GHẾ ĐÃ CHỌN:");
        int idx = 1;
        for (Ticket ticket : booking.getTickets()) {
            System.out.printf("  %d. Mã Vé: %-12s | %-16s | Khu: %-6s | %,10.0f VNĐ%n",
                    idx++, ticket.getTicketId(), ticket.getSeatName(), ticket.getZone(), ticket.getPrice());
        }
        System.out.println("-------------------------------------------------------");
        System.out.printf(" TỔNG CỘNG THANH TOÁN: %,.0f VNĐ%n", booking.getTotalAmount());
        System.out.println("=======================================================");
        System.out.println(" Lưu ý: Quý khách vui lòng thanh toán trong vòng " + 
                booking.getFormattedRemainingTime() + " để không bị hủy vé giữ chỗ!");
        System.out.println("=======================================================\n");
    }
}
