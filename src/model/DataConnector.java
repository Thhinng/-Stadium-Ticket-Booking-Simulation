package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;


public class DataConnector {

    private static final String CONFIG_FILE = "config/data-config.properties";

    private String dataDirectory = "data";
    private final Properties properties = new Properties();

    private static DataConnector instance;

    public DataConnector() {
        loadConfiguration();
    }

    public static synchronized DataConnector getInstance() {
        if (instance == null) {
            instance = new DataConnector();
        }
        return instance;
    }

   
    public void loadConfiguration() {
        File configFile = new File(CONFIG_FILE);
        if (configFile.exists()) {
            try (InputStream is = new FileInputStream(configFile)) {
                properties.load(is);
                String dir = properties.getProperty("data.directory");
                if (dir != null && !dir.trim().isEmpty()) {
                    this.dataDirectory = dir.trim();
                }
            } catch (IOException e) {
                System.err.println("Không thể đọc file cấu hình liên kết dữ liệu: " + e.getMessage());
            }
        }
        ensureDirectoryExists(dataDirectory);
    }

    public void ensureDirectoryExists(String dirPath) {
        try {
            Path p = Paths.get(dirPath);
            if (!Files.exists(p)) {
                Files.createDirectories(p);
            }
        } catch (IOException e) {
            System.err.println("Lỗi tạo thư mục dữ liệu: " + e.getMessage());
        }
    }

    public String getDataDirectory() {
        return dataDirectory;
    }

  
    public void setDataDirectory(String dataDirectory) {
        this.dataDirectory = dataDirectory;
        ensureDirectoryExists(dataDirectory);
    }

  
    public Path resolvePath(String fileName) {
        return Paths.get(dataDirectory, fileName);
    }

    public BufferedReader getReader(String fileName) throws IOException {
        Path path = resolvePath(fileName);
        return Files.newBufferedReader(path, StandardCharsets.UTF_8);
    }

    public BufferedWriter getWriter(String fileName, boolean append) throws IOException {
        Path path = resolvePath(fileName);
        if (append) {
            return Files.newBufferedWriter(path, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } else {
            return Files.newBufferedWriter(path, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
    }

    public boolean exists(String fileName) {
        return Files.exists(resolvePath(fileName));
    }

    public Map<String, String> testAllConnections() {
        Map<String, String> results = new LinkedHashMap<>();
        String[] files = {
            "fans.csv",
            "matches.csv",
            "stadiums.csv",
            "sections.csv",
            "seats.csv",
            "tickets.csv",
            "transactions.csv",
            "simulation_results.csv"
        };

        for (String file : files) {
            Path p = resolvePath(file);
            if (!Files.exists(p)) {
              
                if ("fans.csv".equals(file) && Files.exists(resolvePath("fan.csv"))) {
                    p = resolvePath("fan.csv");
                    file = "fan.csv";
                }
            }

            if (Files.exists(p)) {
                try {
                    long size = Files.size(p);
                    long lines = Files.lines(p, StandardCharsets.UTF_8).count();
                    results.put(file, String.format("CONNECTED [Đã kết nối - %,d bytes, %,d dòng]", size, lines));
                } catch (IOException e) {
                    results.put(file, "ERROR [Có file nhưng không đọc được: " + e.getMessage() + "]");
                }
            } else {
                results.put(file, "DISCONNECTED [Chưa tìm thấy file tại " + p.toAbsolutePath() + "]");
            }
        }
        return results;
    }
}
