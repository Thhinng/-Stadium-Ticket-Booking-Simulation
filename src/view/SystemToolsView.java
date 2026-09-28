/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

import controller.SystemController;
import java.util.Scanner;

/**
 *
 * @author MY PC
 */
public class SystemToolsView {

    private SystemController contoller;
    private Scanner scanner;

    public SystemToolsView(SystemController contoller) {
        this.contoller = contoller;
        this.scanner = new Scanner(System.in);
    }

    public SystemController getContoller() {
        return contoller;
    }

    public void setContoller(SystemController contoller) {
        this.contoller = contoller;
    }

    public Scanner getScanner() {
        return scanner;
    }

    public void setScanner(Scanner scanner) {
        this.scanner = scanner;
    }

    public void showMenu() {
        int choice;
        do {
            System.out.println("===== SYSTEM TOOLS =====");
            System.out.println("1. Reset CSV");
            System.out.println("2. Run Simulation");
            System.out.print("Enter choice: ");
            choice = scanner.nextInt();
            

            if (choice == 1) {
                resetCSV();
            } else if (choice == 2) {
                runSimulation();
            } else if (choice == 0) {
                System.out.print("Exit.");
            } else {
                System.out.print("Invalid choice!");
            }
        }
         while (choice != 0);
    }

    public void resetCSV() {
        contoller.resetCSV();
    }

    public void runSimulation() {
        contoller.runSimulation();
    }
}
