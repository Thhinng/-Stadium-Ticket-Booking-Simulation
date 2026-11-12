package service;

import model.*;
import java.util.*;

public class EventService {
    private final Store store;
    private final AuthService auth;
    public EventService(Store store, AuthService auth) { this.store = store; this.auth = auth; }

    public List<Event> listPublic() {
        synchronized (this.store) {
            List<Event> result = new ArrayList<Event>();
            for (Event e : this.store.getDatabase().getEvents())
                if (e.getStatus() != EventStatus.CANCELLED && e.getStartsAt() > this.store.now()) result.add(e);
            result.sort(Comparator.comparingLong(x -> x.getStartsAt()));
            return result;
        }
    }

    public List<Event> searchPublic(String keyword) {
        String q = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        List<Event> result = new ArrayList<Event>();
        for (Event e : this.listPublic()) {
            if ((e.title() + " " + e.getCompetition()).toLowerCase(Locale.ROOT).contains(q)) result.add(e);
        }
        return result;
    }

    public List<Event> listAll(int adminId) {
        this.auth.requireActive(adminId, Role.ADMIN);
        synchronized (this.store) {
            List<Event> result = new ArrayList<Event>(this.store.getDatabase().getEvents());
            result.sort(Comparator.comparingLong(x -> x.getStartsAt()));
            return result;
        }
    }

    public Event find(int eventId) {
        synchronized (this.store) {
            for (Event e : this.store.getDatabase().getEvents()) if (e.getId() == eventId) return e;
            throw new IllegalArgumentException("Không tìm thấy trận đấu #" + eventId);
        }
    }

    public List<Section> sections() {
        synchronized (this.store) { return new ArrayList<Section>(this.store.getDatabase().getSections()); }
    }

    public Section section(String code) {
        synchronized (this.store) {
            for (Section s : this.store.getDatabase().getSections()) if (s.getCode().equalsIgnoreCase(code.trim())) return s;
            throw new IllegalArgumentException("Không có khán đài " + code);
        }
    }

    public long price(Event event, Section section) {
        Long price = event.getZonePrices().get(section.getCode());
        return price == null ? section.getBasePrice() : price.longValue();
    }

    public Map<String, Long> pricesForSections(Event event, List<Section> sections) {
        Map<String, Long> prices = new LinkedHashMap<String, Long>();
        for (Section section : sections) prices.put(section.getCode(), this.price(event, section));
        return prices;
    }

    public Event add(int adminId, String home, String away, String competition, long startsAt) {
        this.auth.requireActive(adminId, Role.ADMIN);
        validateDetails(home, away, competition);
        synchronized (this.store) {
            if (startsAt <= this.store.now()) throw new IllegalArgumentException("Trận đấu phải diễn ra trong tương lai.");
            Event event = new Event(this.store.getDatabase().takeNextEventId(), home.trim(), away.trim(), competition.trim(), startsAt, EventStatus.OPEN);
            for (Section s : this.store.getDatabase().getSections()) event.putZonePrice(s.getCode(), s.getBasePrice());
            this.store.getDatabase().getEvents().add(event);
            this.store.save();
            return event;
        }
    }

    public void edit(int adminId, int eventId, String home, String away, String competition, long startsAt) {
        this.auth.requireActive(adminId, Role.ADMIN);
        validateDetails(home, away, competition);
        synchronized (this.store) {
            Event event = this.find(eventId);
            if (event.getStatus() == EventStatus.CANCELLED) throw new IllegalArgumentException("Trận đấu bị hủy không thể chỉnh sửa.");
            if (startsAt <= this.store.now()) throw new IllegalArgumentException("Thời gian phải ở tương lai.");
            event.setHome(home.trim());
            event.setAway(away.trim());
            event.setCompetition(competition.trim());
            event.setStartsAt(startsAt);
            this.store.save();
        }
    }

    public void setSaleStatus(int adminId, int eventId, boolean open) {
        this.auth.requireActive(adminId, Role.ADMIN);
        synchronized (this.store) {
            Event event = this.find(eventId);
            if (event.getStatus() == EventStatus.CANCELLED) throw new IllegalArgumentException("Không thể mở bán trận đấu đã hủy.");
            if (open && event.getStartsAt() <= this.store.now()) throw new IllegalArgumentException("Trận đấu đã diễn ra.");
            event.setStatus(open ? EventStatus.OPEN : EventStatus.CLOSED);
            this.store.save();
        }
    }

    public void updatePrice(int adminId, int eventId, String code, long price) {
        this.auth.requireActive(adminId, Role.ADMIN);
        if (price < 1000 || price > 1000000000L) throw new IllegalArgumentException("Giá vé phải từ 1.000 đến 1 tỷ VND.");
        synchronized (this.store) {
            Event e = this.find(eventId);
            if (e.getStatus() == EventStatus.CANCELLED) throw new IllegalArgumentException("Trận đấu đã hủy.");
            Section s = this.section(code);
            e.putZonePrice(s.getCode(), price);
            // Existing locks hold the quoted price, so changing price doesn't surprise buyers mid-checkout.
            this.store.save();
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
