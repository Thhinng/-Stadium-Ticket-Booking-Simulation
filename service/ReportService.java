package app.service;

import app.model.TransactionLog;
import app.model.TransactionType;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ReportService — tầng nghiệp vụ (Model/Service) do Người 4 phụ trách.
 *
 * QUAN TRỌNG: Lớp này KHÔNG import/khai báo bất kỳ class nào của thành viên
 * khác (không có Ticket, Match...). Các hàm tính doanh thu / tỷ lệ lấp đầy
 * chỉ nhận vào những GIÁ TRỊ THÔ (số, danh sách, Map) mà đội đã thống nhất —
 * việc lấy các giá trị đó ra từ class Ticket/Match thật sẽ do người gọi
 * (Controller của Người 2, Người 3, hoặc lớp Main khi tích hợp) thực hiện,
 * chỉ bằng vài dòng gọi getter, KHÔNG cần sửa gì trong ReportService.
 *
 * Chức năng:
 *  1. Tính tổng doanh thu / doanh thu theo từng trận đấu.
 *  2. Tính tỷ lệ lấp đầy sân (%) cho từng trận đấu.
 *  3. Ghi nhận và truy vấn Nhật ký giao dịch (TransactionLog).
 *  4. Đọc / ghi nhật ký ra file CSV (transactions.csv).
 */
public class ReportService {

    private final List<TransactionLog> transactionLogs = new ArrayList<>();
    private int logCounter = 1;

    // =================================================================
    // 1. GHI NHẬN GIAO DỊCH VÀO NHẬT KÝ
    // =================================================================

    /**
     * Ghi lại một giao dịch mới vào nhật ký hệ thống.
     * Được các Controller khác (VD: BookingController, AuthController) gọi
     * mỗi khi có hành động cần lưu vết.
     */
    public TransactionLog logTransaction(String username, TransactionType type, String description, double amount) {
        String logId = "LOG" + String.format("%04d", logCounter++);
        TransactionLog log = new TransactionLog(logId, LocalDateTime.now(), username, type, description, amount);
        transactionLogs.add(log);
        return log;
    }

    /** Trả về toàn bộ nhật ký (không cho sửa trực tiếp danh sách gốc). */
    public List<TransactionLog> getAllLogs() {
        return Collections.unmodifiableList(transactionLogs);
    }

    // =================================================================
    // 2. BÁO CÁO DOANH THU
    // =================================================================

    /**
     * Tính tổng doanh thu: cộng dồn toàn bộ giá trị trong danh sách
     * "giá các vé ĐÃ THANH TOÁN" mà người gọi truyền vào.
     * (Việc lọc vé nào đã thanh toán là do class Ticket của Người 3 quyết
     * định qua getStatus() — ReportService không cần biết Ticket là gì.)
     */
    public double getTotalRevenue(List<Double> paidTicketPrices) {
        double total = 0.0;
        for (double price : paidTicketPrices) {
            total += price;
        }
        return total;
    }

    /**
     * Doanh thu chia theo từng trận đấu.
     * @param paidPricesByMatch key = mã trận đấu (String),
     *                          value = danh sách giá các vé đã thanh toán của trận đó
     * @return key = mã trận đấu, value = tổng doanh thu trận đó
     */
    public Map<String, Double> getRevenueByMatch(Map<String, List<Double>> paidPricesByMatch) {
        Map<String, Double> result = new LinkedHashMap<>();
        for (Map.Entry<String, List<Double>> entry : paidPricesByMatch.entrySet()) {
            result.put(entry.getKey(), getTotalRevenue(entry.getValue()));
        }
        return result;
    }

    // =================================================================
    // 3. BÁO CÁO TỶ LỆ LẤP ĐẦY SÂN
    // =================================================================

    /**
     * Công thức tỷ lệ lấp đầy (%) = (số ghế đã lấp đầy) / (tổng số ghế) * 100.
     */
    public double getOccupancyRate(int occupiedSeats, int totalSeats) {
        if (totalSeats <= 0) return 0.0;
        return (occupiedSeats * 100.0) / totalSeats;
    }

    /**
     * Báo cáo tỷ lệ lấp đầy cho nhiều trận đấu cùng lúc.
     * @param occupiedSeatsByMatch key = mã trận đấu, value = số ghế đã lấp đầy của trận đó
     * @param totalSeatsByMatch    key = mã trận đấu, value = tổng số ghế của trận đó
     * @return key = mã trận đấu, value = tỷ lệ lấp đầy (%)
     */
    public Map<String, Double> getOccupancyReport(Map<String, Integer> occupiedSeatsByMatch,
                                                   Map<String, Integer> totalSeatsByMatch) {
        Map<String, Double> result = new LinkedHashMap<>();
        for (String matchId : totalSeatsByMatch.keySet()) {
            int occupied = occupiedSeatsByMatch.getOrDefault(matchId, 0);
            int total = totalSeatsByMatch.get(matchId);
            result.put(matchId, getOccupancyRate(occupied, total));
        }
        return result;
    }

    // =================================================================
    // 4. TRUY VẤN NHẬT KÝ GIAO DỊCH
    // =================================================================

    /**
     * Truy vấn nhật ký theo điều kiện lọc. Mọi tham số có thể để null
     * (hoặc chuỗi rỗng với username) để bỏ qua điều kiện đó.
     */
    public List<TransactionLog> queryLogs(String username, TransactionType type,
                                           LocalDateTime from, LocalDateTime to) {
        return transactionLogs.stream()
                .filter(l -> username == null || username.isBlank() || l.getUsername().equalsIgnoreCase(username))
                .filter(l -> type == null || l.getType() == type)
                .filter(l -> from == null || !l.getTimestamp().isBefore(from))
                .filter(l -> to == null || !l.getTimestamp().isAfter(to))
                .collect(Collectors.toList());
    }

    // =================================================================
    // 5. ĐỌC / GHI NHẬT KÝ RA FILE CSV
    // =================================================================

    /** Đọc toàn bộ nhật ký từ file CSV (mỗi dòng 1 giao dịch), nạp vào bộ nhớ. */
    public void loadLogsFromCsv(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            transactionLogs.clear();
            int maxId = 0;
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                TransactionLog log = TransactionLog.fromCsvLine(line);
                transactionLogs.add(log);
                try {
                    maxId = Math.max(maxId, Integer.parseInt(log.getLogId().replace("LOG", "")));
                } catch (NumberFormatException ignored) {
                    // bỏ qua nếu logId không đúng định dạng LOGxxxx
                }
            }
            logCounter = maxId + 1;
        }
    }

    /** Ghi toàn bộ nhật ký hiện có ra file CSV. */
    public void saveLogsToCsv(String filePath) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
            for (TransactionLog log : transactionLogs) {
                bw.write(log.toCsvLine());
                bw.newLine();
            }
        }
    }
}
