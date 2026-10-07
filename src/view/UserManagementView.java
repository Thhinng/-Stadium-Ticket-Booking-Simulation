package view;

import java.util.Scanner;

public class UserManagementView {

    private Scanner sc = new Scanner(System.in);

    public int showMenu() {

        System.out.println("\n===== USER MANAGEMENT =====");
        System.out.println("1. View users");
        System.out.println("2. Lock Fan");
        System.out.println("3. Unlock Fan");
        System.out.println("4. Back");
        System.out.print("Choose: ");

        try {
            return Integer.parseInt(sc.nextLine());
        } catch (Exception e) {
            return -1;
        }
    }

    public String getUserId() {

        System.out.print("Enter Fan ID: ");

        return sc.nextLine();
    }

    public void showResult(boolean success) {

        if (success) {
            System.out.println("Operation successful!");
        } else {
            System.out.println("Fan not found!");
        }
    }
}