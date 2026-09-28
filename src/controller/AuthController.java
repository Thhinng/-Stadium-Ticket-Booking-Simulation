
package controller;
   import model.User;
   import model.Fan;
   import java.util.ArrayList;
   import java.util.List;

public class AuthController {
    private List<User> users;
    public AuthController() {
        users = new ArrayList<>();
    }
     public boolean register(String userId, String fullName, String email, String password, String phone, String dateOfBirth, String identityCard, String address) {
         for (User user : users) {
             if(user.getEmail().equals(email)){
                 return false;
             }
         }
         Fan fan = new Fan (userId, fullName, email, password, phone, "Fan", true, dateOfBirth, identityCard, address);
         users.add(fan);
         return true;
     }
     public User login(String email, String password) {
         for (User user : users) {
         if (user.login(email, password)) {
             return user;
         }
     }
         return null;
     }
     public List<User> getUsers() {
         return users;
     }
    
}
