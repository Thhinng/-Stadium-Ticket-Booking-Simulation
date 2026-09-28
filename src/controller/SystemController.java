/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import model.CSVRepository;
import model.SimulationEngine;

/**
 *
 * @author MY PC
 */
public class SystemController {
   private CSVRepository csvRepository;
   private SimulationEngine simulationEngine;

    public SystemController() {
        csvRepository = new CSVRepository("data.csv");
        simulationEngine = new SimulationEngine(5, "MATCH001");
    }
    
    public void resetCSV(){
        csvRepository.resetCSV();
    }
    
    public void runSimulation(){
        simulationEngine.startSimulation();
    }
   
}
