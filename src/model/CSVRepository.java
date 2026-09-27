/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author MY PC
 */
public class CSVRepository {

    private String fileName;

    public CSVRepository(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    

    public List<String> loadData() {
        System.out.println("Loading data from " + fileName);
        return new ArrayList<>();
    }

    public void saveData(List<String> data) {
        System.out.println("Saving data to " + fileName);
    }

    public void resetCSV() {
        System.out.println("CSV has been reset.");
    }

}
