package service;

import model.*;
import util.PasswordUtil;
import java.util.*;

public class AuthService {
    private final Store store;
    public AuthService(Store store) { this.store = store; }

    public User register(String username, String name, String password) {
        username = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
        name = name == null ? "" : name.trim();
        validateUsername(username);
        if (name.length() < 2 || name.length() > 80) throw new IllegalArgumentException("Họ tên phải dài từ 2 đến 80 ký tự.");
        validatePassword(password);
        synchronized (this.store) {
            for (User u : this.store.getDatabase().getUsers())
                if (u.getUsername().equalsIgnoreCase(username)) throw new IllegalArgumentException("Tên đăng nhập đã tồn tại.");
            String salt = PasswordUtil.newSalt();
            User user = new User(this.store.getDatabase().takeNextUserId(), Role.FAN, username, name, salt,
                    PasswordUtil.hash(password, salt), true, this.store.now());
            this.store.getDatabase().getUsers().add(user);
            this.store.save();
            return user;
        }
    }

    public User login(String username, String password, Role expectedRole) {
        synchronized (this.store) {
            for (User u : this.store.getDatabase().getUsers()) {
                if (u.getUsername().equalsIgnoreCase(username == null ? "" : username.trim())
                        && u.getRole() == expectedRole && u.getActive() && PasswordUtil.verify(password, u.getSalt(), u.getPasswordHash()))
                    return u;
            }
            throw new IllegalArgumentException("Sai tài khoản/mật khẩu, sai vai trò hoặc tài khoản đã bị khóa.");
        }
    }

    public User requireActive(int userId, Role role) {
        synchronized (this.store) {
            for (User u : this.store.getDatabase().getUsers())
                if (u.getId() == userId && u.getActive() && u.getRole() == role) return u;
            throw new IllegalArgumentException("Không có quyền truy cập hoặc tài khoản đã bị khóa.");
        }
    }

    public User getById(int userId) {
        synchronized (this.store) {
            for (User u : this.store.getDatabase().getUsers()) if (u.getId() == userId) return u;
            throw new IllegalArgumentException("Không tìm thấy người dùng #" + userId);
        }
    }

    public void changePassword(int id, String oldPassword, String newPassword) {
        validatePassword(newPassword);
        synchronized (this.store) {
            User u = this.getById(id);
            if (!u.getActive() || !PasswordUtil.verify(oldPassword, u.getSalt(), u.getPasswordHash()))
                throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác.");
            this.setPassword(u, newPassword);
            this.store.save();
        }
    }

    public void changeName(int id, String name) {
        name = name == null ? "" : name.trim();
        if (name.length() < 2 || name.length() > 80) throw new IllegalArgumentException("Họ tên phải dài từ 2 đến 80 ký tự.");
        synchronized (this.store) {
            User u = this.getById(id);
            if (!u.getActive()) throw new IllegalArgumentException("Tài khoản đã bị khóa.");
            u.setName(name);
            this.store.save();
        }
    }

    void setPassword(User u, String password) {
        validatePassword(password);
        String salt = PasswordUtil.newSalt();
        u.setPasswordCredentials(salt, PasswordUtil.hash(password, salt));
    }

    private static void validateUsername(String username) {
        if (!username.matches("[a-z0-9_]{3,24}"))
            throw new IllegalArgumentException("Tên đăng nhập 3-24 ký tự, chỉ a-z, 0-9 và _.");
    }

    private static void validatePassword(String password) {
        if (password == null || password.length() < 6 || password.length() > 100)
            throw new IllegalArgumentException("Mật khẩu phải từ 6 đến 100 ký tự.");
    }
}
