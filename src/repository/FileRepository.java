package repository;

import model.*;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Local durable storage; one atomic snapshot, no outside dependencies. */
public class FileRepository {
    private final Path file;

    public FileRepository(Path dataDirectory) {
        this.file = dataDirectory.resolve("stadium.properties");
    }

    public boolean exists() { return Files.exists(file); }

    public Database load() {
        Properties p = new Properties();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            p.load(reader);
            if (!"1".equals(p.getProperty("format"))) {
                throw new IllegalStateException("Phiên bản dữ liệu không được hỗ trợ");
            }
            Database d = new Database();
            d.nextUserId = num(p, "next.user");
            d.nextEventId = num(p, "next.event");
            d.nextHoldId = num(p, "next.hold");
            d.nextOrderId = num(p, "next.order");
            d.nextPaymentId = num(p, "next.payment");

            for (int i = 0; i < num(p, "users.count"); i++) {
                String k = "users." + i + ".";
                d.users.add(new User(num(p, k + "id"), Role.valueOf(str(p, k + "role")),
                        str(p, k + "username"), str(p, k + "name"), str(p, k + "salt"),
                        str(p, k + "hash"), Boolean.parseBoolean(str(p, k + "active")), lng(p, k + "created")));
            }
            for (int i = 0; i < num(p, "sections.count"); i++) {
                String k = "sections." + i + ".";
                d.sections.add(new Section(str(p, k + "code"), str(p, k + "name"),
                        num(p, k + "capacity"), lng(p, k + "price"), Boolean.parseBoolean(str(p, k + "active"))));
            }
            for (int i = 0; i < num(p, "events.count"); i++) {
                String k = "events." + i + ".";
                Event e = new Event(num(p, k + "id"), str(p, k + "home"), str(p, k + "away"),
                        str(p, k + "competition"), lng(p, k + "starts"), EventStatus.valueOf(str(p, k + "status")));
                int n = num(p, k + "priceCount");
                for (int j = 0; j < n; j++) {
                    e.zonePrices.put(str(p, k + "zone." + j + ".code"), lng(p, k + "zone." + j + ".price"));
                }
                d.events.add(e);
            }
            for (int i = 0; i < num(p, "holds.count"); i++) {
                String k = "holds." + i + ".";
                d.holds.add(new Hold(num(p, k + "id"), num(p, k + "user"), num(p, k + "event"),
                        seats(str(p, k + "seats")), lng(p, k + "total"), lng(p, k + "created"), lng(p, k + "expires")));
            }
            for (int i = 0; i < num(p, "orders.count"); i++) {
                String k = "orders." + i + ".";
                d.orders.add(new Order(num(p, k + "id"), num(p, k + "user"), num(p, k + "event"),
                        seats(str(p, k + "seats")), lng(p, k + "total"), lng(p, k + "created"),
                        OrderStatus.valueOf(str(p, k + "status")), str(p, k + "method"), str(p, k + "reference")));
            }
            for (int i = 0; i < num(p, "payments.count"); i++) {
                String k = "payments." + i + ".";
                d.payments.add(new PaymentRecord(num(p, k + "id"), num(p, k + "user"), num(p, k + "order"),
                        str(p, k + "type"), str(p, k + "method"), str(p, k + "result"),
                        lng(p, k + "amount"), str(p, k + "reference"), lng(p, k + "created")));
            }
            return d;
        } catch (IOException | RuntimeException ex) {
            throw new IllegalStateException("Không đọc được dữ liệu " + file + ". Kiểm tra file, KHÔNG xóa dữ liệu cũ.", ex);
        }
    }

    public void save(Database d) {
        Properties p = new Properties();
        put(p, "format", 1);
        put(p, "next.user", d.nextUserId);
        put(p, "next.event", d.nextEventId);
        put(p, "next.hold", d.nextHoldId);
        put(p, "next.order", d.nextOrderId);
        put(p, "next.payment", d.nextPaymentId);
        put(p, "users.count", d.users.size());
        for (int i = 0; i < d.users.size(); i++) {
            User u = d.users.get(i);
            String k = "users." + i + ".";
            put(p, k + "id", u.id); put(p, k + "role", u.role);
            put(p, k + "username", u.username); put(p, k + "name", u.name);
            put(p, k + "salt", u.salt); put(p, k + "hash", u.passwordHash);
            put(p, k + "active", u.active); put(p, k + "created", u.createdAt);
        }
        put(p, "sections.count", d.sections.size());
        for (int i = 0; i < d.sections.size(); i++) {
            Section s = d.sections.get(i);
            String k = "sections." + i + ".";
            put(p, k + "code", s.code); put(p, k + "name", s.name);
            put(p, k + "capacity", s.capacity); put(p, k + "price", s.basePrice);
            put(p, k + "active", s.active);
        }
        put(p, "events.count", d.events.size());
        for (int i = 0; i < d.events.size(); i++) {
            Event e = d.events.get(i);
            String k = "events." + i + ".";
            put(p, k + "id", e.id); put(p, k + "home", e.home);
            put(p, k + "away", e.away); put(p, k + "competition", e.competition);
            put(p, k + "starts", e.startsAt); put(p, k + "status", e.status);
            put(p, k + "priceCount", e.zonePrices.size());
            int j = 0;
            for (Map.Entry<String, Long> en : e.zonePrices.entrySet()) {
                put(p, k + "zone." + j + ".code", en.getKey());
                put(p, k + "zone." + j + ".price", en.getValue());
                j++;
            }
        }
        put(p, "holds.count", d.holds.size());
        for (int i = 0; i < d.holds.size(); i++) {
            Hold h = d.holds.get(i);
            String k = "holds." + i + ".";
            put(p, k + "id", h.id); put(p, k + "user", h.userId);
            put(p, k + "event", h.eventId); put(p, k + "seats", String.join(",", h.seatIds));
            put(p, k + "total", h.total); put(p, k + "created", h.createdAt);
            put(p, k + "expires", h.expiresAt);
        }
        put(p, "orders.count", d.orders.size());
        for (int i = 0; i < d.orders.size(); i++) {
            Order o = d.orders.get(i);
            String k = "orders." + i + ".";
            put(p, k + "id", o.id); put(p, k + "user", o.userId);
            put(p, k + "event", o.eventId); put(p, k + "seats", String.join(",", o.seatIds));
            put(p, k + "total", o.total); put(p, k + "created", o.purchasedAt);
            put(p, k + "status", o.status); put(p, k + "method", o.paymentMethod);
            put(p, k + "reference", o.paymentReference);
        }
        put(p, "payments.count", d.payments.size());
        for (int i = 0; i < d.payments.size(); i++) {
            PaymentRecord r = d.payments.get(i);
            String k = "payments." + i + ".";
            put(p, k + "id", r.id); put(p, k + "user", r.userId);
            put(p, k + "order", r.orderId); put(p, k + "type", r.type);
            put(p, k + "method", r.method); put(p, k + "result", r.result);
            put(p, k + "amount", r.amount); put(p, k + "reference", r.reference);
            put(p, k + "created", r.createdAt);
        }
        try {
            Files.createDirectories(file.getParent());
            Path temp = Files.createTempFile(file.getParent(), "stadium-", ".tmp");
            try {
                try (Writer writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                    p.store(writer, "Stadium ticketing demo - UTF-8");
                }
                try {
                    Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } catch (AtomicMoveNotSupportedException ex) {
                    Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
                }
            } finally {
                Files.deleteIfExists(temp);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Không lưu được dữ liệu vào " + file, ex);
        }
    }

    private static void put(Properties p, String key, Object value) { p.setProperty(key, String.valueOf(value)); }
    private static String str(Properties p, String key) {
        String value = p.getProperty(key);
        if (value == null) throw new IllegalStateException("Thiếu trường dữ liệu: " + key);
        return value;
    }
    private static int num(Properties p, String key) { return Integer.parseInt(str(p, key)); }
    private static long lng(Properties p, String key) { return Long.parseLong(str(p, key)); }
    private static List<String> seats(String value) {
        if (value.isEmpty()) return new ArrayList<String>();
        return new ArrayList<String>(Arrays.asList(value.split(",")));
    }
}
