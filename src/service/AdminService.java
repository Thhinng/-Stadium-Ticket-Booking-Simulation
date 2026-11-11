package service;

import model.*;
import java.util.*;

public class AdminService {
    private final Store store;
    private final AuthService auth;
    private final EventService events;
    private final BookingService booking;

    public AdminService(Store store, AuthService auth, EventService events, BookingService booking) {
        this.store = store;
        this.auth = auth;
        this.events = events;
        this.booking = booking;
    }

    private void check(int adminId) { auth.requireActive(adminId, Role.ADMIN); }

    public Dashboard dashboard(int adminId) {
        check(adminId);
        synchronized (store) {
            store.expireHolds();
            Dashboard d = new Dashboard();
            for (User u : store.db.users) {
                if (u.role == Role.FAN) {
                    d.totalFans++;
                    if (u.active) d.activeFans++;
                }
            }
            for (Event e : store.db.events) {
                d.totalEvents++;
                if (e.status == EventStatus.OPEN && e.startsAt > store.now()) d.openEvents++;
            }
            for (Order o : store.db.orders) {
                if (o.status == OrderStatus.PAID) d.paidOrders++;
                else d.refundedOrders++;
            }
            for (PaymentRecord p : store.db.payments) if (p.result.equals("SUCCESS")) d.netRevenue += p.amount;
            d.activeHolds = store.db.holds.size();
            return d;
        }
    }

    public List<User> fans(int adminId, String keyword) {
        check(adminId);
        String q = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        synchronized (store) {
            List<User> result = new ArrayList<User>();
            for (User u : store.db.users) {
                if (u.role == Role.FAN && (u.username.toLowerCase(Locale.ROOT).contains(q)
                        || u.name.toLowerCase(Locale.ROOT).contains(q))) result.add(u);
            }
            result.sort(Comparator.comparingInt(u -> u.id));
            return result;
        }
    }

    public List<Order> fanOrders(int adminId, int fanId) {
        check(adminId);
        User fan = auth.getById(fanId);
        if (fan.role != Role.FAN) throw new IllegalArgumentException("Đây không phải tài khoản Fan.");
        synchronized (store) {
            List<Order> result = new ArrayList<Order>();
            for (Order o : store.db.orders) if (o.userId == fanId) result.add(o);
            result.sort((a, b) -> Integer.compare(b.id, a.id));
            return result;
        }
    }

    public void setFanActive(int adminId, int fanId, boolean active) {
        check(adminId);
        synchronized (store) {
            User fan = auth.getById(fanId);
            if (fan.role != Role.FAN) throw new IllegalArgumentException("Chỉ được quản lý tài khoản Fan.");
            fan.active = active;
            if (!active) store.db.holds.removeIf(h -> h.userId == fanId);
            store.save();
        }
    }

    public void resetFanPassword(int adminId, int fanId, String newPassword) {
        check(adminId);
        synchronized (store) {
            User fan = auth.getById(fanId);
            if (fan.role != Role.FAN) throw new IllegalArgumentException("Chỉ được đặt lại mật khẩu cho Fan.");
            auth.setPassword(fan, newPassword);
            store.save();
        }
    }

    public Section addSection(int adminId, String code, String name, int capacity, long price) {
        check(adminId);
        code = code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
        validateSection(code, name, capacity, price);
        synchronized (store) {
            for (Section s : store.db.sections) if (s.code.equals(code))
                throw new IllegalArgumentException("Mã khán đài đã tồn tại.");
            Section added = new Section(code, name.trim(), capacity, price, true);
            store.db.sections.add(added);
            for (Event e : store.db.events) e.zonePrices.put(code, price);
            store.save();
            return added;
        }
    }

    public void editSection(int adminId, String code, String name, int capacity, long price, boolean active) {
        check(adminId);
        synchronized (store) {
            Section section = events.section(code);
            validateSection(section.code, name, capacity, price);
            if (capacity < section.capacity) {
                for (Order o : store.db.orders)
                    if (o.status == OrderStatus.PAID && hasSeatAbove(o.seatIds, section.code, capacity))
                        throw new IllegalArgumentException("Không thể giảm sức chứa: có ghế đã bán ngoài phạm vi mới.");
                store.expireHolds();
                for (Hold h : store.db.holds)
                    if (hasSeatAbove(h.seatIds, section.code, capacity))
                        throw new IllegalArgumentException("Không thể giảm sức chứa: có ghế đang được giữ ngoài phạm vi mới.");
            }
            section.name = name.trim();
            section.capacity = capacity;
            section.basePrice = price; // affects new events; existing event prices configured separately
            section.active = active;
            store.save();
        }
    }

    private static boolean hasSeatAbove(List<String> seats, String code, int limit) {
        for (String seat : seats) {
            if (seat.startsWith(code + "-")) {
                try {
                    if (Integer.parseInt(seat.substring(code.length() + 1)) > limit) return true;
                } catch (NumberFormatException ex) { return true; }
            }
        }
        return false;
    }

    private static void validateSection(String code, String name, int capacity, long price) {
        if (!code.matches("[A-Z]{1,3}")) throw new IllegalArgumentException("Mã khán đài phải gồm 1-3 chữ cái A-Z.");
        if (name == null || name.trim().length() < 2 || name.trim().length() > 80)
            throw new IllegalArgumentException("Tên khán đài phải từ 2 đến 80 ký tự.");
        if (capacity < 1 || capacity > 99) throw new IllegalArgumentException("Mỗi khu có 1-99 ghế trong bản demo.");
        if (price < 1000 || price > 1000000000L) throw new IllegalArgumentException("Giá cơ bản từ 1.000 đến 1 tỷ VND.");
    }

    public List<Order> allOrders(int adminId) {
        check(adminId);
        synchronized (store) {
            List<Order> result = new ArrayList<Order>(store.db.orders);
            result.sort((a, b) -> Integer.compare(b.id, a.id));
            return result;
        }
    }

    public List<PaymentRecord> allPayments(int adminId) {
        check(adminId);
        synchronized (store) {
            List<PaymentRecord> result = new ArrayList<PaymentRecord>(store.db.payments);
            result.sort((a, b) -> Integer.compare(b.id, a.id));
            return result;
        }
    }

    public List<Hold> allHolds(int adminId) {
        check(adminId);
        synchronized (store) {
            store.expireHolds();
            return new ArrayList<Hold>(store.db.holds);
        }
    }

    public void releaseHold(int adminId, int holdId) {
        check(adminId);
        synchronized (store) {
            if (!store.db.holds.removeIf(h -> h.id == holdId))
                throw new IllegalArgumentException("Không tìm thấy lượt giữ ghế #" + holdId);
            store.save();
        }
    }

    public void refundOrder(int adminId, int orderId) {
        check(adminId);
        synchronized (store) {
            booking.refundInTransaction(booking.findOrder(orderId));
            store.save();
        }
    }

    /** Cancel event, release all outstanding holds, refund every previously paid order (demo). */
    public int cancelEvent(int adminId, int eventId) {
        check(adminId);
        synchronized (store) {
            Event event = events.find(eventId);
            if (event.status == EventStatus.CANCELLED) throw new IllegalArgumentException("Trận đấu đã bị hủy trước đó.");
            event.status = EventStatus.CANCELLED;
            store.db.holds.removeIf(h -> h.eventId == eventId);
            int count = 0;
            for (Order o : store.db.orders) {
                if (o.eventId == eventId && o.status == OrderStatus.PAID) {
                    booking.refundInTransaction(o);
                    count++;
                }
            }
            store.save();
            return count;
        }
    }
}
