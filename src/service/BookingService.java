package service;

import model.*;
import java.util.*;

/** Seat lifecycle: AVAILABLE -> HELD (5 min) -> PAID, or HELD -> AVAILABLE on expiry/cancel. */
public class BookingService {
    public static final long HOLD_MILLIS = 5 * 60 * 1000L;
    private final Store store;
    private final AuthService auth;
    private final EventService events;

    public BookingService(Store store, AuthService auth, EventService events) {
        this.store = store;
        this.auth = auth;
        this.events = events;
    }

    public Map<String, Character> seatMap(int userId, int eventId) {
        auth.requireActive(userId, Role.FAN); // admin uses a different authorized entry point below
        return seatMapInternal(eventId, userId);
    }

    public Map<String, Character> seatMapAdmin(int adminId, int eventId) {
        auth.requireActive(adminId, Role.ADMIN);
        return seatMapInternal(eventId, 0);
    }

    private Map<String, Character> seatMapInternal(int eventId, int viewerId) {
        synchronized (store) {
            store.expireHolds();
            events.find(eventId);
            Map<String, Character> states = new LinkedHashMap<String, Character>();
            for (Section section : store.db.sections) {
                for (int i = 1; i <= section.capacity; i++) {
                    states.put(seatId(section.code, i), section.active ? 'O' : '-');
                }
            }
            for (Order order : store.db.orders) {
                if (order.eventId == eventId && order.status == OrderStatus.PAID)
                    for (String s : order.seatIds) states.put(s, 'B');
            }
            for (Hold hold : store.db.holds) {
                if (hold.eventId == eventId)
                    for (String s : hold.seatIds) states.put(s, hold.userId == viewerId ? 'Y' : 'L');
            }
            return states;
        }
    }

    public Hold reserve(int fanId, int eventId, List<String> rawSeats) {
        auth.requireActive(fanId, Role.FAN);
        if (rawSeats == null || rawSeats.isEmpty() || rawSeats.size() > 4)
            throw new IllegalArgumentException("Chọn từ 1 đến 4 ghế mỗi lượt thanh toán.");
        List<String> chosen = new ArrayList<String>();
        Set<String> unique = new HashSet<String>();
        for (String seat : rawSeats) {
            String normalized = seat.trim().toUpperCase(Locale.ROOT);
            if (!unique.add(normalized)) throw new IllegalArgumentException("Ghế bị nhập trùng: " + normalized);
            chosen.add(normalized);
        }
        synchronized (store) {
            store.expireHolds();
            Event event = events.find(eventId);
            if (event.status != EventStatus.OPEN || event.startsAt <= store.now())
                throw new IllegalArgumentException("Trận đấu hiện không mở bán vé.");
            for (Hold h : store.db.holds) {
                if (h.userId == fanId) throw new IllegalArgumentException("Bạn có một lượt giữ ghế chưa hoàn tất. Thanh toán hoặc hủy trước.");
            }
            long total = 0;
            for (String seat : chosen) {
                int hyphen = seat.lastIndexOf('-');
                if (hyphen < 1 || hyphen == seat.length() - 1)
                    throw new IllegalArgumentException("Mã ghế không hợp lệ: " + seat + " (ví dụ: A-01)");
                String code = seat.substring(0, hyphen);
                int number;
                try { number = Integer.parseInt(seat.substring(hyphen + 1)); }
                catch (NumberFormatException ex) { throw new IllegalArgumentException("Mã ghế không hợp lệ: " + seat); }
                Section section = events.section(code);
                if (!section.active || number < 1 || number > section.capacity || !seat.equals(seatId(section.code, number)))
                    throw new IllegalArgumentException("Ghế không tồn tại hoặc khán đài đang đóng: " + seat);
                if (isUnavailable(eventId, seat)) throw new IllegalArgumentException("Ghế " + seat + " đã bán hoặc đang được giữ.");
                total = Math.addExact(total, events.price(event, section));
            }
            long now = store.now();
            Hold hold = new Hold(store.db.nextHoldId++, fanId, eventId, chosen, total, now, now + HOLD_MILLIS);
            store.db.holds.add(hold);
            store.save();
            return hold;
        }
    }

    private boolean isUnavailable(int eventId, String seat) {
        for (Order order : store.db.orders)
            if (order.eventId == eventId && order.status == OrderStatus.PAID && order.seatIds.contains(seat)) return true;
        for (Hold hold : store.db.holds)
            if (hold.eventId == eventId && hold.seatIds.contains(seat)) return true;
        return false;
    }

    public Hold currentHold(int fanId) {
        auth.requireActive(fanId, Role.FAN);
        synchronized (store) {
            store.expireHolds();
            for (Hold h : store.db.holds) if (h.userId == fanId) return h;
            return null;
        }
    }

    public void release(int fanId) {
        auth.requireActive(fanId, Role.FAN);
        synchronized (store) {
            if (store.db.holds.removeIf(h -> h.userId == fanId)) store.save();
        }
    }

    public void releaseOnLogout(int fanId) {
        synchronized (store) {
            if (store.db.holds.removeIf(h -> h.userId == fanId)) store.save();
        }
    }

    /** A demo payment result, not an actual payment gateway. Failure leaves hold available for retry. */
    public Order checkout(int fanId, String method, boolean simulateSuccess) {
        auth.requireActive(fanId, Role.FAN);
        if (!method.equals("VI_DIEN_TU_DEMO") && !method.equals("CHUYEN_KHOAN_DEMO"))
            throw new IllegalArgumentException("Phương thức thanh toán không được hỗ trợ.");
        synchronized (store) {
            store.expireHolds();
            Hold hold = null;
            for (Hold h : store.db.holds) if (h.userId == fanId) { hold = h; break; }
            if (hold == null) throw new IllegalArgumentException("Không còn ghế được giữ, vui lòng đặt lại.");
            Event event = events.find(hold.eventId);
            if (event.status == EventStatus.CANCELLED || event.startsAt <= store.now())
                throw new IllegalArgumentException("Trận đấu đã hủy hoặc bắt đầu, không thể thanh toán.");
            int paymentId = store.db.nextPaymentId++;
            String reference = "DEMO-TX-" + paymentId;
            if (!simulateSuccess) {
                store.db.payments.add(new PaymentRecord(paymentId, fanId, 0, "PAYMENT", method,
                        "FAILED", hold.total, reference, store.now()));
                store.save();
                throw new IllegalArgumentException("Giao dịch mô phỏng thất bại. Ghế vẫn được giữ đến hết hạn.");
            }
            // Recheck immediately before issuing ticket; synchronized(store) excludes another sale in this JVM.
            for (String seat : hold.seatIds) {
                for (Order order : store.db.orders) {
                    if (order.eventId == hold.eventId && order.status == OrderStatus.PAID && order.seatIds.contains(seat))
                        throw new IllegalStateException("Phát hiện ghế đã bán: " + seat);
                }
            }
            Order order = new Order(store.db.nextOrderId++, fanId, hold.eventId, hold.seatIds, hold.total,
                    store.now(), OrderStatus.PAID, method, reference);
            store.db.orders.add(order);
            store.db.payments.add(new PaymentRecord(paymentId, fanId, order.id, "PAYMENT", method,
                    "SUCCESS", order.total, reference, store.now()));
            store.db.holds.remove(hold);
            store.save();
            return order;
        }
    }

    public List<Order> ordersForFan(int fanId) {
        auth.requireActive(fanId, Role.FAN);
        synchronized (store) {
            List<Order> result = new ArrayList<Order>();
            for (Order o : store.db.orders) if (o.userId == fanId) result.add(o);
            result.sort((a, b) -> Integer.compare(b.id, a.id));
            return result;
        }
    }

    public List<PaymentRecord> paymentsForFan(int fanId) {
        auth.requireActive(fanId, Role.FAN);
        synchronized (store) {
            List<PaymentRecord> result = new ArrayList<PaymentRecord>();
            for (PaymentRecord r : store.db.payments) if (r.userId == fanId) result.add(r);
            result.sort((a, b) -> Integer.compare(b.id, a.id));
            return result;
        }
    }

    public void requestRefund(int fanId, int orderId) {
        auth.requireActive(fanId, Role.FAN);
        synchronized (store) {
            Order order = findOrder(orderId);
            if (order.userId != fanId) throw new IllegalArgumentException("Vé này không thuộc tài khoản của bạn.");
            Event event = events.find(order.eventId);
            if (event.startsAt - store.now() < 24 * 60 * 60 * 1000L)
                throw new IllegalArgumentException("Chỉ có thể hủy vé trước giờ thi đấu ít nhất 24 giờ.");
            refundInTransaction(order);
            store.save();
        }
    }

    void refundInTransaction(Order order) {
        if (order.status != OrderStatus.PAID) throw new IllegalArgumentException("Đơn đã hoàn tiền hoặc không còn hiệu lực.");
        order.status = OrderStatus.REFUNDED;
        int id = store.db.nextPaymentId++;
        store.db.payments.add(new PaymentRecord(id, order.userId, order.id, "REFUND", order.paymentMethod,
                "SUCCESS", -order.total, "DEMO-REF-" + id, store.now()));
    }

    public Order findOrder(int orderId) {
        synchronized (store) {
            for (Order o : store.db.orders) if (o.id == orderId) return o;
            throw new IllegalArgumentException("Không tìm thấy đơn hàng #" + orderId);
        }
    }

    public static String seatId(String zone, int number) { return String.format(Locale.ROOT, "%s-%02d", zone, number); }
}
