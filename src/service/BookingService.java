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
        this.auth.requireActive(userId, Role.FAN); // admin uses a different authorized entry point below
        return this.seatMapInternal(eventId, userId);
    }

    public Map<String, Character> seatMapAdmin(int adminId, int eventId) {
        this.auth.requireActive(adminId, Role.ADMIN);
        return this.seatMapInternal(eventId, 0);
    }

    private Map<String, Character> seatMapInternal(int eventId, int viewerId) {
        synchronized (this.store) {
            this.store.expireHolds();
            this.events.find(eventId);
            Map<String, Character> states = new LinkedHashMap<String, Character>();
            for (Section section : this.store.getDatabase().getSections()) {
                for (int i = 1; i <= section.getCapacity(); i++) {
                    states.put(seatId(section.getCode(), i), section.getActive() ? 'O' : '-');
                }
            }
            for (Order order : this.store.getDatabase().getOrders()) {
                if (order.getEventId() == eventId && order.getStatus() == OrderStatus.PAID)
                    for (String s : order.getSeatIds()) states.put(s, 'B');
            }
            for (Hold hold : this.store.getDatabase().getHolds()) {
                if (hold.getEventId() == eventId)
                    for (String s : hold.getSeatIds()) states.put(s, hold.getUserId() == viewerId ? 'Y' : 'L');
            }
            return states;
        }
    }

    public Hold reserve(int fanId, int eventId, List<String> rawSeats) {
        this.auth.requireActive(fanId, Role.FAN);
        if (rawSeats == null || rawSeats.isEmpty() || rawSeats.size() > 4)
            throw new IllegalArgumentException("Chọn từ 1 đến 4 ghế mỗi lượt thanh toán.");
        List<String> chosen = new ArrayList<String>();
        Set<String> unique = new HashSet<String>();
        for (String seat : rawSeats) {
            String normalized = seat.trim().toUpperCase(Locale.ROOT);
            if (!unique.add(normalized)) throw new IllegalArgumentException("Ghế bị nhập trùng: " + normalized);
            chosen.add(normalized);
        }
        synchronized (this.store) {
            this.store.expireHolds();
            Event event = this.events.find(eventId);
            if (event.getStatus() != EventStatus.OPEN || event.getStartsAt() <= this.store.now())
                throw new IllegalArgumentException("Trận đấu hiện không mở bán vé.");
            for (Hold h : this.store.getDatabase().getHolds()) {
                if (h.getUserId() == fanId) throw new IllegalArgumentException("Bạn có một lượt giữ ghế chưa hoàn tất. Thanh toán hoặc hủy trước.");
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
                Section section = this.events.section(code);
                if (!section.getActive() || number < 1 || number > section.getCapacity() || !seat.equals(seatId(section.getCode(), number)))
                    throw new IllegalArgumentException("Ghế không tồn tại hoặc khán đài đang đóng: " + seat);
                if (this.isUnavailable(eventId, seat)) throw new IllegalArgumentException("Ghế " + seat + " đã bán hoặc đang được giữ.");
                total = Math.addExact(total, this.events.price(event, section));
            }
            long now = this.store.now();
            Hold hold = new Hold(this.store.getDatabase().takeNextHoldId(), fanId, eventId, chosen, total, now, now + HOLD_MILLIS);
            this.store.getDatabase().getHolds().add(hold);
            this.store.save();
            return hold;
        }
    }

    private boolean isUnavailable(int eventId, String seat) {
        for (Order order : this.store.getDatabase().getOrders())
            if (order.getEventId() == eventId && order.getStatus() == OrderStatus.PAID && order.getSeatIds().contains(seat)) return true;
        for (Hold hold : this.store.getDatabase().getHolds())
            if (hold.getEventId() == eventId && hold.getSeatIds().contains(seat)) return true;
        return false;
    }

    public Hold currentHold(int fanId) {
        this.auth.requireActive(fanId, Role.FAN);
        synchronized (this.store) {
            this.store.expireHolds();
            for (Hold h : this.store.getDatabase().getHolds()) if (h.getUserId() == fanId) return h;
            return null;
        }
    }

    public void release(int fanId) {
        this.auth.requireActive(fanId, Role.FAN);
        synchronized (this.store) {
            if (this.store.getDatabase().getHolds().removeIf(h -> h.getUserId() == fanId)) this.store.save();
        }
    }

    public void releaseOnLogout(int fanId) {
        synchronized (this.store) {
            if (this.store.getDatabase().getHolds().removeIf(h -> h.getUserId() == fanId)) this.store.save();
        }
    }

    /** A demo payment result, not an actual payment gateway. Failure leaves hold available for retry. */
    public Order checkout(int fanId, String method, boolean simulateSuccess) {
        this.auth.requireActive(fanId, Role.FAN);
        if (!method.equals("VI_DIEN_TU_DEMO") && !method.equals("CHUYEN_KHOAN_DEMO"))
            throw new IllegalArgumentException("Phương thức thanh toán không được hỗ trợ.");
        synchronized (this.store) {
            this.store.expireHolds();
            Hold hold = null;
            for (Hold h : this.store.getDatabase().getHolds()) if (h.getUserId() == fanId) { hold = h; break; }
            if (hold == null) throw new IllegalArgumentException("Không còn ghế được giữ, vui lòng đặt lại.");
            Event event = this.events.find(hold.getEventId());
            if (event.getStatus() == EventStatus.CANCELLED || event.getStartsAt() <= this.store.now())
                throw new IllegalArgumentException("Trận đấu đã hủy hoặc bắt đầu, không thể thanh toán.");
            int paymentId = this.store.getDatabase().takeNextPaymentId();
            String reference = "DEMO-TX-" + paymentId;
            if (!simulateSuccess) {
                this.store.getDatabase().getPayments().add(new PaymentRecord(paymentId, fanId, 0, "PAYMENT", method,
                        "FAILED", hold.getTotal(), reference, this.store.now()));
                this.store.save();
                throw new IllegalArgumentException("Giao dịch mô phỏng thất bại. Ghế vẫn được giữ đến hết hạn.");
            }
            // Recheck immediately before issuing ticket; synchronized(store) excludes another sale in this JVM.
            for (String seat : hold.getSeatIds()) {
                for (Order order : this.store.getDatabase().getOrders()) {
                    if (order.getEventId() == hold.getEventId() && order.getStatus() == OrderStatus.PAID && order.getSeatIds().contains(seat))
                        throw new IllegalStateException("Phát hiện ghế đã bán: " + seat);
                }
            }
            Order order = new Order(this.store.getDatabase().takeNextOrderId(), fanId, hold.getEventId(), hold.getSeatIds(), hold.getTotal(),
                    this.store.now(), OrderStatus.PAID, method, reference);
            this.store.getDatabase().getOrders().add(order);
            this.store.getDatabase().getPayments().add(new PaymentRecord(paymentId, fanId, order.getId(), "PAYMENT", method,
                    "SUCCESS", order.getTotal(), reference, this.store.now()));
            this.store.getDatabase().getHolds().remove(hold);
            this.store.save();
            return order;
        }
    }

    public List<Order> ordersForFan(int fanId) {
        this.auth.requireActive(fanId, Role.FAN);
        synchronized (this.store) {
            List<Order> result = new ArrayList<Order>();
            for (Order o : this.store.getDatabase().getOrders()) if (o.getUserId() == fanId) result.add(o);
            result.sort((a, b) -> Integer.compare(b.getId(), a.getId()));
            return result;
        }
    }

    public List<PaymentRecord> paymentsForFan(int fanId) {
        this.auth.requireActive(fanId, Role.FAN);
        synchronized (this.store) {
            List<PaymentRecord> result = new ArrayList<PaymentRecord>();
            for (PaymentRecord r : this.store.getDatabase().getPayments()) if (r.getUserId() == fanId) result.add(r);
            result.sort((a, b) -> Integer.compare(b.getId(), a.getId()));
            return result;
        }
    }

    public void requestRefund(int fanId, int orderId) {
        this.auth.requireActive(fanId, Role.FAN);
        synchronized (this.store) {
            Order order = this.findOrder(orderId);
            if (order.getUserId() != fanId) throw new IllegalArgumentException("Vé này không thuộc tài khoản của bạn.");
            Event event = this.events.find(order.getEventId());
            if (event.getStartsAt() - this.store.now() < 24 * 60 * 60 * 1000L)
                throw new IllegalArgumentException("Chỉ có thể hủy vé trước giờ thi đấu ít nhất 24 giờ.");
            this.refundInTransaction(order);
            this.store.save();
        }
    }

    void refundInTransaction(Order order) {
        if (order.getStatus() != OrderStatus.PAID) throw new IllegalArgumentException("Đơn đã hoàn tiền hoặc không còn hiệu lực.");
        order.setStatus(OrderStatus.REFUNDED);
        int id = this.store.getDatabase().takeNextPaymentId();
        this.store.getDatabase().getPayments().add(new PaymentRecord(id, order.getUserId(), order.getId(), "REFUND", order.getPaymentMethod(),
                "SUCCESS", -order.getTotal(), "DEMO-REF-" + id, this.store.now()));
    }

    public Order findOrder(int orderId) {
        synchronized (this.store) {
            for (Order o : this.store.getDatabase().getOrders()) if (o.getId() == orderId) return o;
            throw new IllegalArgumentException("Không tìm thấy đơn hàng #" + orderId);
        }
    }

    public static String seatId(String zone, int number) { return String.format(Locale.ROOT, "%s-%02d", zone, number); }
}
