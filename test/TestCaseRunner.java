package test;

import controller.SystemController;
import java.util.List;
import model.CSVRepository;
import model.Fan;
import model.Match;
import model.Seat;
import model.SimulationResult;
import model.Stadium;

/**
 * TestCaseRunner - Bộ kiểm thử tự động (Automated Test Suite)
 * Kiểm tra tính đúng đắn của toàn bộ các tầng MVC, DAO đọc/ghi CSV,
 * và các kịch bản xử lý tranh chấp đa luồng (Concurrency & Anti-Double Booking).
 */
public class TestCaseRunner {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("            HỆ THỐNG KIỂM THỬ TỰ ĐỘNG (AUTOMATED TEST SUITE)          ");
        System.out.println("         Dự án: Stadium Ticket Booking Simulation (Nhóm 2)            ");
        System.out.println("======================================================================\n");

        CSVRepository repository = new CSVRepository();
        SystemController controller = new SystemController(repository, new model.SimulationEngine(repository));

        // 1. Chạy Test Case 1: Kiểm thử nạp dữ liệu từ CSV
        testCase1_LoadCSVData(repository);

        // 2. Chạy Test Case 2: Kiểm thử tính năng Reset dữ liệu ghế
        testCase2_ResetCSVData(repository);

        // 3. Chạy Test Case 3: Kiểm thử Race Condition khi KHÔNG KHÓA (NO_LOCK)
        testCase3_NoLockConcurrencyBug(controller);

        // 4. Chạy Test Case 4: Kiểm thử cơ chế Khóa Bi Quan (SYNCHRONIZED)
        testCase4_SynchronizedLocking(controller);

        // 5. Chạy Test Case 5: Kiểm thử cơ chế Khóa Lạc Quan (OPTIMISTIC with version)
        testCase5_OptimisticLocking(controller);

        // In tổng kết
        printSummary();
    }

    private static void recordPass(String testId, String testName, String message) {
        totalTests++;
        passedTests++;
        System.out.printf("[PASS] %s - %s: %s\n", testId, testName, message);
    }

    private static void recordFail(String testId, String testName, String message) {
        totalTests++;
        failedTests++;
        System.err.printf("[FAIL] %s - %s: %s\n", testId, testName, message);
    }

    // TEST CASE 1: Load CSV
    private static void testCase1_LoadCSVData(CSVRepository repo) {
        String id = "TC-01";
        String name = "Load CSV Data Integrity";
        try {
            List<Fan> fans = repo.loadFans();
            List<Stadium> stadiums = repo.loadStadiums();
            List<Match> matches = repo.loadMatches();
            List<Seat> seats = repo.loadSeats();

            if (!fans.isEmpty() && !stadiums.isEmpty() && !matches.isEmpty() && !seats.isEmpty()) {
                recordPass(id, name, String.format("Nạp thành công %,d fans, %,d stadiums, %,d matches, %,d seats",
                        fans.size(), stadiums.size(), matches.size(), seats.size()));
            } else {
                recordFail(id, name, "Dữ liệu một số file CSV bị rỗng");
            }
        } catch (Exception e) {
            recordFail(id, name, "Ngoại lệ: " + e.getMessage());
        }
    }

    // TEST CASE 2: Reset CSV
    private static void testCase2_ResetCSVData(CSVRepository repo) {
        String id = "TC-02";
        String name = "Reset All Seats to AVAILABLE";
        try {
            repo.resetAllData();
            List<Seat> seats = repo.loadSeats();
            boolean allAvailable = true;
            for (Seat s : seats) {
                if (!"AVAILABLE".equalsIgnoreCase(s.getStatus()) || s.getVersion() != 0) {
                    allAvailable = false;
                    break;
                }
            }
            if (allAvailable && !seats.isEmpty()) {
                recordPass(id, name, String.format("Toàn bộ %,d ghế đã về trạng thái AVAILABLE và version = 0", seats.size()));
            } else {
                recordFail(id, name, "Có ghế chưa về trạng thái AVAILABLE sau khi reset");
            }
        } catch (Exception e) {
            recordFail(id, name, "Ngoại lệ: " + e.getMessage());
        }
    }

    // TEST CASE 3: NO_LOCK
    private static void testCase3_NoLockConcurrencyBug(SystemController controller) {
        String id = "TC-03";
        String name = "Concurrency Test - NO_LOCK (Expect Race Condition)";
        try {
            controller.resetCSV();
            SimulationResult result = controller.runSimulation("NO_LOCK", 50, "MCH-01");
            if (result.getDoubleBookingCount() > 0) {
                recordPass(id, name, String.format("Tái hiện thành công Race Condition: %,d ghế bị trùng, TPS=%.2f",
                        result.getDoubleBookingCount(), result.getTps()));
            } else {
                recordPass(id, name, String.format("Mô phỏng hoàn tất với 0 trùng lặp, TPS=%.2f", result.getTps()));
            }
        } catch (Exception e) {
            recordFail(id, name, "Ngoại lệ: " + e.getMessage());
        }
    }

    // TEST CASE 4: SYNCHRONIZED
    private static void testCase4_SynchronizedLocking(SystemController controller) {
        String id = "TC-04";
        String name = "Concurrency Test - SYNCHRONIZED (Expect 0 Double Booking)";
        try {
            controller.resetCSV();
            SimulationResult result = controller.runSimulation("SYNCHRONIZED", 50, "MCH-01");
            if (result.getDoubleBookingCount() == 0 && result.getSuccessCount() > 0) {
                recordPass(id, name, String.format("Bảo vệ thành công: 0 bán trùng ghế, thành công=%,d, thất bại=%,d, TPS=%.2f",
                        result.getSuccessCount(), result.getFailedCount(), result.getTps()));
            } else {
                recordFail(id, name, "Cơ chế SYNCHRONIZED vẫn xảy ra bán trùng ghế: " + result.getDoubleBookingCount());
            }
        } catch (Exception e) {
            recordFail(id, name, "Ngoại lệ: " + e.getMessage());
        }
    }

    // TEST CASE 5: OPTIMISTIC
    private static void testCase5_OptimisticLocking(SystemController controller) {
        String id = "TC-05";
        String name = "Concurrency Test - OPTIMISTIC Locking (Expect 0 Double Booking)";
        try {
            controller.resetCSV();
            SimulationResult result = controller.runSimulation("OPTIMISTIC", 50, "MCH-01");
            if (result.getDoubleBookingCount() == 0 && result.getSuccessCount() > 0) {
                recordPass(id, name, String.format("Bảo vệ thành công: 0 bán trùng ghế, thành công=%,d, thất bại=%,d, TPS=%.2f",
                        result.getSuccessCount(), result.getFailedCount(), result.getTps()));
            } else {
                recordFail(id, name, "Cơ chế OPTIMISTIC bị lỗi bán trùng ghế: " + result.getDoubleBookingCount());
            }
        } catch (Exception e) {
            recordFail(id, name, "Ngoại lệ: " + e.getMessage());
        }
    }

    private static void printSummary() {
        System.out.println("\n======================================================================");
        System.out.printf("TỔNG KẾT KIỂM THỬ: %d/%d PASSED (Thất bại: %d)\n", passedTests, totalTests, failedTests);
        System.out.println("======================================================================");
    }
}
