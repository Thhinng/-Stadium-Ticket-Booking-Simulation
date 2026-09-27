/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import javax.xml.transform.Source;

/**
 *
 * @author MY PC
 */
public class SimulationEngine {
    private int concurrentUsers;
    private String targetMatchId;

    public SimulationEngine(int concurrentUsers, String targetMatchId) {
        this.concurrentUsers = concurrentUsers;
        this.targetMatchId = targetMatchId;
    }

    public int getConcurrentUsers() {
        return concurrentUsers;
    }

    public void setConcurrentUsers(int concurrentUsers) {
        this.concurrentUsers = concurrentUsers;
    }

    public String getTargetMatchId() {
        return targetMatchId;
    }

    public void setTargetMatchId(String targetMatchId) {
        this.targetMatchId = targetMatchId;
    }
    
    
    public void startSimulation(){
        System.out.println("Starting simulation...");
        for (int i = 1; i <= concurrentUsers; i++){
            System.out.println("User " + i + "is booking ticket for " + targetMatchId);
        }
        System.out.println("Simulation completed.");
    }
}
