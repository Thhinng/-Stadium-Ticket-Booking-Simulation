package app.controller;

import app.model.TransactionLog;
import app.model.TransactionType;
import app.service.ReportService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * ReportController — tầng Controller do Người 4 phụ trách.
 * Chỉ phụ thuộc ReportService (cũng do Người 4 viết) — không import bất kỳ
 * class nào của thành viên khác.
 */
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // ---- Use case: Xem báo cáo doanh thu ----

    public double getTotalRevenue(List<Double> paidTicketPrices) {
        return reportService.getTotalRevenue(paidTicketPrices);
    }

    public Map<String, Double> getRevenueByMatch(Map<String, List<Double>> paidPricesByMatch) {
        return reportService.getRevenueByMatch(paidPricesByMatch);
    }

    // ---- Use case: Xem báo cáo tỷ lệ lấp đầy sân ----

    public double getOccupancyRate(int occupiedSeats, int totalSeats) {
        return reportService.getOccupancyRate(occupiedSeats, totalSeats);
    }

    public Map<String, Double> getOccupancyReport(Map<String, Integer> occupiedSeatsByMatch,
                                                   Map<String, Integer> totalSeatsByMatch) {
        return reportService.getOccupancyReport(occupiedSeatsByMatch, totalSeatsByMatch);
    }

    // ---- Ghi nhật ký (được các Controller khác trong hệ thống gọi) ----

    public TransactionLog logTransaction(String username, TransactionType type, String description, double amount) {
        return reportService.logTransaction(username, type, description, amount);
    }

    // ---- Use case: Xem nhật ký giao dịch ----

    public List<TransactionLog> getTransactionLogs() {
        return reportService.getAllLogs();
    }

    public List<TransactionLog> searchTransactionLogs(String username, TransactionType type,
                                                        LocalDateTime from, LocalDateTime to) {
        return reportService.queryLogs(username, type, from, to);
    }

    // ---- Lưu trữ nhật ký ----

    public void loadLogs(String filePath) throws IOException {
        reportService.loadLogsFromCsv(filePath);
    }

    public void saveLogs(String filePath) throws IOException {
        reportService.saveLogsToCsv(filePath);
    }
}
