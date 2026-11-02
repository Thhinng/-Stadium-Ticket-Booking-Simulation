package controller;

import java.util.List;
import java.util.Map;
import model.CSVRepository;
import model.Fan;
import model.Match;
import model.Seat;
import model.SimulationEngine;
import model.SimulationResult;
import model.Stadium;

public class SystemController {

    private final CSVRepository csvRepository;
    private final SimulationEngine simulationEngine;

    public SystemController() {
        this.csvRepository = new CSVRepository();
        this.simulationEngine = new SimulationEngine(csvRepository);
    }

    public SystemController(CSVRepository csvRepository, SimulationEngine simulationEngine) {
        this.csvRepository = csvRepository != null ? csvRepository : new CSVRepository();
        this.simulationEngine = simulationEngine != null ? simulationEngine : new SimulationEngine(this.csvRepository);
    }

    public CSVRepository getCsvRepository() {
        return csvRepository;
    }

    public SimulationEngine getSimulationEngine() {
        return simulationEngine;
    }

 
    public void resetCSV() {
        csvRepository.resetAllData();
    }

    public void runSimulation() {
        simulationEngine.startSimulation();
    }

  
    public SimulationResult runSimulation(String mechanism, int threadCount, String matchId) {
        return simulationEngine.runSimulation(mechanism, threadCount, matchId);
    }

    public SimulationResult getLastSimulationResult() {
        return simulationEngine.getLastResult();
    }

    public List<Fan> getAllFans() {
        return csvRepository.loadFans();
    }

    public List<Stadium> getAllStadiums() {
        return csvRepository.loadStadiums();
    }

    public List<Match> getAllMatches() {
        return csvRepository.loadMatches();
    }

    public List<Seat> getAllSeats() {
        return csvRepository.loadSeats();
    }

    
    public Map<String, String> checkDataConnections() {
        return csvRepository.checkDataConnections();
    }

    public String getDataDirectory() {
        return csvRepository.getConnector().getDataDirectory();
    }

    public void setDataDirectory(String dir) {
        csvRepository.getConnector().setDataDirectory(dir);
    }

   
    public String getSystemDataSummary() {
        List<Fan> fans = csvRepository.loadFans();
        List<Stadium> stadiums = csvRepository.loadStadiums();
        List<Match> matches = csvRepository.loadMatches();
        List<Seat> seats = csvRepository.loadSeats();

        long availableSeats = 0;
        for (Seat s : seats) {
            if ("AVAILABLE".equalsIgnoreCase(s.getStatus())) {
                availableSeats++;
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("================ BÁO CÁO DỮ LIỆU HIỆN CÓ ================\n");
        sb.append(String.format("  - Thư mục dữ liệu liên kết: [%s]\n", getDataDirectory()));
        sb.append(String.format("  - Tổng số Fans:            %,d\n", fans.size()));
        sb.append(String.format("  - Tổng số Sân vận động:     %,d\n", stadiums.size()));
        sb.append(String.format("  - Tổng số Trận đấu:        %,d\n", matches.size()));
        sb.append(String.format("  - Tổng số Ghế:             %,d (Còn trống: %,d, Đã đặt: %,d)\n",
                seats.size(), availableSeats, (seats.size() - availableSeats)));
        sb.append("=========================================================");
        return sb.toString();
    }
}
