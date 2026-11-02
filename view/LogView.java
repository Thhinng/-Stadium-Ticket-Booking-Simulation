package view;

import model.TransactionLog;

import java.util.List;

/**
 * LogView — "Bảng nhật ký hệ thống cho Admin" (Người 4).
 * Hiển thị dạng bảng trên console danh sách các TransactionLog.
 */
public class LogView {

    private String repeat(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    public void showLogs(List<TransactionLog> logs) {
        System.out.println();
        System.out.println("========== NHẬT KÝ GIAO DỊCH HỆ THỐNG ==========");
        if (logs == null || logs.isEmpty()) {
            System.out.println("(Không có giao dịch nào phù hợp)");
            return;
        }
        System.out.printf("%-6s | %-19s | %-10s | %-8s | %-30s | %12s%n",
                "Mã log", "Thời gian", "Người dùng", "Loại", "Mô tả", "Số tiền");
        System.out.println(repeat("-", 100));
        for (TransactionLog log : logs) {
            System.out.printf("%-6s | %-19s | %-10s | %-8s | %-30s | %,12.0f%n",
                    log.getLogId(),
                    log.getTimestamp().format(TransactionLog.FORMAT),
                    log.getUsername(),
                    log.getType(),
                    log.getDescription(),
                    log.getAmount());
        }
    }
}
