package com.stadium.view;

import com.stadium.model.Booking;
import com.stadium.model.Ticket;

import java.util.List;

/**
 * View: UserTicketsView (Quản lý Danh Sách Vé & Đơn Đặt Vé Cá Nhân).
 * Use Cases: Xem vé đã mua, Hủy vé chưa thanh toán, Yêu cầu Trả vé/Hoàn tiền.
 * Member 3: Lê Bách - Booking & Payment Module
 */
public class UserTicketsView {

    public static void displayUserBookings(List<Booking> bookings) {
        System.out.println("\n=========================================================================================");
        System.out.println("                         LỊCH SỬ ĐƠN ĐẶT VÉ CỦA BẠN                                     ");
        System.out.println("=========================================================================================");
        if (bookings == null || bookings.isEmpty()) {
            System.out.println(" Bạn chưa có đơn đặt vé nào trong hệ thống.");
            System.out.println("=========================================================================================\n");
            return;
        }

        System.out.printf("%-10s | %-32s | %-5s | %-12s | %-14s | %-20s%n",
                "MÃ ĐƠN", "TRẬN ĐẤU", "SỐ VÉ", "TỔNG TIỀN", "TRẠNG THÁI", "THỜI GIAN ĐẶT");
        System.out.println("-----------------------------------------------------------------------------------------");

        for (Booking b : bookings) {
            System.out.printf("%-10s | %-32s | %-5d | %,12.0f | %-14s | %-20s%n",
                    b.getBookingId(),
                    truncate(b.getMatchName(), 32),
                    b.getTickets().size(),
                    b.getTotalAmount(),
                    b.getStatus().getDescription(),
                    b.getFormattedBookingTime());
        }
        System.out.println("=========================================================================================\n");
    }

    public static void displayUserTickets(List<Ticket> tickets) {
        System.out.println("\n=========================================================================================");
        System.out.println("                         DANH SÁCH VÉ CÁ NHÂN ĐÃ MUA                                    ");
        System.out.println("=========================================================================================");
        if (tickets == null || tickets.isEmpty()) {
            System.out.println(" Bạn hiện chưa sở hữu vé trận đấu nào.");
            System.out.println("=========================================================================================\n");
            return;
        }

        System.out.printf("%-12s | %-28s | %-18s | %-8s | %-12s | %-12s%n",
                "MÃ VÉ", "TRẬN ĐẤU", "VỊ TRÍ GHẾ", "KHU VỰC", "GIÁ VÉ", "TRẠNG THÁI");
        System.out.println("-----------------------------------------------------------------------------------------");

        for (Ticket t : tickets) {
            System.out.printf("%-12s | %-28s | %-18s | %-8s | %,12.0f | %-12s%n",
                    t.getTicketId(),
                    truncate(t.getMatchName(), 28),
                    t.getSeatName(),
                    t.getZone(),
                    t.getPrice(),
                    t.getStatus().getDescription());
        }
        System.out.println("=========================================================================================\n");
    }

    public static void displayTicketDigitalDetails(Ticket ticket) {
        if (ticket == null) return;
        System.out.println("\n-------------------------------------------------------");
        System.out.println("              THÔNG TIN VÉ ĐIỆN TỬ (E-TICKET)          ");
        System.out.println("-------------------------------------------------------");
        System.out.println(" Mã Vé        : " + ticket.getTicketId());
        System.out.println(" Trận Đấu     : " + ticket.getMatchName());
        System.out.println(" Khán Đài     : " + ticket.getZone());
        System.out.println(" Vị Trí Ghế   : " + ticket.getSeatName());
        System.out.println(" Người Sở Hữu : " + ticket.getUserId());
        System.out.println(" Thời Gian Mua: " + ticket.getFormattedPurchaseTime());
        System.out.println(" Mã QR Vào Cổng: " + ticket.getQrCodeData());
        System.out.println("-------------------------------------------------------\n");
    }

    private static String truncate(String text, int length) {
        if (text == null) return "";
        if (text.length() <= length) return text;
        return text.substring(0, length - 3) + "...";
    }
}
