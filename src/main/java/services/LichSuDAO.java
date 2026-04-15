package services;

import models.LichSuBienDong;
import services.db.MysqlConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LichSuDAO {

    // ================== CRUD ==================

    public List<LichSuBienDong> getAll() {
        List<LichSuBienDong> list = new ArrayList<>();
        String sql = "SELECT * FROM lich_su_bien_dong ORDER BY ngay_bien_dong DESC";

        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Lỗi getAll: " + e.getMessage());
        }

        return list;
    }

    public boolean insert(LichSuBienDong l) {
        try (Connection conn = MysqlConnection.getConnection()) {
            return insertWithConnection(conn, l);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ================== CORE TRANSACTION METHOD ==================

    public boolean insertWithConnection(Connection conn, LichSuBienDong l) {
        if (!isValid(l)) return false;

        String sql = "INSERT INTO lich_su_bien_dong " +
                "(nhan_khau_id, ho_khau_id, loai_bien_dong, ngay_bien_dong, ghi_chu, nguoi_thuc_hien) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setPreparedStatement(ps, l);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        l.setId(rs.getInt(1));
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Lỗi insertWithConnection: " + e.getMessage());
        }

        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM lich_su_bien_dong WHERE id = ?";

        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi delete: " + e.getMessage());
        }

        return false;
    }

    // ================== AUTO LOG (GIỐNG SERVICE) ==================

    public boolean ghiLog(int nhanKhauId, Integer hoKhauId,
                          String loai, String ghiChu, String nguoi) {

        LichSuBienDong log = new LichSuBienDong();
        log.setNhanKhauId(nhanKhauId);
        log.setHoKhauId(hoKhauId);
        log.setLoaiBienDong(loai);
        log.setNgayBienDong(LocalDate.now());
        log.setGhiChu(ghiChu);
        log.setNguoiThucHien(nguoi);

        return insert(log);
    }

    // ================== TRANSACTION ALL-IN-ONE ==================

    /**
     * Ví dụ: thêm nhân khẩu + ghi log trong 1 transaction
     * (m sẽ gọi DAO khác ở đây nếu cần)
     */
    public boolean executeWithLog(
            InsertAction action,
            int nhanKhauId,
            Integer hoKhauId,
            String loai,
            String ghiChu,
            String nguoi) {

        Connection conn = null;

        try {
            conn = MysqlConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. chạy action chính (vd: insert nhân khẩu)
            boolean ok1 = action.execute(conn);

            if (!ok1) {
                conn.rollback();
                return false;
            }

            // 2. ghi log
            LichSuBienDong log = new LichSuBienDong();
            log.setNhanKhauId(nhanKhauId);
            log.setHoKhauId(hoKhauId);
            log.setLoaiBienDong(loai);
            log.setNgayBienDong(LocalDate.now());
            log.setGhiChu(ghiChu);
            log.setNguoiThucHien(nguoi);

            boolean ok2 = insertWithConnection(conn, log);

            if (!ok2) {
                conn.rollback();
                return false;
            }

            conn.commit();
            return true;

        } catch (Exception e) {
            try {
                if (conn != null) conn.rollback();
            } catch (Exception ignored) {}

            e.printStackTrace();
        }

        return false;
    }

    // ================== INTERFACE CHO ACTION ==================

    public interface InsertAction {
        boolean execute(Connection conn) throws Exception;
    }

    // ================== HELPER ==================

    private LichSuBienDong mapResultSet(ResultSet rs) throws SQLException {
        LichSuBienDong l = new LichSuBienDong();

        l.setId(rs.getInt("id"));
        l.setNhanKhauId(rs.getInt("nhan_khau_id"));

        int hoKhauId = rs.getInt("ho_khau_id");
        if (!rs.wasNull()) {
            l.setHoKhauId(hoKhauId);
        }

        l.setLoaiBienDong(rs.getString("loai_bien_dong"));

        Date ngay = rs.getDate("ngay_bien_dong");
        if (ngay != null) {
            l.setNgayBienDong(ngay.toLocalDate());
        }

        l.setGhiChu(rs.getString("ghi_chu"));
        l.setNguoiThucHien(rs.getString("nguoi_thuc_hien"));

        return l;
    }

    private void setPreparedStatement(PreparedStatement ps, LichSuBienDong l) throws SQLException {
        ps.setInt(1, l.getNhanKhauId());

        if (l.getHoKhauId() != null) {
            ps.setInt(2, l.getHoKhauId());
        } else {
            ps.setNull(2, Types.INTEGER);
        }

        ps.setString(3, l.getLoaiBienDong());
        ps.setDate(4, Date.valueOf(l.getNgayBienDong()));
        ps.setString(5, l.getGhiChu());
        ps.setString(6, l.getNguoiThucHien());
    }

    private boolean isValid(LichSuBienDong l) {
        if (l == null) return false;
        if (l.getNhanKhauId() <= 0) return false;
        if (l.getLoaiBienDong() == null || l.getLoaiBienDong().isEmpty()) return false;
        if (l.getNgayBienDong() == null) return false;
        return true;
    }
}