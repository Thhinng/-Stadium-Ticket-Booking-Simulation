package controller;

import model.Event;
import service.EventService;
import view.ConsoleView;
import view.GuestView;

/** Guest chỉ được xem danh sách trận đấu và thông tin công khai. */
public class GuestController {
    private final EventService eventService;
    private final GuestView guestView;
    private final ConsoleView console;

    public GuestController(EventService eventService, GuestView guestView, ConsoleView console) {
        this.eventService = eventService;
        this.guestView = guestView;
        this.console = console;
    }

    public void run() {
        this.guestView.showMatches(this.eventService.listPublic());

        while (true) {
            this.guestView.showMenu();
            int choice = this.console.askInt("Guest > ");

            try {
                switch (choice) {
                    case 1:
                        this.guestView.showMatches(this.eventService.listPublic());
                        break;
                    case 2:
                        this.showMatchDetails();
                        break;
                    case 0:
                        return;
                    default:
                        this.console.error("Vui lòng chọn chức năng có trong menu.");
                }
            } catch (IllegalArgumentException e) {
                this.console.error(e.getMessage());
            }
        }
    }

    private void showMatchDetails() {
        int matchId = this.console.askInt("Nhập mã trận đấu: ");

        for (Event event : this.eventService.listPublic()) {
            if (event.getId() == matchId) {
                this.guestView.showDetail(event);
                return;
            }
        }

        this.console.error("Trận đấu này không có trong sảnh Guest.");
    }
}
