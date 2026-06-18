package services;

import models.NopTien;
import services.db.MysqlConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NopTienDAO {

    // Map ResultSet -> Object (tái sử dụng)
    private NopTien mapResultSet(ResultSet rs) throws SQLException {
        NopTien nt = new NopTien();
        nt.setId(rs.getInt("id"));
        nt.setKhoanThuId(rs.getInt("khoan_thu_id"));
        nt.setHoKhauId(rs.getInt("ho_khau_id"));
        nt.setSoTien(rs.getBigDecimal("so_tien"));
        nt.setNguoiThu(rs.getString("nguoi_thu"));
        nt.setGhiChu(rs.getString("ghi_chu"));
        Date ngay = rs.getDate("ngay_nop");
        if (ngay != null) nt.setNgayNop(ngay.toLocalDate());
        return nt;
    }

    // 1. Lấy toàn bộ
    public List<NopTien> getAllNopTien() {
        List<NopTien> danhSach = new ArrayList<>();
        String sql = "SELECT id, khoan_thu_id, ho_khau_id, so_tien, nguoi_thu, ghi_chu, ngay_nop FROM nop_tien";

        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                danhSach.add(mapResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách nộp tiền: " + e.getMessage());
        }
        return danhSach;
    }

    // 2. Tính tổng tiền thu theo tháng/năm (dùng cho Dashboard)
    public BigDecimal sumByMonth(int month, int year) {
        String sql = "SELECT COALESCE(SUM(so_tien), 0) FROM nop_tien " +
                     "WHERE MONTH(ngay_nop) = ? AND YEAR(ngay_nop) = ?";
        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, month);
            pstmt.setInt(2, year);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal(1);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi sumByMonth: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    // 3. Tính tổng tiền thu theo từng tháng trong năm (dùng cho biểu đồ Dashboard)
    public List<BigDecimal> sumByEachMonthInYear(int year) {
        List<BigDecimal> result = new ArrayList<>();
        for (int i = 0; i < 12; i++) result.add(BigDecimal.ZERO);

        String sql = "SELECT MONTH(ngay_nop) AS thang, SUM(so_tien) AS tong " +
                     "FROM nop_tien WHERE YEAR(ngay_nop) = ? GROUP BY MONTH(ngay_nop)";
        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, year);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int thang = rs.getInt("thang");
                    BigDecimal tong = rs.getBigDecimal("tong");
                    if (thang >= 1 && thang <= 12 && tong != null) {
                        result.set(thang - 1, tong);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi sumByEachMonthInYear: " + e.getMessage());
        }
        return result;
    }

    // 4. Thêm mới
    public boolean themNopTien(NopTien nt) {
        String sql = "INSERT INTO nop_tien(khoan_thu_id, ho_khau_id, so_tien, nguoi_thu, ghi_chu, ngay_nop) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, nt.getKhoanThuId());
            pstmt.setInt(2, nt.getHoKhauId());
            if (nt.getSoTien() != null) pstmt.setBigDecimal(3, nt.getSoTien());
            else pstmt.setNull(3, Types.DECIMAL);
            pstmt.setString(4, nt.getNguoiThu());
            pstmt.setString(5, nt.getGhiChu());
            if (nt.getNgayNop() != null) pstmt.setDate(6, Date.valueOf(nt.getNgayNop()));
            else pstmt.setNull(6, Types.DATE);

            boolean ok = pstmt.executeUpdate() > 0;
            if (ok) AuditService.log("THU_PHI", "Nộp tiền",
                    "Hộ id=" + nt.getHoKhauId() + " nộp khoản id=" + nt.getKhoanThuId()
                    + " số tiền=" + nt.getSoTien());
            return ok;

        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm nộp tiền: " + e.getMessage());
            return false;
        }
    }

    // 5. Cập nhật
    public boolean capNhatNopTien(NopTien nt) {
        String sql = "UPDATE nop_tien SET khoan_thu_id=?, ho_khau_id=?, so_tien=?, nguoi_thu=?, ghi_chu=?, ngay_nop=? WHERE id=?";

        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, nt.getKhoanThuId());
            pstmt.setInt(2, nt.getHoKhauId());
            if (nt.getSoTien() != null) pstmt.setBigDecimal(3, nt.getSoTien());
            else pstmt.setNull(3, Types.DECIMAL);
            pstmt.setString(4, nt.getNguoiThu());
            pstmt.setString(5, nt.getGhiChu());
            if (nt.getNgayNop() != null) pstmt.setDate(6, Date.valueOf(nt.getNgayNop()));
            else pstmt.setNull(6, Types.DATE);
            pstmt.setInt(7, nt.getId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật nộp tiền: " + e.getMessage());
            return false;
        }
    }

    // 6. Xóa
    public boolean xoaNopTien(int id) {
        String sql = "DELETE FROM nop_tien WHERE id = ?";
        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            boolean ok = pstmt.executeUpdate() > 0;
            if (ok) AuditService.log("XOA", "Nộp tiền", "Xóa bản ghi nộp tiền id=" + id);
            return ok;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa nộp tiền: " + e.getMessage());
            return false;
        }
    }

    // ===== ĐỐI SOÁT QUỸ =====

    /** Tổng thu theo từng người thu (đối soát tiền mặt theo thu ngân). */
    public java.util.List<models.DoiSoatRow> tongTheoNguoiThu() {
        String sql = "SELECT COALESCE(NULLIF(TRIM(nguoi_thu),''),'(không rõ)') ng, "
                   + "COUNT(*) sl, COALESCE(SUM(so_tien),0) t "
                   + "FROM nop_tien GROUP BY ng ORDER BY t DESC";
        return aggregate(sql);
    }

    /** Tổng thu theo từng ngày. */
    public java.util.List<models.DoiSoatRow> tongTheoNgay() {
        String sql = "SELECT DATE_FORMAT(ngay_nop,'%d/%m/%Y') ng, "
                   + "COUNT(*) sl, COALESCE(SUM(so_tien),0) t "
                   + "FROM nop_tien GROUP BY ngay_nop ORDER BY ngay_nop DESC";
        return aggregate(sql);
    }

    private java.util.List<models.DoiSoatRow> aggregate(String sql) {
        java.util.List<models.DoiSoatRow> list = new ArrayList<>();
        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new models.DoiSoatRow(rs.getString("ng"), rs.getInt("sl"), rs.getBigDecimal("t")));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi đối soát: " + e.getMessage());
        }
        return list;
    }

    /** Đếm số lượt nộp của một khoản thu (chặn xóa khoản thu nếu đã có người nộp). */
    public int countByKhoanThu(int khoanThuId) {
        String sql = "SELECT COUNT(*) FROM nop_tien WHERE khoan_thu_id = ?";
        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, khoanThuId);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        } catch (SQLException e) {
            System.err.println("Lỗi countByKhoanThu: " + e.getMessage());
        }
        return 0;
    }
}