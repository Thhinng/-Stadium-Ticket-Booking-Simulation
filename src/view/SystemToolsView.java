/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

import controller.SystemController;
import java.util.List;
import java.util.Scanner;
import model.Fan;
import model.Match;
import model.Seat;
import model.SimulationResult;
import model.Stadium;

public class SystemToolsView {

    private SystemController controller;
    private Scanner scanner;

    public SystemToolsView(SystemController controller) {
        this.controller = controller;
        this.scanner = new Scanner(System.in);
    }

    public SystemController getController() {
        return controller;
    }

    public void setController(SystemController controller) {
        this.controller = controller;
    }

   
    public SystemController getContoller() {
        return controller;
    }

    public void setContoller(SystemController controller) {
        this.controller = controller;
    }

    public Scanner getScanner() {
        return scanner;
    }

    public void setScanner(Scanner scanner) {
        this.scanner = scanner;
    }

    public void showMenu() {
        int choice = -1;
        do {
            System.out.println("\n==================================================");
            System.out.println("        SYSTEM TOOLS & SIMULATION DASHBOARD       ");
            System.out.println("==================================================");
            System.out.println("1. Khôi phục toàn bộ dữ liệu CSV mẫu (Reset CSV)");
            System.out.println("2. Xem thống kê dữ liệu hiện có trong CSV");
            System.out.println("3. Xem danh sách Fans (Khách hàng) từ CSV");
            System.out.println("4. Xem danh sách Trận đấu & Sân vận động");
            System.out.println("5. Xem danh sách Ghế & Trạng thái hiện tại");
            System.out.println("6. Chạy mô phỏng tải cao (Run Concurrency Simulation)");
            System.out.println("7. Xem kết quả mô phỏng gần nhất");
            System.out.println("0. Thoát chương trình");
            System.out.println("==================================================");
            System.out.print("Vui lòng chọn thao tác [0-7]: ");

            try {
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    continue;
                }
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println(">> Lỗi: Vui lòng nhập số hợp lệ!");
                continue;
            }

            switch (choice) {
                case 1:
                    resetCSV();
                    break;
                case 2:
                    showSystemSummary();
                    break;
                case 3:
                    showFansList();
                    break;
                case 4:
                    showMatchesAndStadiums();
                    break;
                case 5:
                    showSeatsList();
                    break;
                case 6:
                    showSimulationSubmenu();
                    break;
                case 7:
                    showLastResult();
                    break;
                case 0:
                    System.out.println(">> Đã thoát chương trình. Tạm biệt!");
                    break;
                default:
                    System.out.println(">> Lựa chọn không hợp lệ, vui lòng chọn lại!");
                    break;
            }
        } while (choice != 0);
    }

    public void resetCSV() {
        System.out.println("\n>> Đang tiến hành reset lại tất cả các file CSV...");
        controller.resetCSV();
    }

    public void runSimulation() {
        controller.runSimulation();
    }

    private void showSystemSummary() {
        System.out.println("\n" + controller.getSystemDataSummary());
    }

    private void showFansList() {
        List<Fan> fans = controller.getAllFans();
        System.out.println("\n================ DANH SÁCH FANS TRONG HỆ THỐNG ================");
        if (fans.isEmpty()) {
            System.out.println("  (Chưa có dữ liệu Fan. Hãy chọn mục 1 để tạo dữ liệu mẫu)");
            return;
        }
        System.out.printf("%-10s | %-20s | %-24s | %-12s | %-8s\n",
                "MÃ FAN", "HỌ VÀ TÊN", "EMAIL", "SỐ ĐT", "TRẠNG THÁI");
        System.out.println("----------------------------------------------------------------------------------");
        for (Fan f : fans) {
            System.out.printf("%-10s | %-20s | %-24s | %-12s | %-8s\n",
                    f.getId(), f.getFullName(), f.getEmail(), f.getPhone(), f.getStatus());
        }
    }

    private void showMatchesAndStadiums() {
        System.out.println("\n================ DANH SÁCH SÂN VẬN ĐỘNG ================");
        List<Stadium> stadiums = controller.getAllStadiums();
        if (stadiums.isEmpty()) {
            System.out.println("  (Chưa có dữ liệu sân vận động)");
        } else {
            for (Stadium s : stadiums) {
                System.out.printf("  [%s] %s | Địa chỉ: %s | Sức chứa: %,d chỗ\n",
                        s.getId(), s.getName(), s.getAddress(), s.getCapacity());
            }
        }

        System.out.println("\n================ DANH SÁCH TRẬN ĐẤU ================");
        List<Match> matches = controller.getAllMatches();
        if (matches.isEmpty()) {
            System.out.println("  (Chưa có dữ liệu trận đấu)");
        } else {
            for (Match m : matches) {
                System.out.printf("  [%s] %s vs %s | Sân: %s | Thời gian: %s | Trạng thái: %s\n",
                        m.getId(), m.getHomeTeam(), m.getAwayTeam(), m.getStadiumId(), m.getMatchDate(), m.getStatus());
            }
        }
    }

    private void showSeatsList() {
        List<Seat> seats = controller.getAllSeats();
        System.out.println("\n================ DANH SÁCH GHẾ NGỒI ================");
        if (seats.isEmpty()) {
            System.out.println("  (Chưa có dữ liệu ghế)");
            return;
        }
        System.out.printf("%-10s | %-12s | %-8s | %-10s | %-12s | %-8s\n",
                "MÃ GHẾ", "KHU VỰC", "TRẬN", "SỐ GHẾ", "TRẠNG THÁI", "VERSION");
        System.out.println("-----------------------------------------------------------------------");
        for (Seat s : seats) {
            System.out.printf("%-10s | %-12s | %-8s | %-10s | %-12s | %-8d\n",
                    s.getId(), s.getSectionId(), s.getMatchId(), s.getSeatNumber(), s.getStatus(), s.getVersion());
        }
    }

    private void showSimulationSubmenu() {
        System.out.println("\n----- CẤU HÌNH MÔ PHỎNG ĐẶT VÉ ĐA LUỒNG -----");
        System.out.println("1. NO_LOCK      - Không khóa (Minh họa lỗi Race Condition & Bán trùng ghế)");
        System.out.println("2. SYNCHRONIZED - Khóa bi quan (Pessimistic Lock - An toàn luồng)");
        System.out.println("3. OPTIMISTIC   - Khóa lạc quan (Optimistic Lock kiểm tra version ghế)");
        System.out.println("0. Quay lại");
        System.out.print("Chọn cơ chế [1-3]: ");

        String mechChoice = scanner.nextLine().trim();
        String mechanism;
        if ("1".equals(mechChoice)) {
            mechanism = "NO_LOCK";
        } else if ("2".equals(mechChoice)) {
            mechanism = "SYNCHRONIZED";
        } else if ("3".equals(mechChoice)) {
            mechanism = "OPTIMISTIC";
        } else {
            System.out.println(">> Đã hủy mô phỏng.");
            return;
        }

        int threads = 50;
        System.out.print("Nhập số lượng luồng đồng thời (Mặc định: 50): ");
        String threadInput = scanner.nextLine().trim();
        if (!threadInput.isEmpty()) {
            try {
                threads = Integer.parseInt(threadInput);
                if (threads <= 0) threads = 50;
            } catch (NumberFormatException e) {
                System.out.println(">> Nhập không đúng, sử dụng mặc định 50 luồng.");
                threads = 50;
            }
        }

        System.out.println(">> Đang kích hoạt mô phỏng...");
        controller.runSimulation(mechanism, threads, "M01");
    }

    private void showLastResult() {
        SimulationResult result = controller.getLastSimulationResult();
        if (result == null) {
            System.out.println("\n>> Chưa có kết quả mô phỏng nào trong phiên chạy này!");
        } else {
            System.out.println("\n" + result.toString());
        }
    }
}
