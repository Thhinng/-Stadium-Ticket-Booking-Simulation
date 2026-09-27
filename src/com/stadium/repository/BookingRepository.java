package com.stadium.repository;

import com.stadium.model.*;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-Safe Data Access Repository for Bookings, Tickets, and Stadium Seats.
 * Handles concurrency, locking, anti-double booking, and CSV persistence.
 * Member 3: Lê Bách - Booking & Payment Module
 */
public class BookingRepository {
    private final Map<String, Booking> bookingMap = new ConcurrentHashMap<>();
    private final Map<String, Ticket> ticketMap = new ConcurrentHashMap<>();
    private final Map<String, Seat> seatMap = new ConcurrentHashMap<>();
    private final Map<String, Match> matchMap = new ConcurrentHashMap<>();
    
    // Per-seat locks to ensure atomic double-booking protection
    private final Map<String, ReentrantLock> seatLocks = new ConcurrentHashMap<>();
    private final ReentrantLock globalLock = new ReentrantLock();

    private final String dataDir = "data";
    private final String bookingsCsvPath = "data/bookings.csv";
    private final String ticketsCsvPath = "data/tickets.csv";

    public BookingRepository() {
        initSampleData();
        ensureDataDirExists();
    }

    private void ensureDataDirExists() {
        try {
            Files.createDirectories(Paths.get(dataDir));
        } catch (IOException e) {
            System.err.println("Không thể tạo thư mục data: " + e.getMessage());
        }
    }

    /**
     * Initializes sample stadium matches and seat inventory for demo testing.
     */
    public void initSampleData() {
        Match match1 = new Match("M001", "Việt Nam vs Thái Lan (Chung Kết AFF Cup)", "Sân vận động Quốc gia Mỹ Đình", LocalDateTime.now().plusDays(5));
        Match match2 = new Match("M002", "Hà Nội FC vs Hải Phòng FC (V-League)", "Sân vận động Hàng Đẫy", LocalDateTime.now().plusDays(2));
        matchMap.put(match1.getMatchId(), match1);
        matchMap.put(match2.getMatchId(), match2);

        // Generate seats for Match 1
        String[] zones = {"SVIP", "VIP", "Standard"};
        double[] prices = {1500000.0, 800000.0, 400000.0};
        char[] rows = {'A', 'B', 'C'};

        for (int z = 0; z < zones.length; z++) {
            for (char r : rows) {
                for (int num = 1; num <= 5; num++) {
                    String seatId = String.format("%s-%s-%s%d", match1.getMatchId(), zones[z], r, num);
                    Seat seat = new Seat(seatId, match1.getMatchId(), zones[z], String.valueOf(r), num, prices[z]);
                    seatMap.put(seatId, seat);
                    seatLocks.put(seatId, new ReentrantLock());
                }
            }
        }
    }

    public Match getMatch(String matchId) {
        return matchMap.get(matchId);
    }

    public List<Match> getAllMatches() {
        return new ArrayList<>(matchMap.values());
    }

    public Seat getSeat(String seatId) {
        return seatMap.get(seatId);
    }

    public List<Seat> getSeatsForMatch(String matchId) {
        List<Seat> list = new ArrayList<>();
        for (Seat s : seatMap.values()) {
            if (s.getMatchId().equalsIgnoreCase(matchId)) {
                list.add(s);
            }
        }
        return list;
    }

    /**
     * Acquires locks on requested seats atomically to prevent double booking.
     * Returns true if all requested seats are AVAILABLE and successfully locked.
     */
    public boolean lockSeatsForBooking(List<String> seatIds, String userId) {
        // Sort seatIds to avoid deadlocks across multiple threads locking in different order
        List<String> sortedSeatIds = new ArrayList<>(seatIds);
        Collections.sort(sortedSeatIds);

        List<ReentrantLock> acquiredLocks = new ArrayList<>();
        try {
            for (String seatId : sortedSeatIds) {
                ReentrantLock lock = seatLocks.computeIfAbsent(seatId, k -> new ReentrantLock());
                lock.lock();
                acquiredLocks.add(lock);

                Seat seat = seatMap.get(seatId);
                if (seat == null || seat.getStatus() != TicketStatus.AVAILABLE) {
                    // One of the seats is not available, rollback lock acquisition
                    return false;
                }
            }

            // All seats are available! Change status to HOLD
            for (String seatId : sortedSeatIds) {
                Seat seat = seatMap.get(seatId);
                seat.setStatus(TicketStatus.HOLD);
                seat.setLockedByUserId(userId);
            }
            return true;
        } finally {
            for (ReentrantLock lock : acquiredLocks) {
                lock.unlock();
            }
        }
    }

    public void releaseSeats(List<String> seatIds) {
        for (String seatId : seatIds) {
            Seat seat = seatMap.get(seatId);
            if (seat != null) {
                seat.setStatus(TicketStatus.AVAILABLE);
                seat.setLockedByUserId(null);
            }
        }
    }

    public void saveBooking(Booking booking) {
        bookingMap.put(booking.getBookingId(), booking);
        for (Ticket t : booking.getTickets()) {
            ticketMap.put(t.getTicketId(), t);
        }
        saveToCsv();
    }

    public Booking getBooking(String bookingId) {
        return bookingMap.get(bookingId);
    }

    public Ticket getTicket(String ticketId) {
        return ticketMap.get(ticketId);
    }

    public List<Booking> getAllBookings() {
        return new ArrayList<>(bookingMap.values());
    }

    public List<Booking> getBookingsByUser(String userId) {
        List<Booking> userBookings = new ArrayList<>();
        for (Booking b : bookingMap.values()) {
            if (b.getUserId().equalsIgnoreCase(userId)) {
                userBookings.add(b);
            }
        }
        userBookings.sort(Comparator.comparing(Booking::getBookingTime).reversed());
        return userBookings;
    }

    public List<Ticket> getTicketsByUser(String userId) {
        List<Ticket> userTickets = new ArrayList<>();
        for (Ticket t : ticketMap.values()) {
            if (userId.equalsIgnoreCase(t.getUserId())) {
                userTickets.add(t);
            }
        }
        return userTickets;
    }

    /**
     * Persists current bookings and tickets to CSV files.
     */
    public synchronized void saveToCsv() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(bookingsCsvPath))) {
            writer.println("bookingId,userId,matchId,matchName,totalAmount,bookingTime,status,paymentMethod,transactionId");
            for (Booking b : bookingMap.values()) {
                writer.printf("%s,%s,%s,\"%s\",%.2f,%s,%s,%s,%s%n",
                        b.getBookingId(),
                        b.getUserId(),
                        b.getMatchId(),
                        b.getMatchName().replace("\"", "'"),
                        b.getTotalAmount(),
                        b.getFormattedBookingTime(),
                        b.getStatus().name(),
                        b.getPaymentMethod() != null ? b.getPaymentMethod() : "",
                        b.getTransactionId() != null ? b.getTransactionId() : "");
            }
        } catch (IOException e) {
            System.err.println("Lỗi lưu CSV đơn đặt vé: " + e.getMessage());
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(ticketsCsvPath))) {
            writer.println("ticketId,bookingId,userId,matchId,seatId,seatName,zone,price,status");
            for (Ticket t : ticketMap.values()) {
                writer.printf("%s,%s,%s,%s,%s,\"%s\",%s,%.2f,%s%n",
                        t.getTicketId(),
                        t.getBookingId(),
                        t.getUserId(),
                        t.getMatchId(),
                        t.getSeatId(),
                        t.getSeatName(),
                        t.getZone(),
                        t.getPrice(),
                        t.getStatus().name());
            }
        } catch (IOException e) {
            System.err.println("Lỗi lưu CSV vé: " + e.getMessage());
        }
    }
}
