package app.model;

/**
 * Loại giao dịch được ghi vào nhật ký hệ thống (TransactionLog).
 * Thuộc phần Model do Người 4 (Analytics & Logs) phụ trách.
 */
public enum TransactionType {
    BOOK,       // Đặt vé
    PAYMENT,    // Thanh toán
    CANCEL,     // Hủy vé
    REFUND,     // Trả vé / hoàn tiền
    LOGIN,      // Đăng nhập (tuỳ chọn, log cả hoạt động tài khoản)
    OTHER       // Loại khác
}
