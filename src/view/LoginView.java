package view;

import java.util.Scanner;
import model.User;

public class LoginView {

    private Scanner sc = new Scanner(System.in);

    public String getEmail() {
        System.out.print("Enter Email: ");
        return sc.nextLine();
    }

    public String getPassword() {
        System.out.print("Enter Password: ");
        return sc.nextLine();
    }

    public void showResult(User user) {

        if (user == null) {

            System.out.println("Login failed or account is locked!");

        } else {

            System.out.println("Login successfully!");
            System.out.println("Welcome, " + user.getFullName());
            System.out.println("Role: " + user.getRole());
        }
    }
}