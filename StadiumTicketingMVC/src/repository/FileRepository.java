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

    public boolean exists() { return Files.exists(this.file); }

    public Database load() {
        Properties p = new Properties();
        try (Reader reader = Files.newBufferedReader(this.file, StandardCharsets.UTF_8)) {
            p.load(reader);
            if (!"1".equals(p.getProperty("format"))) {
                throw new IllegalStateException("Phiên bản dữ liệu không được hỗ trợ");
            }
            Database d = new Database();
            d.setNextUserId(num(p, "next.user"));
            d.setNextEventId(num(p, "next.event"));
            d.setNextHoldId(num(p, "next.hold"));
            d.setNextOrderId(num(p, "next.order"));
            d.setNextPaymentId(num(p, "next.payment"));

            for (int i = 0; i < num(p, "users.count"); i++) {
                String k = "users." + i + ".";
                d.getUsers().add(new User(num(p, k + "id"), Role.valueOf(str(p, k + "role")),
                        str(p, k + "username"), str(p, k + "name"), str(p, k + "salt"),
                        str(p, k + "hash"), Boolean.parseBoolean(str(p, k + "active")), lng(p, k + "created")));
            }
            for (int i = 0; i < num(p, "sections.count"); i++) {
                String k = "sections." + i + ".";
                d.getSections().add(new Section(str(p, k + "code"), str(p, k + "name"),
                        num(p, k + "capacity"), lng(p, k + "price"), Boolean.parseBoolean(str(p, k + "active"))));
            }
            for (int i = 0; i < num(p, "events.count"); i++) {
                String k = "events." + i + ".";
                Event e = new Event(num(p, k + "id"), str(p, k + "home"), str(p, k + "away"),
                        str(p, k + "competition"), lng(p, k + "starts"), EventStatus.valueOf(str(p, k + "status")));
                int n = num(p, k + "priceCount");
                for (int j = 0; j < n; j++) {
                    e.putZonePrice(str(p, k + "zone." + j + ".getCode()"), lng(p, k + "zone." + j + ".price"));
                }
                d.getEvents().add(e);
            }
            for (int i = 0; i < num(p, "holds.count"); i++) {
                String k = "holds." + i + ".";
                d.getHolds().add(new Hold(num(p, k + "id"), num(p, k + "user"), num(p, k + "event"),
                        seats(str(p, k + "seats")), lng(p, k + "total"), lng(p, k + "created"), lng(p, k + "expires")));
            }
            for (int i = 0; i < num(p, "orders.count"); i++) {
                String k = "orders." + i + ".";
                d.getOrders().add(new Order(num(p, k + "id"), num(p, k + "user"), num(p, k + "event"),
                        seats(str(p, k + "seats")), lng(p, k + "total"), lng(p, k + "created"),
                        OrderStatus.valueOf(str(p, k + "status")), str(p, k + "method"), str(p, k + "reference")));
            }
            for (int i = 0; i < num(p, "payments.count"); i++) {
                String k = "payments." + i + ".";
                d.getPayments().add(new PaymentRecord(num(p, k + "id"), num(p, k + "user"), num(p, k + "order"),
                        str(p, k + "type"), str(p, k + "method"), str(p, k + "result"),
                        lng(p, k + "amount"), str(p, k + "reference"), lng(p, k + "created")));
            }
            return d;
        } catch (IOException | RuntimeException ex) {
            throw new IllegalStateException("Không đọc được dữ liệu " + this.file + ". Kiểm tra file, KHÔNG xóa dữ liệu cũ.", ex);
        }
    }

    public void save(Database d) {
        Properties p = new Properties();
        put(p, "format", 1);
        put(p, "next.user", d.getNextUserId());
        put(p, "next.event", d.getNextEventId());
        put(p, "next.hold", d.getNextHoldId());
        put(p, "next.order", d.getNextOrderId());
        put(p, "next.payment", d.getNextPaymentId());
        put(p, "users.count", d.getUsers().size());
        for (int i = 0; i < d.getUsers().size(); i++) {
            User u = d.getUsers().get(i);
            String k = "users." + i + ".";
            put(p, k + "id", u.getId()); put(p, k + "role", u.getRole());
            put(p, k + "username", u.getUsername()); put(p, k + "name", u.getName());
            put(p, k + "salt", u.getSalt()); put(p, k + "hash", u.getPasswordHash());
            put(p, k + "active", u.getActive()); put(p, k + "created", u.getCreatedAt());
        }
        put(p, "sections.count", d.getSections().size());
        for (int i = 0; i < d.getSections().size(); i++) {
            Section s = d.getSections().get(i);
            String k = "sections." + i + ".";
            put(p, k + "code", s.getCode()); put(p, k + "name", s.getName());
            put(p, k + "capacity", s.getCapacity()); put(p, k + "price", s.getBasePrice());
            put(p, k + "active", s.getActive());
        }
        put(p, "events.count", d.getEvents().size());
        for (int i = 0; i < d.getEvents().size(); i++) {
            Event e = d.getEvents().get(i);
            String k = "events." + i + ".";
            put(p, k + "id", e.getId()); put(p, k + "home", e.getHome());
            put(p, k + "away", e.getAway()); put(p, k + "competition", e.getCompetition());
            put(p, k + "starts", e.getStartsAt()); put(p, k + "status", e.getStatus());
            put(p, k + "priceCount", e.getZonePrices().size());
            int j = 0;
            for (Map.Entry<String, Long> en : e.getZonePrices().entrySet()) {
                put(p, k + "zone." + j + ".getCode()", en.getKey());
                put(p, k + "zone." + j + ".price", en.getValue());
                j++;
            }
        }
        put(p, "holds.count", d.getHolds().size());
        for (int i = 0; i < d.getHolds().size(); i++) {
            Hold h = d.getHolds().get(i);
            String k = "holds." + i + ".";
            put(p, k + "id", h.getId()); put(p, k + "user", h.getUserId());
            put(p, k + "event", h.getEventId()); put(p, k + "seats", String.join(",", h.getSeatIds()));
            put(p, k + "total", h.getTotal()); put(p, k + "created", h.getCreatedAt());
            put(p, k + "expires", h.getExpiresAt());
        }
        put(p, "orders.count", d.getOrders().size());
        for (int i = 0; i < d.getOrders().size(); i++) {
            Order o = d.getOrders().get(i);
            String k = "orders." + i + ".";
            put(p, k + "id", o.getId()); put(p, k + "user", o.getUserId());
            put(p, k + "event", o.getEventId()); put(p, k + "seats", String.join(",", o.getSeatIds()));
            put(p, k + "total", o.getTotal()); put(p, k + "created", o.getPurchasedAt());
            put(p, k + "status", o.getStatus()); put(p, k + "method", o.getPaymentMethod());
            put(p, k + "reference", o.getPaymentReference());
        }
        put(p, "payments.count", d.getPayments().size());
        for (int i = 0; i < d.getPayments().size(); i++) {
            PaymentRecord r = d.getPayments().get(i);
            String k = "payments." + i + ".";
            put(p, k + "id", r.getId()); put(p, k + "user", r.getUserId());
            put(p, k + "order", r.getOrderId()); put(p, k + "type", r.getType());
            put(p, k + "method", r.getMethod()); put(p, k + "result", r.getResult());
            put(p, k + "amount", r.getAmount()); put(p, k + "reference", r.getReference());
            put(p, k + "created", r.getCreatedAt());
        }
        try {
            Files.createDirectories(this.file.getParent());
            Path temp = Files.createTempFile(this.file.getParent(), "stadium-", ".tmp");
            try {
                try (Writer writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                    p.store(writer, "Stadium ticketing demo - UTF-8");
                }
                try {
                    Files.move(temp, this.file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } catch (AtomicMoveNotSupportedException ex) {
                    Files.move(temp, this.file, StandardCopyOption.REPLACE_EXISTING);
                }
            } finally {
                Files.deleteIfExists(temp);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Không lưu được dữ liệu vào " + this.file, ex);
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
