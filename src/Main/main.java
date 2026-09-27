/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Main;

import controller.SystemController;
import view.SystemToolsView;

/**
 *
 * @author MY PC
 */
public class main {
    public static void main(String[] args) {
        SystemController controller  = new SystemController();
        SystemToolsView view = new SystemToolsView(controller);
        view.showMenu();
        
    }
}
