package services;

import models.User;
import services.db.MysqlConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // 1. READ (Kiểm tra Đăng nhập) - Dùng cho form Login của Hiển
    public User kiemTraDangNhap(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapUser(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
        return users;
    }

    // 3. CREATE (Tạo tài khoản mới)
    public boolean addUser(User user) {
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole());

            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
        u.setCreatedAt(rs.getTimestamp("created_at"));
        u.setUpdatedAt(rs.getTimestamp("updated_at"));
        return u;
    }
}