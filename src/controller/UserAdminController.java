package controller;

import model.User;
import java.util.List;

public class UserAdminController {

    private List<User> users;

    public UserAdminController(List<User> users) {
        this.users = users;
    }

    public void displayUsers() {

        if (users.isEmpty()) {
            System.out.println("No users found!");
            return;
        }

        System.out.println("\n===== USER LIST =====");

        for (User user : users) {

            System.out.println(
                    "ID: " + user.getUserId()
                    + " | Name: " + user.getFullName()
                    + " | Email: " + user.getEmail()
                    + " | Role: " + user.getRole()
                    + " | Status: "
                    + (user.isActive() ? "Active" : "Locked")
            );
        }
    }

    public boolean lockFan(String userId) {

        for (User user : users) {

            if (user.getUserId().equalsIgnoreCase(userId)
                    && user.getRole().equalsIgnoreCase("Fan")) {

                user.setActive(false);

                return true;
            }
        }

        return false;
    }

    public boolean unlockFan(String userId) {

        for (User user : users) {

            if (user.getUserId().equalsIgnoreCase(userId)
                    && user.getRole().equalsIgnoreCase("Fan")) {

                user.setActive(true);

                return true;
            }
        }

        return false;
    }
}