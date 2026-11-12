package service;

import model.*;
import repository.FileRepository;
import util.PasswordUtil;
import util.TimeUtil;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Iterator;

/** Shared synchronization boundary for all services in this one-process console app. */
public class Store {
    private final Database db;
    private final FileRepository repository;
    private final Clock clock;

    public Store(FileRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
        if (repository.exists()) {
            this.db = repository.load();
        } else {
            this.db = initialDatabase(clock);
            repository.save(this.db);
        }
        synchronized (this) { this.expireHolds(); }
    }

    Database getDatabase() { return this.db; }

    long now() { return this.clock.millis(); }
    void save() { this.repository.save(this.db); }

    boolean expireHolds() {
        boolean changed = false;
        Iterator<Hold> it = this.db.getHolds().iterator();
        while (it.hasNext()) {
            Hold hold = it.next();
            if (hold.getExpiresAt() <= this.now()) {
                it.remove();
                changed = true;
            }
        }
        if (changed) this.save();
        return changed;
    }

    static Database initialDatabase(Clock clock) {
        Database db = new Database();
        String salt = PasswordUtil.newSalt();
        db.getUsers().add(new User(db.takeNextUserId(), Role.ADMIN, "admin", "Quản trị viên", salt,
                PasswordUtil.hash("Admin@123", salt), true, clock.millis()));
        db.getSections().add(new Section("A", "Khán đài A (VIP)", 16, 450000, true));
        db.getSections().add(new Section("B", "Khán đài B", 24, 250000, true));
        db.getSections().add(new Section("C", "Khán đài C", 32, 120000, true));
        String[][] matches = {
                {"Hà Nội FC", "Bình Định FC", "V.League 1"},
                {"Đà Nẵng FC", "Hải Phòng FC", "V.League 1"},
                {"Công An Hà Nội", "Nam Định FC", "V.League 1"},
                {"U23 Việt Nam", "U23 Thái Lan", "Giao hữu quốc tế"}
        };
        int[] afterDays = {2, 5, 10, 20};
        LocalDate localToday = LocalDate.now(clock.withZone(TimeUtil.ZONE));
        for (int i = 0; i < matches.length; i++) {
            long start = localToday.plusDays(afterDays[i]).atTime(LocalTime.of(19, 30))
                    .atZone(TimeUtil.ZONE).toInstant().toEpochMilli();
            Event e = new Event(db.takeNextEventId(), matches[i][0], matches[i][1], matches[i][2], start, EventStatus.OPEN);
            for (Section s : db.getSections()) e.putZonePrice(s.getCode(), s.getBasePrice());
            db.getEvents().add(e);
        }
        return db;
    }
}
