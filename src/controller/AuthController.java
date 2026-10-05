package controller;

import model.User;
import model.Fan;
import java.util.ArrayList;
import java.util.List;

public class AuthController {

    private List<User> users;
    private User currentUser;

    public AuthController() {
        users = new ArrayList<>();
        currentUser = null;
    }

    public boolean register(String userId, String fullName,
            String email, String password, String phone,
            String dateOfBirth, String identityCard,
            String address) {

        for (User user : users) {

            if (user.getEmail().equalsIgnoreCase(email)
                    || user.getUserId().equalsIgnoreCase(userId)) {

                return false;
            }
        }

        Fan fan = new Fan(
                userId,
                fullName,
                email,
                password,
                phone,
                "Fan",
                true,
                dateOfBirth,
                identityCard,
                address
        );

        users.add(fan);

        return true;
    }

    public User login(String email, String password) {

        for (User user : users) {

            if (user.login(email, password)) {

                currentUser = user;

                return user;
            }
        }

        return null;
    }

    public void logout() {

        if (currentUser != null) {

            currentUser.logout();
            currentUser = null;

        } else {

            System.out.println("No user is logged in!");
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public List<User> getUsers() {
        return users;
    }
}