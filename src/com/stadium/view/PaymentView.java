package com.stadium.view;

import com.stadium.model.Booking;
import com.stadium.model.Ticket;

/**
 * View: PaymentView (Cổng Thanh Toán & Hóa Đơn Điện Tử).
 * Member 3: Lê Bách - Booking & Payment Module
 */
public class PaymentView {

    public static void displayPaymentMethods(Booking booking) {
        System.out.println("\n=======================================================");
        System.out.println("               CỔNG THANH TOÁN VÉ SÂN                ");
        System.out.println("=======================================================");
        System.out.println(" Mã Đơn Hàng : " + booking.getBookingId());
        System.out.printf(" Số Tiền Cần Thanh Toán: %,.0f VNĐ%n", booking.getTotalAmount());
        System.out.println(" Thời Gian Còn Lại: " + booking.getFormattedRemainingTime());
        System.out.println("-------------------------------------------------------");
        System.out.println(" VUI LÒNG CHỌN PHƯƠNG THỨC THANH TOÁN:");
        System.out.println(" 1. Quét Mã QR VNPay (Thanh toán tức thì)");
        System.out.println(" 2. Ví Điện Tử MoMo / ZaloPay");
        System.out.println(" 3. Thẻ Ghi Nợ / Tín Dụng Quốc Tế (Visa/Mastercard)");
        System.out.println(" 4. Chuyển Khoản Ngân Hàng Trực Tiếp");
        System.out.println("=======================================================\n");
    }

    public static void displayReceipt(Booking booking) {
        System.out.println("\n=======================================================");
        System.out.println("             HÓA ĐƠN THANH TOÁN THÀNH CÔNG             ");
        System.out.println("=======================================================");
        System.out.println(" Mã Giao Dịch  : " + booking.getTransactionId());
        System.out.println(" Mã Đơn Hàng   : " + booking.getBookingId());
        System.out.println(" Người Đặt     : " + booking.getUserId());
        System.out.println(" Trận Đấu      : " + booking.getMatchName());
        System.out.println(" Phương Thức   : " + booking.getPaymentMethod());
        System.out.println(" Thời Gian TT  : " + booking.getFormattedPaymentTime());
        System.out.println(" Trạng Thái    : " + booking.getStatus().getDescription());
        System.out.println("-------------------------------------------------------");
        System.out.println(" VÉ ĐÃ XUẤT CỦA BẠN:");
        for (Ticket t : booking.getTickets()) {
            System.out.println(" + Mã Vé: " + t.getTicketId() + " | Ghế: " + t.getSeatName());
            System.out.println("   Mã QR Barcode Soát Vé: " + t.getQrCodeData());
        }
        System.out.println("-------------------------------------------------------");
        System.out.printf(" TỔNG ĐÃ THANH TOÁN: %,.0f VNĐ%n", booking.getTotalAmount());
        System.out.println("=======================================================");
        System.out.println(" Cảm ơn quý khách! Chúc quý khách xem trận đấu vui vẻ!");
        System.out.println("=======================================================\n");
    }
}
