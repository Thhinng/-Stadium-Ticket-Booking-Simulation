package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * CSVRepository - DAO tổng chịu trách nhiệm đọc/ghi tất cả file CSV trong hệ thống.
 * Sử dụng DataConnector để quản lý kết nối và liên kết trực tiếp với các file dữ liệu bên ngoài.
 */
public class CSVRepository {

    private final DataConnector connector;

    public static final String FANS_FILE = "fans.csv";
    public static final String FAN_FILE = "fan.csv";
    public static final String STADIUM_FILE = "stadiums.csv";
    public static final String MATCH_FILE = "matches.csv";
    public static final String SECTION_FILE = "sections.csv";
    public static final String SEAT_FILE = "seats.csv";
    public static final String TICKET_FILE = "tickets.csv";
    public static final String TRANSACTION_FILE = "transactions.csv";
    public static final String SIMULATION_FILE = "simulation_results.csv";

    public CSVRepository() {
        this.connector = DataConnector.getInstance();
    }

    public CSVRepository(DataConnector connector) {
        this.connector = connector != null ? connector : DataConnector.getInstance();
    }

    public DataConnector getConnector() {
        return connector;
    }

    /**
     * Kiểm tra trạng thái liên kết với các file dữ liệu bên ngoài.
     */
    public Map<String, String> checkDataConnections() {
        return connector.testAllConnections();
    }

    // ==========================================
    // 1. FAN / USER CSV
    // ==========================================
    public List<Fan> loadFans() {
        List<Fan> fans = new ArrayList<>();
        String targetFile = connector.exists(FANS_FILE) ? FANS_FILE : FAN_FILE;
        if (!connector.exists(targetFile)) {
            return fans;
        }

        try (BufferedReader br = connector.getReader(targetFile)) {
            String headerLine = br.readLine();
            if (headerLine == null) return fans;

            boolean isExternalFormat = headerLine.toLowerCase().contains("username") || headerLine.toLowerCase().contains("balance");

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(",", -1);
                if (isExternalFormat && parts.length >= 6) {
                    // fanId,username,password,fullName,email,phone,balance
                    String id = parts[0].trim();
                    String pwd = parts[2].trim();
                    String fullName = parts[3].trim();
                    String email = parts[4].trim();
                    String phone = parts[5].trim();
                    fans.add(new Fan(id, fullName, email, pwd, phone, "FAN", "ACTIVE", "", "", ""));
                } else if (parts.length >= 6) {
                    String id = parts[0].trim();
                    String fullName = parts[1].trim();
                    String email = parts[2].trim();
                    String pwd = parts[3].trim();
                    String phone = parts[4].trim();
                    if (parts.length >= 10) {
                        String role = parts[5].trim();
                        String status = parts[6].trim();
                        String dob = parts[7].trim();
                        String idCard = parts[8].trim();
                        String address = parts[9].trim();
                        fans.add(new Fan(id, fullName, email, pwd, phone, role, status, dob, idCard, address));
                    } else {
                        String status = parts[5].trim();
                        fans.add(new Fan(id, fullName, email, pwd, phone, status));
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi đọc file fans.csv: " + e.getMessage());
        }
        return fans;
    }

    public void saveFans(List<Fan> fans) {
        String targetFile = connector.exists(FANS_FILE) ? FANS_FILE : FAN_FILE;
        try (BufferedWriter bw = connector.getWriter(targetFile, false)) {
            bw.write("fanId,username,password,fullName,email,phone,balance\n");
            for (Fan fan : fans) {
                bw.write(String.format("%s,%s,%s,%s,%s,%s,%.2f\n",
                        fan.getId(),
                        fan.getEmail().split("@")[0],
                        fan.getPassword(),
                        fan.getFullName(),
                        fan.getEmail(),
                        fan.getPhone(),
                        5000.0));
            }
        } catch (IOException e) {
            System.err.println("Lỗi ghi file fans.csv: " + e.getMessage());
        }
    }

    // ==========================================
    // 2. STADIUM CSV
    // ==========================================
    public List<Stadium> loadStadiums() {
        List<Stadium> stadiums = new ArrayList<>();
        if (!connector.exists(STADIUM_FILE)) return stadiums;

        try (BufferedReader br = connector.getReader(STADIUM_FILE)) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || isHeader) {
                    isHeader = false;
                    continue;
                }
                List<String> parts = parseCsvLine(line);
                if (parts.size() >= 4) {
                    String id = parts.get(0).trim();
                    String name = parts.get(1).trim();
                    String address = parts.get(2).trim();
                    int capacity = Integer.parseInt(parts.get(3).trim());
                    stadiums.add(new Stadium(id, name, address, capacity));
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi đọc file stadiums.csv: " + e.getMessage());
        }
        return stadiums;
    }

    // ==========================================
    // 3. MATCH CSV
    // ==========================================
    public List<Match> loadMatches() {
        List<Match> matches = new ArrayList<>();
        if (!connector.exists(MATCH_FILE)) return matches;

        try (BufferedReader br = connector.getReader(MATCH_FILE)) {
            String headerLine = br.readLine();
            if (headerLine == null) return matches;

            boolean isExternalFormat = headerLine.toLowerCase().contains("matchname");

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                List<String> parts = parseCsvLine(line);
                if (isExternalFormat && parts.size() >= 7) {
                    // matchId,stadiumId,matchName,matchDate,homeTeam,awayTeam,status
                    String id = parts.get(0).trim();
                    String stadiumId = parts.get(1).trim();
                    String matchDate = parts.get(3).trim();
                    String homeTeam = parts.get(4).trim();
                    String awayTeam = parts.get(5).trim();
                    String status = parts.get(6).trim();
                    matches.add(new Match(id, homeTeam, awayTeam, stadiumId, matchDate, status));
                } else if (parts.size() >= 6) {
                    matches.add(new Match(parts.get(0).trim(), parts.get(1).trim(), parts.get(2).trim(),
                            parts.get(3).trim(), parts.get(4).trim(), parts.get(5).trim()));
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi đọc file matches.csv: " + e.getMessage());
        }
        return matches;
    }

    // ==========================================
    // 4. SECTION CSV
    // ==========================================
    public List<Section> loadSections() {
        List<Section> sections = new ArrayList<>();
        if (!connector.exists(SECTION_FILE)) return sections;

        try (BufferedReader br = connector.getReader(SECTION_FILE)) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                List<String> parts = parseCsvLine(line);
                if (parts.size() >= 4) {
                    sections.add(new Section(parts.get(0).trim(), parts.get(1).trim(), parts.get(2).trim(), parts.get(3).trim()));
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi đọc file sections.csv: " + e.getMessage());
        }
        return sections;
    }

    // ==========================================
    // 5. SEAT CSV
    // ==========================================
    public List<Seat> loadSeats() {
        List<Seat> seats = new ArrayList<>();
        if (!connector.exists(SEAT_FILE)) return seats;

        try (BufferedReader br = connector.getReader(SEAT_FILE)) {
            String header = br.readLine();
            boolean isExternalFormat = header != null && header.toLowerCase().contains("rownumber");

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(",", -1);
                if (isExternalFormat && parts.length >= 6) {
                    String id = parts[0].trim();
                    String sectionId = parts[1].trim();
                    String rowNum = parts[2].trim();
                    String seatNum = parts[3].trim();
                    String status = parts[4].trim();
                    int version = Integer.parseInt(parts[5].trim());
                    seats.add(new Seat(id, sectionId, "MCH-01", "R" + rowNum + "-S" + seatNum, status, version));
                } else if (parts.length >= 6) {
                    String id = parts[0].trim();
                    String sectionId = parts[1].trim();
                    String matchId = parts[2].trim();
                    String seatNumber = parts[3].trim();
                    String status = parts[4].trim();
                    int version = Integer.parseInt(parts[5].trim());
                    seats.add(new Seat(id, sectionId, matchId, seatNumber, status, version));
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi đọc file seats.csv: " + e.getMessage());
        }
        return seats;
    }

    public void saveSeats(List<Seat> seats) {
        try (BufferedWriter bw = connector.getWriter(SEAT_FILE, false)) {
            bw.write("seatId,sectionId,rowNumber,seatNumber,status,version\n");
            for (Seat s : seats) {
                bw.write(String.format("%s,%s,0,%s,%s,%d\n",
                        s.getId(), s.getSectionId(),
                        s.getSeatNumber(), s.getStatus(), s.getVersion()));
            }
        } catch (IOException e) {
            System.err.println("Lỗi ghi file seats.csv: " + e.getMessage());
        }
    }

    // ==========================================
    // 6. TICKETS & TRANSACTIONS
    // ==========================================
    public synchronized void appendTicket(Ticket t) {
        boolean fileExists = connector.exists(TICKET_FILE);
        try (BufferedWriter bw = connector.getWriter(TICKET_FILE, true)) {
            if (!fileExists) {
                bw.write("ticketId,matchId,seatId,fanId,price,bookingTime,status\n");
            }
            bw.write(String.format("%s,%s,%s,%s,%.2f,%s,%s\n",
                    t.getId(), t.getMatchId(), t.getSeatId(),
                    t.getFanId(), t.getPrice(), "2026-11-01 18:00:00", t.getStatus()));
        } catch (IOException e) {
            System.err.println("Lỗi append ticket: " + e.getMessage());
        }
    }

    public synchronized void appendTransaction(Transaction tx) {
        boolean fileExists = connector.exists(TRANSACTION_FILE);
        try (BufferedWriter bw = connector.getWriter(TRANSACTION_FILE, true)) {
            if (!fileExists) {
                bw.write("transactionId,matchId,fanId,seatId,status,mechanism,timestamp,latencyMs\n");
            }
            bw.write(String.format("%s,%s,%s,SEAT,SUCCESS,%s,%s,10\n",
                    tx.getId(), tx.getMatchId(), tx.getFanId(),
                    tx.getType(), tx.getTimestamp()));
        } catch (IOException e) {
            System.err.println("Lỗi append transaction: " + e.getMessage());
        }
    }

    public synchronized void appendSimulationResult(SimulationResult res) {
        boolean fileExists = connector.exists(SIMULATION_FILE);
        try (BufferedWriter bw = connector.getWriter(SIMULATION_FILE, true)) {
            if (!fileExists) {
                bw.write("id,mechanism,executedAt,totalRequests,successCount,failedCount,doubleBookingCount,durationMs,tps,summary\n");
            }
            bw.write(String.format("%s,%s,%s,%d,%d,%d,%d,%d,%.2f,\"%s\"\n",
                    res.getId(),
                    res.getMechanism(),
                    res.getExecutedAt(),
                    res.getTotalRequests(),
                    res.getSuccessCount(),
                    res.getFailedCount(),
                    res.getDoubleBookingCount(),
                    res.getDurationMs(),
                    res.getTps(),
                    res.getResult().replace("\"", "'")));
        } catch (IOException e) {
            System.err.println("Lỗi append simulation result: " + e.getMessage());
        }
    }

    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(cur.toString());
                cur = new StringBuilder();
            } else {
                cur.append(c);
            }
        }
        result.add(cur.toString());
        return result;
    }

    public void resetCSV() {
        resetAllData();
    }

    public void resetAllData() {
        List<Seat> seats = loadSeats();
        for (Seat s : seats) {
            s.setStatus("AVAILABLE");
            s.setVersion(0);
        }
        saveSeats(seats);
        System.out.println(">> Đã reset toàn bộ dữ liệu ghế về trạng thái AVAILABLE và version = 0 thành công!");
    }

    public void readFans() {
        List<Fan> fans = loadFans();
        System.out.println("Total fans: " + fans.size());
    }
}
