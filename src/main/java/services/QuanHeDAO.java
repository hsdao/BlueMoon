package services;

import models.QuanHe;
import services.db.MysqlConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuanHeDAO {

    // ================== CRUD ==================

    public List<QuanHe> getAll() {
        List<QuanHe> list = new ArrayList<>();
        String sql = "SELECT * FROM quan_he ORDER BY id ASC";

        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Lỗi getAll QuanHe: " + e.getMessage());
        }

        return list;
    }

    public boolean insert(QuanHe q) {
        try (Connection conn = MysqlConnection.getConnection()) {
            return insertWithConnection(conn, q);
        } catch (Exception e) {
            System.err.println("Lỗi QuanHeDAO: " + e.getMessage());
        }
        return false;
    }

    // ⭐ dùng cho transaction chung
    public boolean insertWithConnection(Connection conn, QuanHe q) {
        if (!isValid(q)) return false;

        String sql = "INSERT INTO quan_he (ten_quan_he) VALUES (?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, q.getTenQuanHe());

            int rows = ps.executeUpdate();

            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        q.setId(rs.getInt(1));
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Lỗi insert QuanHe: " + e.getMessage());
        }

        return false;
    }

    // ================== HELPER ==================

    private QuanHe mapResultSet(ResultSet rs) throws SQLException {
        return new QuanHe(
                rs.getInt("id"),
                rs.getString("ten_quan_he")
        );
    }

    private boolean isValid(QuanHe q) {
        if (q == null) return false;
        if (q.getTenQuanHe() == null || q.getTenQuanHe().trim().isEmpty()) return false;
        return true;
    }
}