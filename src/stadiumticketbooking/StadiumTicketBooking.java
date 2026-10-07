package stadiumticketbooking;

import java.util.Scanner;

import controller.AuthController;
import controller.UserAdminController;

import model.User;

import view.LoginView;
import view.RegisterView;
import view.UserManagementView;

public class StadiumTicketBooking {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        AuthController authController = new AuthController();

        RegisterView registerView = new RegisterView();
        LoginView loginView = new LoginView();
        UserManagementView userView = new UserManagementView();

        while (true) {

            System.out.println("\n==============================");
            System.out.println("   STADIUM TICKET BOOKING");
            System.out.println("==============================");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. User Management");
            System.out.println("4. Logout");
            System.out.println("5. Exit");
            System.out.print("Choose: ");

            int choice;

            try {
                choice = Integer.parseInt(sc.nextLine());
            } catch (Exception e) {
                System.out.println("Invalid choice!");
                continue;
            }

            switch (choice) {

                // ================= REGISTER =================
                case 1:

                    String id = registerView.getId();
                    String fullName = registerView.getFullName();
                    String email = registerView.getEmail();
                    String password = registerView.getPassword();
                    String phone = registerView.getPhone();
                    String dateOfBirth = registerView.getDateOfBirth();
                    String identityCard = registerView.getIdentityCard();
                    String address = registerView.getAddress();

                    boolean registered = authController.register(
                            id,
                            fullName,
                            email,
                            password,
                            phone,
                            dateOfBirth,
                            identityCard,
                            address
                    );

                    registerView.showResult(registered);

                    break;

                // ================= LOGIN =================
                case 2:

                    String loginEmail = loginView.getEmail();
                    String loginPassword = loginView.getPassword();

                    User user = authController.login(
                            loginEmail,
                            loginPassword
                    );

                    loginView.showResult(user);

                    break;

                // ================= USER MANAGEMENT =================
                case 3:

                    UserAdminController userAdminController
                            = new UserAdminController(
                                    authController.getUsers()
                            );

                    while (true) {

                        int managementChoice = userView.showMenu();

                        switch (managementChoice) {

                            case 1:

                                userAdminController.displayUsers();

                                break;

                            case 2:

                                String lockId = userView.getUserId();

                                boolean locked
                                        = userAdminController.lockFan(lockId);

                                userView.showResult(locked);

                                break;

                            case 3:

                                String unlockId = userView.getUserId();

                                boolean unlocked
                                        = userAdminController.unlockFan(unlockId);

                                userView.showResult(unlocked);

                                break;

                            case 4:

                                break;

                            default:

                                System.out.println("Invalid choice!");
                                continue;
                        }

                        if (managementChoice == 4) {
                            break;
                        }
                    }

                    break;

                // ================= LOGOUT =================
                case 4:

                    authController.logout();

                    break;

                // ================= EXIT =================
                case 5:

                    System.out.println("Thank you for using the system!");

                    sc.close();

                    return;

                default:

                    System.out.println("Invalid choice!");
            }
        }
    }
}