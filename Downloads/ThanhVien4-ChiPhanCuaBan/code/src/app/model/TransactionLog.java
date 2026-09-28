package app.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Model TransactionLog — đại diện cho MỘT dòng nhật ký giao dịch trong hệ thống.
 * Model chính thuộc phạm vi phụ trách của Người 4 (Analytics & Logs).
 */
public class TransactionLog {

    /** Định dạng thời gian dùng chung khi hiển thị và khi đọc/ghi CSV. */
    public static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final String logId;
    private final LocalDateTime timestamp;
    private final String username;
    private final TransactionType type;
    private final String description;
    private final double amount; // số tiền liên quan (0 nếu giao dịch không liên quan tới tiền)

    public TransactionLog(String logId, LocalDateTime timestamp, String username,
                           TransactionType type, String description, double amount) {
        this.logId = logId;
        this.timestamp = timestamp;
        this.username = username;
        this.type = type;
        this.description = description;
        this.amount = amount;
    }

    public String getLogId() { return logId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getUsername() { return username; }
    public TransactionType getType() { return type; }
    public String getDescription() { return description; }
    public double getAmount() { return amount; }

    /** Chuyển 1 dòng log thành chuỗi CSV để lưu file (mỗi log trên 1 dòng). */
    public String toCsvLine() {
        String safeDesc = (description == null ? "" : description).replace(",", ";");
        return String.join(",",
                logId,
                timestamp.format(FORMAT),
                username,
                type.name(),
                safeDesc,
                String.valueOf(amount));
    }

    /** Dựng lại 1 TransactionLog từ 1 dòng CSV (ngược với toCsvLine()). */
    public static TransactionLog fromCsvLine(String line) {
        String[] p = line.split(",", -1);
        return new TransactionLog(
                p[0],
                LocalDateTime.parse(p[1], FORMAT),
                p[2],
                TransactionType.valueOf(p[3]),
                p[4],
                Double.parseDouble(p[5]));
    }

    @Override
    public String toString() {
        return String.format("[%s] %-19s | %-10s | %-8s | %-30s | %,10.0f đ",
                logId, timestamp.format(FORMAT), username, type, description, amount);
    }
}
