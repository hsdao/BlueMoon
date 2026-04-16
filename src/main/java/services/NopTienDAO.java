package services;

import models.NopTien;
import services.db.MysqlConnection;

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
        if (ngay != null) {
            nt.setNgayNop(ngay.toLocalDate());
        }

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
            throw new RuntimeException("Lỗi khi lấy danh sách nộp tiền", e);
        }

        return danhSach;
    }

    // 2. Thêm mới
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

            if (nt.getNgayNop() != null) {
                pstmt.setDate(6, Date.valueOf(nt.getNgayNop()));
            } else {
                pstmt.setNull(6, Types.DATE);
            }

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi thêm nộp tiền", e);
        }
    }

    // 3. UPDATE
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

            if (nt.getNgayNop() != null) {
                pstmt.setDate(6, Date.valueOf(nt.getNgayNop()));
            } else {
                pstmt.setNull(6, Types.DATE);
            }

            pstmt.setInt(7, nt.getId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi cập nhật nộp tiền", e);
        }
    }

    // 4. DELETE
    public boolean xoaNopTien(int id) {
        String sql = "DELETE FROM nop_tien WHERE id = ?";

        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi xóa nộp tiền", e);
        }
    }

    // 5. Lấy theo ID
    public NopTien getNopTienById(int id) {
        String sql = "SELECT id, khoan_thu_id, ho_khau_id, so_tien, nguoi_thu, ghi_chu, ngay_nop FROM nop_tien WHERE id = ?";

        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSet(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy nộp tiền theo ID", e);
        }

        return null;
    }
}