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
        synchronized (store) {
            for (User u : store.db.users)
                if (u.username.equalsIgnoreCase(username)) throw new IllegalArgumentException("Tên đăng nhập đã tồn tại.");
            String salt = PasswordUtil.newSalt();
            User user = new User(store.db.nextUserId++, Role.FAN, username, name, salt,
                    PasswordUtil.hash(password, salt), true, store.now());
            store.db.users.add(user);
            store.save();
            return user;
        }
    }

    public User login(String username, String password, Role expectedRole) {
        synchronized (store) {
            for (User u : store.db.users) {
                if (u.username.equalsIgnoreCase(username == null ? "" : username.trim())
                        && u.role == expectedRole && u.active && PasswordUtil.verify(password, u.salt, u.passwordHash))
                    return u;
            }
            throw new IllegalArgumentException("Sai tài khoản/mật khẩu, sai vai trò hoặc tài khoản đã bị khóa.");
        }
    }

    public User requireActive(int userId, Role role) {
        synchronized (store) {
            for (User u : store.db.users)
                if (u.id == userId && u.active && u.role == role) return u;
            throw new IllegalArgumentException("Không có quyền truy cập hoặc tài khoản đã bị khóa.");
        }
    }

    public User getById(int userId) {
        synchronized (store) {
            for (User u : store.db.users) if (u.id == userId) return u;
            throw new IllegalArgumentException("Không tìm thấy người dùng #" + userId);
        }
    }

    public void changePassword(int id, String oldPassword, String newPassword) {
        validatePassword(newPassword);
        synchronized (store) {
            User u = getById(id);
            if (!u.active || !PasswordUtil.verify(oldPassword, u.salt, u.passwordHash))
                throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác.");
            setPassword(u, newPassword);
            store.save();
        }
    }

    public void changeName(int id, String name) {
        name = name == null ? "" : name.trim();
        if (name.length() < 2 || name.length() > 80) throw new IllegalArgumentException("Họ tên phải dài từ 2 đến 80 ký tự.");
        synchronized (store) {
            User u = getById(id);
            if (!u.active) throw new IllegalArgumentException("Tài khoản đã bị khóa.");
            u.name = name;
            store.save();
        }
    }

    void setPassword(User u, String password) {
        validatePassword(password);
        u.salt = PasswordUtil.newSalt();
        u.passwordHash = PasswordUtil.hash(password, u.salt);
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
