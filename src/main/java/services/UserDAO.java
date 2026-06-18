package services;

import models.User;
import services.db.MysqlConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // 1. READ (Kiểm tra Đăng nhập)
    //    Lấy user theo username rồi đối chiếu mật khẩu qua PasswordUtil (hỗ trợ cả
    //    dữ liệu cũ dạng thô). KHÔNG so sánh mật khẩu trực tiếp trong SQL nữa.
    public User kiemTraDangNhap(String username, String password) {
        User u = findByUsername(username);
        if (u == null) return null;
        if (!PasswordUtil.verify(u.getPassword(), password)) return null;

        // Tự nâng cấp: nếu mật khẩu trong DB còn ở dạng thô -> băm lại cho lần sau.
        if (!PasswordUtil.isHashed(u.getPassword())) {
            u.setPassword(PasswordUtil.hash(password));
            updateUser(u);
        }
        return u;
    }

    // 1b. READ theo username (dùng cho đăng nhập & quên mật khẩu)
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapUser(rs);
            }
        } catch (Exception e) {
            System.err.println("Lỗi UserDAO: " + e.getMessage());
        }
        return null;
    }

    // 2. READ ALL (Lấy danh sách tất cả User) - Dùng cho màn hình Admin quản lý nhân viên
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                users.add(mapUser(rs));
            }
        } catch (Exception e) {
            System.err.println("Lỗi UserDAO: " + e.getMessage());
        }
        return users;
    }

    // 3. CREATE (Tạo tài khoản mới) - mật khẩu được BĂM trước khi lưu.
    public boolean addUser(User user) {
        String sql = "INSERT INTO users (username, password, role, full_name) VALUES (?, ?, ?, ?)";
        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String stored = PasswordUtil.isHashed(user.getPassword())
                    ? user.getPassword()
                    : PasswordUtil.hash(user.getPassword());
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, stored);
            pstmt.setString(3, user.getRole());
            pstmt.setString(4, user.getFullName());

            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Lỗi UserDAO: " + e.getMessage());
            return false;
        }
    }

    // 4. UPDATE (Cập nhật mật khẩu hoặc phân quyền)
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET password = ?, role = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getPassword());
            pstmt.setString(2, user.getRole());
            pstmt.setInt(3, user.getId());

            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Lỗi UserDAO: " + e.getMessage());
            return false;
        }
    }

    // 5. DELETE (Xóa tài khoản)
    public boolean deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Lỗi UserDAO: " + e.getMessage());
            return false;
        }
    }

    // Hàm hỗ trợ (Helper) để gom dữ liệu từ ResultSet vào Object User cho gọn code
    private User mapUser(ResultSet rs) throws Exception {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setRole(rs.getString("role"));
        try { u.setFullName(rs.getString("full_name")); } catch (Exception ignore) {}
        u.setCreatedAt(rs.getTimestamp("created_at"));
        u.setUpdatedAt(rs.getTimestamp("updated_at"));
        return u;
    }
}