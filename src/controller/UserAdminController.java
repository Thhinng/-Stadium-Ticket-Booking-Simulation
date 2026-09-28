
package controller;
import model.User;
import model.Admin;
import java.util.List;
public class UserAdminController {
    private List<User> users;
    public UserAdminController  (List<User> users) {
        this.users = users;
}
    public boolean lockUser(String userId) {
        for (User user : users){
            if(user.getUserId().equals(userId)) {
                user.setActive(false);
                return true;
            }
        }
        return false;
    }
    public boolean unlockUser(String userId) {
        for (User user : users) {
            if (user.getUserId().equals(userId)){
                user.setActive(true);
                        return true;
            }
        }
        return false;
    }
    public void showUsers() {
        for (User user : users) {
            System.out.println(
                      "ID: " + user.getUserId()
                      + "| Name: " + user.getFullName()
                      + "| Email: " + user.getEmail()
                      + "| Role: " + user.getRole()
                      + "| Active: " + user.isActive());
        }
    }
}
