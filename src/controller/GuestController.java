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
        guestView.showMatches(eventService.listPublic());

        while (true) {
            guestView.showMenu();
            int choice = console.askInt("Guest > ");

            try {
                switch (choice) {
                    case 1:
                        guestView.showMatches(eventService.listPublic());
                        break;
                    case 2:
                        showMatchDetails();
                        break;
                    case 0:
                        return;
                    default:
                        console.error("Vui lòng chọn chức năng có trong menu.");
                }
            } catch (IllegalArgumentException e) {
                console.error(e.getMessage());
            }
        }
    }

    private void showMatchDetails() {
        int matchId = console.askInt("Nhập mã trận đấu: ");

        for (Event event : eventService.listPublic()) {
            if (event.id == matchId) {
                guestView.showDetail(event);
                return;
            }
        }

        console.error("Trận đấu này không có trong sảnh Guest.");
    }
}
