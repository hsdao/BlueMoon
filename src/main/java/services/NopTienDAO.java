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

            return pstmt.executeUpdate() > 0;

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
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa nộp tiền: " + e.getMessage());
            return false;
        }
    }

    // 7. Lấy theo ID
    public NopTien getNopTienById(int id) {
        String sql = "SELECT id, khoan_thu_id, ho_khau_id, so_tien, nguoi_thu, ghi_chu, ngay_nop FROM nop_tien WHERE id = ?";
        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy nộp tiền theo ID: " + e.getMessage());
        }
        return null;
    }
}