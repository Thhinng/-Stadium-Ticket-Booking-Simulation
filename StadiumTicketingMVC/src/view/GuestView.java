package view;

import model.Event;
import model.EventStatus;
import util.TimeUtil;
import java.util.List;

public class GuestView extends BaseView {
    public GuestView(ConsoleView ui) { super(ui); }

    public void showMenu() {
        super.getUi().title("SẢNH KHÁCH - GUEST");
        System.out.println("1. Xem các trận đấu sắp diễn ra");
        System.out.println("2. Xem thông tin cơ bản của trận đấu");
        System.out.println("0. Trở về màn hình chính (đăng nhập/đăng ký)");
    }

    public void showMatches(List<Event> events) {
        super.getUi().title("DANH SÁCH TRẬN ĐẤU - SẢNH");
        if (events.isEmpty()) { System.out.println("Hiện chưa có trận đấu sắp tới."); return; }
        for (Event e : events) {
            System.out.printf("#%-4d | %-30s | %-16s | %s%n", e.getId(), e.title(),
                    TimeUtil.format(e.getStartsAt()), e.getStatus() == EventStatus.OPEN ? "Đang mở bán" : "Chưa/mất mở bán");
            System.out.println("       Giải: " + e.getCompetition());
        }
    }

    public void showDetail(Event e) {
        super.getUi().title("THÔNG TIN TRẬN ĐẤU #" + e.getId());
        System.out.println("Trận đấu: " + e.title());
        System.out.println("Giải đấu: " + e.getCompetition());
        System.out.println("Thời gian: " + TimeUtil.format(e.getStartsAt()));
        System.out.println("Địa điểm: Sân vận động chính");
        System.out.println("Tình trạng: " + (e.getStatus() == EventStatus.OPEN ? "Đang mở bán" : "Tạm ngừng bán"));
        System.out.println("Đăng ký hoặc đăng nhập Fan để xem ghế và mua vé.");
    }
}
