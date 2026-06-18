package application;

import models.User;

public class UserSession {
    private static UserSession instance;
    private User currentUser; // Biến lưu trữ người dùng đang đăng nhập

    // Private constructor để ngăn việc tạo đối tượng mới bằng từ khóa 'new'
    private UserSession() {}

    // Hàm lấy thể hiện duy nhất của Session
    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public void cleanUserSession() {
        this.currentUser = null; // Gọi hàm này khi người dùng Đăng xuất
    }
}