package model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


public class SimulationEngine {

    private final CSVRepository csvRepository;
    private SimulationResult lastResult;

    public SimulationEngine(CSVRepository csvRepository) {
        this.csvRepository = csvRepository;
    }

    public void startSimulation() {
        runSimulation("SYNCHRONIZED", 50, "MCH-01");
    }

    public SimulationResult runSimulation(String mechanism, int threadCount, String matchId) {
        System.out.println("==================================================");
        System.out.println("  BẮT ĐẦU MÔ PHỎNG ĐẶT VÉ ĐA LUỒNG (LOAD SIMULATION)");
        System.out.println("  - Cơ chế (Mechanism):       " + mechanism);
        System.out.println("  - Số lượng luồng (Threads): " + threadCount);
        System.out.println("  - Trận đấu (Match):         " + matchId);
        System.out.println("==================================================");

        List<Seat> allSeats = csvRepository.loadSeats();
        List<Fan> fans = csvRepository.loadFans();

     
        String normMatchId = matchId != null ? matchId.replaceAll("[^0-9]", "") : "";

        List<Seat> candidateSeats = new ArrayList<>();
        for (Seat s : allSeats) {
            String sMatch = s.getMatchId() != null ? s.getMatchId().replaceAll("[^0-9]", "") : "";
            if (sMatch.isEmpty() || normMatchId.isEmpty() || sMatch.equalsIgnoreCase(normMatchId) || matchId.equalsIgnoreCase(s.getMatchId())) {
                candidateSeats.add(s);
            }
        }

        if (candidateSeats.isEmpty()) {
            candidateSeats = allSeats;
        }

       
        int poolSize = Math.min(candidateSeats.size(), 25);
        List<Seat> targetSeats = new ArrayList<>();
        for (int i = 0; i < poolSize; i++) {
            Seat s = candidateSeats.get(i);
            s.setStatus("AVAILABLE");
            s.setVersion(0);
            targetSeats.add(s);
        }

        final int numSeats = targetSeats.size();
        final List<Seat> competingSeats = targetSeats;

        AtomicInteger successCounter = new AtomicInteger(0);
        AtomicInteger failCounter = new AtomicInteger(0);

        Map<String, AtomicInteger> seatBookedCount = new ConcurrentHashMap<>();
        for (Seat s : competingSeats) {
            seatBookedCount.put(s.getId(), new AtomicInteger(0));
        }

        List<Ticket> generatedTickets = Collections.synchronizedList(new ArrayList<>());
        List<Transaction> generatedTransactions = Collections.synchronizedList(new ArrayList<>());

        ExecutorService executor = Executors.newFixedThreadPool(Math.min(threadCount, 64));
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(threadCount);
        Random random = new Random();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        startGate.await();

                        int seatIndex = random.nextInt(numSeats);
                        Seat seat = competingSeats.get(seatIndex);
                        String fanId = !fans.isEmpty() ? fans.get(random.nextInt(fans.size())).getId() : ("FAN-" + threadIndex);

                        boolean booked = false;

                        if ("NO_LOCK".equalsIgnoreCase(mechanism)) {
                           
                            if ("AVAILABLE".equalsIgnoreCase(seat.getStatus())) {
                                try {
                                    Thread.sleep(random.nextInt(8) + 2);
                                } catch (InterruptedException ignored) {}

                                seat.setStatus("BOOKED");
                                booked = true;
                            }
                        } else if ("SYNCHRONIZED".equalsIgnoreCase(mechanism)) {
                         
                            synchronized (seat) {
                                if ("AVAILABLE".equalsIgnoreCase(seat.getStatus())) {
                                    seat.setStatus("BOOKED");
                                    booked = true;
                                }
                            }
                        } else if ("OPTIMISTIC".equalsIgnoreCase(mechanism)) {
                           
                            int expectedVersion = seat.getVersion();
                            String currentStatus = seat.getStatus();

                            if ("AVAILABLE".equalsIgnoreCase(currentStatus)) {
                                try {
                                    Thread.sleep(random.nextInt(4) + 1);
                                } catch (InterruptedException ignored) {}

                                synchronized (seat) {
                                    if (seat.getVersion() == expectedVersion && "AVAILABLE".equalsIgnoreCase(seat.getStatus())) {
                                        seat.setVersion(expectedVersion + 1);
                                        seat.setStatus("BOOKED");
                                        booked = true;
                                    }
                                }
                            }
                        }

                        if (booked) {
                            successCounter.incrementAndGet();
                            seatBookedCount.get(seat.getId()).incrementAndGet();

                            String ticketId = "TCK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                            Ticket ticket = new Ticket(ticketId, fanId, matchId, seat.getId(), "ISSUED", 200.0);
                            generatedTickets.add(ticket);

                            String transId = "TX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                            String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                            Transaction tx = new Transaction(transId, fanId, matchId, "BOOKING", 200.0, time);
                            generatedTransactions.add(tx);
                        } else {
                            failCounter.incrementAndGet();
                        }
                    } catch (Exception e) {
                        failCounter.incrementAndGet();
                    } finally {
                        endGate.countDown();
                    }
                }
            });
        }

        startGate.countDown();

        try {
            endGate.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        executor.shutdown();

        long endTime = System.currentTimeMillis();
        long duration = Math.max(endTime - startTime, 1);

        int doubleBookingSeats = 0;
        for (Map.Entry<String, AtomicInteger> entry : seatBookedCount.entrySet()) {
            if (entry.getValue().get() > 1) {
                doubleBookingSeats += (entry.getValue().get() - 1);
            }
        }

        double tps = (threadCount * 1000.0) / duration;
        String nowStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String resultId = "SIM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        String summary = String.format("Mechanism: %s, Success: %d/%d, DoubleBookings: %d, TPS: %.1f",
                mechanism, successCounter.get(), threadCount, doubleBookingSeats, tps);

        SimulationResult result = new SimulationResult(
                resultId,
                mechanism,
                nowStr,
                threadCount,
                successCounter.get(),
                failCounter.get(),
                doubleBookingSeats,
                duration,
                tps,
                summary
        );

        this.lastResult = result;

      
        csvRepository.appendSimulationResult(result);
        for (Ticket t : generatedTickets) {
            csvRepository.appendTicket(t);
        }
        for (Transaction tx : generatedTransactions) {
            csvRepository.appendTransaction(tx);
        }

        System.out.println(result.toString());
        return result;
    }

    public SimulationResult getLastResult() {
        return lastResult;
    }
}
