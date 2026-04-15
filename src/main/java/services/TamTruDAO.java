package services;

import models.TamTruTamVang;
import services.db.MysqlConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TamTruDAO {

    // ================== CRUD ==================

    // 1. Lấy toàn bộ
    public List<TamTruTamVang> getAll() {
        List<TamTruTamVang> list = new ArrayList<>();
        String sql = "SELECT * FROM tam_tru_tam_vang ORDER BY id DESC";

        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Lỗi getAll TamTru: " + e.getMessage());
        }

        return list;
    }

    // 2. Lấy theo ID
    public TamTruTamVang getById(int id) {
        String sql = "SELECT * FROM tam_tru_tam_vang WHERE id = ?";

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

    // 3. Insert
    public boolean insert(TamTruTamVang t) {
        if (!isValid(t)) return false;

        String sql = "INSERT INTO tam_tru_tam_vang " +
                "(nhan_khau_id, loai, tu_ngay, den_ngay, dia_chi_tam_tru, ly_do, trang_thai) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setPreparedStatement(ps, t, false);

            int rows = ps.executeUpdate();

            // lấy ID tự tăng
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        t.setId(rs.getInt(1));
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Lỗi insert: " + e.getMessage());
        }

        return false;
    }

    // 4. Update
    public boolean update(TamTruTamVang t) {
        if (!isValid(t)) return false;

        String sql = "UPDATE tam_tru_tam_vang SET " +
                "nhan_khau_id=?, loai=?, tu_ngay=?, den_ngay=?, " +
                "dia_chi_tam_tru=?, ly_do=?, trang_thai=? " +
                "WHERE id=?";

        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            setPreparedStatement(ps, t, true);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi update: " + e.getMessage());
        }

        return false;
    }

    // 5. Delete
    public boolean delete(int id) {
        String sql = "DELETE FROM tam_tru_tam_vang WHERE id = ?";

        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi delete: " + e.getMessage());
        }

        return false;
    }

    // ================== SEARCH / FILTER ==================

    // 6. Tìm theo nhân khẩu
    public List<TamTruTamVang> findByNhanKhauId(int nhanKhauId) {
        List<TamTruTamVang> list = new ArrayList<>();
        String sql = "SELECT * FROM tam_tru_tam_vang WHERE nhan_khau_id = ?";

        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, nhanKhauId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Lỗi findByNhanKhauId: " + e.getMessage());
        }

        return list;
    }

    // 7. Lọc theo trạng thái
    public List<TamTruTamVang> findByTrangThai(String trangThai) {
        List<TamTruTamVang> list = new ArrayList<>();
        String sql = "SELECT * FROM tam_tru_tam_vang WHERE trang_thai = ?";

        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, trangThai);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Lỗi findByTrangThai: " + e.getMessage());
        }

        return list;
    }

    // 8. Lọc theo khoảng thời gian
    public List<TamTruTamVang> findByDateRange(LocalDate from, LocalDate to) {
        List<TamTruTamVang> list = new ArrayList<>();
        String sql = "SELECT * FROM tam_tru_tam_vang WHERE tu_ngay >= ? AND den_ngay <= ?";

        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Lỗi findByDateRange: " + e.getMessage());
        }

        return list;
    }

    // ================== HELPER ==================

    // map ResultSet -> Object
    private TamTruTamVang mapResultSet(ResultSet rs) throws SQLException {
        TamTruTamVang t = new TamTruTamVang();

        t.setId(rs.getInt("id"));
        t.setNhanKhauId(rs.getInt("nhan_khau_id"));
        t.setLoai(rs.getString("loai"));

        Date tuNgay = rs.getDate("tu_ngay");
        if (tuNgay != null) t.setTuNgay(tuNgay.toLocalDate());

        Date denNgay = rs.getDate("den_ngay");
        if (denNgay != null) t.setDenNgay(denNgay.toLocalDate());

        t.setDiaChiTamTru(rs.getString("dia_chi_tam_tru"));
        t.setLyDo(rs.getString("ly_do"));
        t.setTrangThai(rs.getString("trang_thai"));

        return t;
    }

    // set param cho PreparedStatement
    private void setPreparedStatement(PreparedStatement ps, TamTruTamVang t, boolean isUpdate) throws SQLException {
        ps.setInt(1, t.getNhanKhauId());
        ps.setString(2, t.getLoai());

        // tuNgay (NOT NULL)
        ps.setDate(3, Date.valueOf(t.getTuNgay()));

        // denNgay (có thể null)
        if (t.getDenNgay() != null) {
            ps.setDate(4, Date.valueOf(t.getDenNgay()));
        } else {
            ps.setNull(4, Types.DATE);
        }

        ps.setString(5, t.getDiaChiTamTru());
        ps.setString(6, t.getLyDo());
        ps.setString(7, t.getTrangThai());

        if (isUpdate) {
            ps.setInt(8, t.getId());
        }
    }

    // validate cơ bản
    private boolean isValid(TamTruTamVang t) {
        if (t == null) return false;
        if (t.getNhanKhauId() <= 0) return false;
        if (t.getLoai() == null || t.getLoai().isEmpty()) return false;
        if (t.getTuNgay() == null) return false;
        return true;
    }
}