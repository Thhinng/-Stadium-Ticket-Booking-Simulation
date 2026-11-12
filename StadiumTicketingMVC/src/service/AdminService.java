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

    private void check(int adminId) { this.auth.requireActive(adminId, Role.ADMIN); }

    public Dashboard dashboard(int adminId) {
        this.check(adminId);
        synchronized (this.store) {
            this.store.expireHolds();
            Dashboard d = new Dashboard();
            for (User u : this.store.getDatabase().getUsers()) {
                if (u.getRole() == Role.FAN) {
                    d.setTotalFans(d.getTotalFans() + 1);
                    if (u.getActive()) d.setActiveFans(d.getActiveFans() + 1);
                }
            }
            for (Event e : this.store.getDatabase().getEvents()) {
                d.setTotalEvents(d.getTotalEvents() + 1);
                if (e.getStatus() == EventStatus.OPEN && e.getStartsAt() > this.store.now()) d.setOpenEvents(d.getOpenEvents() + 1);
            }
            for (Order o : this.store.getDatabase().getOrders()) {
                if (o.getStatus() == OrderStatus.PAID) d.setPaidOrders(d.getPaidOrders() + 1);
                else d.setRefundedOrders(d.getRefundedOrders() + 1);
            }
            for (PaymentRecord p : this.store.getDatabase().getPayments()) if (p.getResult().equals("SUCCESS")) d.setNetRevenue(d.getNetRevenue() + p.getAmount());
            d.setActiveHolds(this.store.getDatabase().getHolds().size());
            return d;
        }
    }

    public List<User> fans(int adminId, String keyword) {
        this.check(adminId);
        String q = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        synchronized (this.store) {
            List<User> result = new ArrayList<User>();
            for (User u : this.store.getDatabase().getUsers()) {
                if (u.getRole() == Role.FAN && (u.getUsername().toLowerCase(Locale.ROOT).contains(q)
                        || u.getName().toLowerCase(Locale.ROOT).contains(q))) result.add(u);
            }
            result.sort(Comparator.comparingInt(u -> u.getId()));
            return result;
        }
    }

    public List<Order> fanOrders(int adminId, int fanId) {
        this.check(adminId);
        User fan = this.auth.getById(fanId);
        if (fan.getRole() != Role.FAN) throw new IllegalArgumentException("Đây không phải tài khoản Fan.");
        synchronized (this.store) {
            List<Order> result = new ArrayList<Order>();
            for (Order o : this.store.getDatabase().getOrders()) if (o.getUserId() == fanId) result.add(o);
            result.sort((a, b) -> Integer.compare(b.getId(), a.getId()));
            return result;
        }
    }

    public void setFanActive(int adminId, int fanId, boolean active) {
        this.check(adminId);
        synchronized (this.store) {
            User fan = this.auth.getById(fanId);
            if (fan.getRole() != Role.FAN) throw new IllegalArgumentException("Chỉ được quản lý tài khoản Fan.");
            fan.setActive(active);
            if (!active) this.store.getDatabase().getHolds().removeIf(h -> h.getUserId() == fanId);
            this.store.save();
        }
    }

    public void resetFanPassword(int adminId, int fanId, String newPassword) {
        this.check(adminId);
        synchronized (this.store) {
            User fan = this.auth.getById(fanId);
            if (fan.getRole() != Role.FAN) throw new IllegalArgumentException("Chỉ được đặt lại mật khẩu cho Fan.");
            this.auth.setPassword(fan, newPassword);
            this.store.save();
        }
    }

    public Section addSection(int adminId, String code, String name, int capacity, long price) {
        this.check(adminId);
        code = code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
        validateSection(code, name, capacity, price);
        synchronized (this.store) {
            for (Section s : this.store.getDatabase().getSections()) if (s.getCode().equals(code))
                throw new IllegalArgumentException("Mã khán đài đã tồn tại.");
            Section added = new Section(code, name.trim(), capacity, price, true);
            this.store.getDatabase().getSections().add(added);
            for (Event e : this.store.getDatabase().getEvents()) e.putZonePrice(code, price);
            this.store.save();
            return added;
        }
    }

    public void editSection(int adminId, String code, String name, int capacity, long price, boolean active) {
        this.check(adminId);
        synchronized (this.store) {
            Section section = this.events.section(code);
            validateSection(section.getCode(), name, capacity, price);
            if (capacity < section.getCapacity()) {
                for (Order o : this.store.getDatabase().getOrders())
                    if (o.getStatus() == OrderStatus.PAID && hasSeatAbove(o.getSeatIds(), section.getCode(), capacity))
                        throw new IllegalArgumentException("Không thể giảm sức chứa: có ghế đã bán ngoài phạm vi mới.");
                this.store.expireHolds();
                for (Hold h : this.store.getDatabase().getHolds())
                    if (hasSeatAbove(h.getSeatIds(), section.getCode(), capacity))
                        throw new IllegalArgumentException("Không thể giảm sức chứa: có ghế đang được giữ ngoài phạm vi mới.");
            }
            section.setName(name.trim());
            section.setCapacity(capacity);
            section.setBasePrice(price); // affects new events; existing event prices configured separately
            section.setActive(active);
            this.store.save();
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
        this.check(adminId);
        synchronized (this.store) {
            List<Order> result = new ArrayList<Order>(this.store.getDatabase().getOrders());
            result.sort((a, b) -> Integer.compare(b.getId(), a.getId()));
            return result;
        }
    }

    public List<PaymentRecord> allPayments(int adminId) {
        this.check(adminId);
        synchronized (this.store) {
            List<PaymentRecord> result = new ArrayList<PaymentRecord>(this.store.getDatabase().getPayments());
            result.sort((a, b) -> Integer.compare(b.getId(), a.getId()));
            return result;
        }
    }

    public List<Hold> allHolds(int adminId) {
        this.check(adminId);
        synchronized (this.store) {
            this.store.expireHolds();
            return new ArrayList<Hold>(this.store.getDatabase().getHolds());
        }
    }

    public void releaseHold(int adminId, int holdId) {
        this.check(adminId);
        synchronized (this.store) {
            if (!this.store.getDatabase().getHolds().removeIf(h -> h.getId() == holdId))
                throw new IllegalArgumentException("Không tìm thấy lượt giữ ghế #" + holdId);
            this.store.save();
        }
    }

    public void refundOrder(int adminId, int orderId) {
        this.check(adminId);
        synchronized (this.store) {
            this.booking.refundInTransaction(this.booking.findOrder(orderId));
            this.store.save();
        }
    }

    /** Cancel event, release all outstanding holds, refund every previously paid order (demo). */
    public int cancelEvent(int adminId, int eventId) {
        this.check(adminId);
        synchronized (this.store) {
            Event event = this.events.find(eventId);
            if (event.getStatus() == EventStatus.CANCELLED) throw new IllegalArgumentException("Trận đấu đã bị hủy trước đó.");
            event.setStatus(EventStatus.CANCELLED);
            this.store.getDatabase().getHolds().removeIf(h -> h.getEventId() == eventId);
            int count = 0;
            for (Order o : this.store.getDatabase().getOrders()) {
                if (o.getEventId() == eventId && o.getStatus() == OrderStatus.PAID) {
                    this.booking.refundInTransaction(o);
                    count++;
                }
            }
            this.store.save();
            return count;
        }
    }
}
