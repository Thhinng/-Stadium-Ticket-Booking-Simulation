package view;

import java.util.Scanner;

public class RegisterView {

    private Scanner sc = new Scanner(System.in);

    public String getId() {
        System.out.print("Enter ID: ");
        return sc.nextLine();
    }

    public String getFullName() {
        System.out.print("Enter Full Name: ");
        return sc.nextLine();
    }

    public String getEmail() {
        System.out.print("Enter Email: ");
        return sc.nextLine();
    }

    public String getPassword() {
        System.out.print("Enter Password: ");
        return sc.nextLine();
    }

    public String getPhone() {
        System.out.print("Enter Phone: ");
        return sc.nextLine();
    }

    public String getDateOfBirth() {
        System.out.print("Enter Date of Birth: ");
        return sc.nextLine();
    }

    public String getIdentityCard() {
        System.out.print("Enter Identity Card: ");
        return sc.nextLine();
    }

    public String getAddress() {
        System.out.print("Enter Address: ");
        return sc.nextLine();
    }

    public void showResult(boolean success) {

        if (success) {
            System.out.println("Register successfully!");
        } else {
            System.out.println("ID or Email already exists!");
        }
    }
}