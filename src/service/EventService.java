package service;

import model.*;
import java.util.*;

public class EventService {
    private final Store store;
    private final AuthService auth;
    public EventService(Store store, AuthService auth) { this.store = store; this.auth = auth; }

    public List<Event> listPublic() {
        synchronized (store) {
            List<Event> result = new ArrayList<Event>();
            for (Event e : store.db.events)
                if (e.status != EventStatus.CANCELLED && e.startsAt > store.now()) result.add(e);
            result.sort(Comparator.comparingLong(x -> x.startsAt));
            return result;
        }
    }

    public List<Event> searchPublic(String keyword) {
        String q = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        List<Event> result = new ArrayList<Event>();
        for (Event e : listPublic()) {
            if ((e.title() + " " + e.competition).toLowerCase(Locale.ROOT).contains(q)) result.add(e);
        }
        return result;
    }

    public List<Event> listAll(int adminId) {
        auth.requireActive(adminId, Role.ADMIN);
        synchronized (store) {
            List<Event> result = new ArrayList<Event>(store.db.events);
            result.sort(Comparator.comparingLong(x -> x.startsAt));
            return result;
        }
    }

    public Event find(int eventId) {
        synchronized (store) {
            for (Event e : store.db.events) if (e.id == eventId) return e;
            throw new IllegalArgumentException("Không tìm thấy trận đấu #" + eventId);
        }
    }

    public List<Section> sections() {
        synchronized (store) { return new ArrayList<Section>(store.db.sections); }
    }

    public Section section(String code) {
        synchronized (store) {
            for (Section s : store.db.sections) if (s.code.equalsIgnoreCase(code.trim())) return s;
            throw new IllegalArgumentException("Không có khán đài " + code);
        }
    }

    public long price(Event event, Section section) {
        Long price = event.zonePrices.get(section.code);
        return price == null ? section.basePrice : price.longValue();
    }

    public Event add(int adminId, String home, String away, String competition, long startsAt) {
        auth.requireActive(adminId, Role.ADMIN);
        validateDetails(home, away, competition);
        synchronized (store) {
            if (startsAt <= store.now()) throw new IllegalArgumentException("Trận đấu phải diễn ra trong tương lai.");
            Event event = new Event(store.db.nextEventId++, home.trim(), away.trim(), competition.trim(), startsAt, EventStatus.OPEN);
            for (Section s : store.db.sections) event.zonePrices.put(s.code, s.basePrice);
            store.db.events.add(event);
            store.save();
            return event;
        }
    }

    public void edit(int adminId, int eventId, String home, String away, String competition, long startsAt) {
        auth.requireActive(adminId, Role.ADMIN);
        validateDetails(home, away, competition);
        synchronized (store) {
            Event event = find(eventId);
            if (event.status == EventStatus.CANCELLED) throw new IllegalArgumentException("Trận đấu bị hủy không thể chỉnh sửa.");
            if (startsAt <= store.now()) throw new IllegalArgumentException("Thời gian phải ở tương lai.");
            event.home = home.trim();
            event.away = away.trim();
            event.competition = competition.trim();
            event.startsAt = startsAt;
            store.save();
        }
    }

    public void setSaleStatus(int adminId, int eventId, boolean open) {
        auth.requireActive(adminId, Role.ADMIN);
        synchronized (store) {
            Event event = find(eventId);
            if (event.status == EventStatus.CANCELLED) throw new IllegalArgumentException("Không thể mở bán trận đấu đã hủy.");
            if (open && event.startsAt <= store.now()) throw new IllegalArgumentException("Trận đấu đã diễn ra.");
            event.status = open ? EventStatus.OPEN : EventStatus.CLOSED;
            store.save();
        }
    }

    public void updatePrice(int adminId, int eventId, String code, long price) {
        auth.requireActive(adminId, Role.ADMIN);
        if (price < 1000 || price > 1000000000L) throw new IllegalArgumentException("Giá vé phải từ 1.000 đến 1 tỷ VND.");
        synchronized (store) {
            Event e = find(eventId);
            if (e.status == EventStatus.CANCELLED) throw new IllegalArgumentException("Trận đấu đã hủy.");
            Section s = section(code);
            e.zonePrices.put(s.code, price);
            // Existing locks hold the quoted price, so changing price doesn't surprise buyers mid-checkout.
            store.save();
        }
    }

    private static void validateDetails(String home, String away, String competition) {
        if (home == null || home.trim().length() < 2 || home.trim().length() > 80 ||
                away == null || away.trim().length() < 2 || away.trim().length() > 80 ||
                competition == null || competition.trim().length() < 2 || competition.trim().length() > 80)
            throw new IllegalArgumentException("Tên đội và giải đấu phải từ 2 đến 80 ký tự.");
        if (home.trim().equalsIgnoreCase(away.trim())) throw new IllegalArgumentException("Hai đội không được trùng nhau.");
    }
}
