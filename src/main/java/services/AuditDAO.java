package services;

import models.AuditLog;
import services.db.MysqlConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/** Truy cập bảng audit_log (nhật ký thao tác). */
public class AuditDAO {

    /** Ghi một dòng nhật ký. Không ném lỗi ra ngoài (nhật ký không được làm hỏng nghiệp vụ). */
    public void ghi(String username, String hanhDong, String doiTuong, String moTa) {
        String sql = "INSERT INTO audit_log(username, hanh_dong, doi_tuong, mo_ta) VALUES (?,?,?,?)";
        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, hanhDong);
            ps.setString(3, doiTuong);
            ps.setString(4, moTa);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("[Audit] Không ghi được nhật ký: " + e.getMessage());
        }
    }

    /** Lấy các dòng nhật ký mới nhất (giới hạn số dòng). */
    public List<AuditLog> getRecent(int limit) {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT id, username, hanh_dong, doi_tuong, mo_ta, thoi_gian "
                   + "FROM audit_log ORDER BY thoi_gian DESC, id DESC LIMIT ?";
        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AuditLog a = new AuditLog();
                    a.setId(rs.getInt("id"));
                    a.setUsername(rs.getString("username"));
                    a.setHanhDong(rs.getString("hanh_dong"));
                    a.setDoiTuong(rs.getString("doi_tuong"));
                    a.setMoTa(rs.getString("mo_ta"));
                    a.setThoiGian(rs.getTimestamp("thoi_gian"));
                    list.add(a);
                }
            }
        } catch (Exception e) {
            System.err.println("[Audit] Lỗi đọc nhật ký: " + e.getMessage());
        }
        return list;
    }
}
