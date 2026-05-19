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

    public QuanHe getById(int id) {
        String sql = "SELECT * FROM quan_he WHERE id = ?";

        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Lỗi getById: " + e.getMessage());
        }

        return null;
    }

    public boolean insert(QuanHe q) {
        try (Connection conn = MysqlConnection.getConnection()) {
            return insertWithConnection(conn, q);
        } catch (Exception e) {
            e.printStackTrace();
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

    public boolean update(QuanHe q) {
        if (!isValid(q)) return false;

        String sql = "UPDATE quan_he SET ten_quan_he = ? WHERE id = ?";

        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, q.getTenQuanHe());
            ps.setInt(2, q.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi update QuanHe: " + e.getMessage());
        }

        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM quan_he WHERE id = ?";

        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi delete QuanHe: " + e.getMessage());
        }

        return false;
    }

    // ================== SEARCH ==================

    public List<QuanHe> searchByName(String keyword) {
        List<QuanHe> list = new ArrayList<>();
        String sql = "SELECT * FROM quan_he WHERE ten_quan_he LIKE ?";

        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Lỗi search QuanHe: " + e.getMessage());
        }

        return list;
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