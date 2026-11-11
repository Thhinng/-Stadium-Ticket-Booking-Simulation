package view;

import model.Event;
import model.EventStatus;
import util.TimeUtil;
import java.util.List;

public class GuestView {
    private final ConsoleView ui;
    public GuestView(ConsoleView ui) { this.ui = ui; }

    public void showMenu() {
        ui.title("SẢNH KHÁCH - GUEST");
        System.out.println("1. Xem các trận đấu sắp diễn ra");
        System.out.println("2. Xem thông tin cơ bản của trận đấu");
        System.out.println("0. Trở về màn hình chính (đăng nhập/đăng ký)");
    }

    public void showMatches(List<Event> events) {
        ui.title("DANH SÁCH TRẬN ĐẤU - SẢNH");
        if (events.isEmpty()) { System.out.println("Hiện chưa có trận đấu sắp tới."); return; }
        for (Event e : events) {
            System.out.printf("#%-4d | %-30s | %-16s | %s%n", e.id, e.title(),
                    TimeUtil.format(e.startsAt), e.status == EventStatus.OPEN ? "Đang mở bán" : "Chưa/mất mở bán");
            System.out.println("       Giải: " + e.competition);
        }
    }

    public void showDetail(Event e) {
        ui.title("THÔNG TIN TRẬN ĐẤU #" + e.id);
        System.out.println("Trận đấu: " + e.title());
        System.out.println("Giải đấu: " + e.competition);
        System.out.println("Thời gian: " + TimeUtil.format(e.startsAt));
        System.out.println("Địa điểm: Sân vận động chính");
        System.out.println("Tình trạng: " + (e.status == EventStatus.OPEN ? "Đang mở bán" : "Tạm ngừng bán"));
        System.out.println("Đăng ký hoặc đăng nhập Fan để xem ghế và mua vé.");
    }
}
