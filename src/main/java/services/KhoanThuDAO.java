package services;

import models.KhoanThu;
import services.db.MysqlConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class KhoanThuDAO {

    // 1. Lấy tất cả khoản thu
    public List<KhoanThu> getAllKhoanThu() {
        List<KhoanThu> danhSach = new ArrayList<>();
        String sql = "SELECT * FROM khoan_thu";

        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                danhSach.add(mapResultSet(rs));
            }
        } catch (Exception e) {
            System.err.println("Lỗi KhoanThuDAO: " + e.getMessage());
        }
        return danhSach;
    }

    // 2. Đếm tổng số khoản thu (dùng cho Dashboard)
    public int countTotal() {
        String sql = "SELECT COUNT(*) FROM khoan_thu";
        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            System.err.println("Lỗi KhoanThuDAO: " + e.getMessage());
        }
        return 0;
    }

    // 3. Thêm khoản thu mới
    public boolean themKhoanThu(KhoanThu kt) {
        String sql = "INSERT INTO khoan_thu(ma_khoan, ten_khoan, loai, so_tien, cach_tinh, so_thang, thang_thu, han_nop, mo_ta, trang_thai, don_gia_xe_may, don_gia_o_to) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, kt.getMaKhoan());
            pstmt.setString(2, kt.getTenKhoan());
            pstmt.setString(3, kt.getLoai());
            if (kt.getSoTien() != null) pstmt.setDouble(4, kt.getSoTien());
            else pstmt.setNull(4, Types.DECIMAL);
            pstmt.setString(5, kt.getCachTinh() != null ? kt.getCachTinh() : "FLAT");
            pstmt.setInt(6, Math.max(1, kt.getSoThang()));
            pstmt.setDate(7, kt.getThangThu());
            pstmt.setDate(8, kt.getHanNop());
            pstmt.setString(9, kt.getMoTa());
            pstmt.setString(10, kt.getTrangThai());
            if (kt.getDonGiaXeMay() != null) pstmt.setDouble(11, kt.getDonGiaXeMay());
            else pstmt.setNull(11, Types.DECIMAL);
            if (kt.getDonGiaOTo() != null) pstmt.setDouble(12, kt.getDonGiaOTo());
            else pstmt.setNull(12, Types.DECIMAL);

            boolean ok = pstmt.executeUpdate() > 0;
            if (ok) AuditService.log("THEM", "Khoản thu", "Thêm: " + kt.getMaKhoan() + " – " + kt.getTenKhoan());
            return ok;

        } catch (Exception e) {
            System.err.println("Lỗi KhoanThuDAO: " + e.getMessage());
            return false;
        }
    }

    // 4. Cập nhật khoản thu
    public boolean capNhatKhoanThu(KhoanThu kt) {
        String sql = "UPDATE khoan_thu SET ma_khoan=?, ten_khoan=?, loai=?, so_tien=?, cach_tinh=?, so_thang=?, thang_thu=?, han_nop=?, mo_ta=?, trang_thai=?, don_gia_xe_may=?, don_gia_o_to=? WHERE id=?";

        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, kt.getMaKhoan());
            pstmt.setString(2, kt.getTenKhoan());
            pstmt.setString(3, kt.getLoai());
            if (kt.getSoTien() != null) pstmt.setDouble(4, kt.getSoTien());
            else pstmt.setNull(4, Types.DECIMAL);
            pstmt.setString(5, kt.getCachTinh() != null ? kt.getCachTinh() : "FLAT");
            pstmt.setInt(6, Math.max(1, kt.getSoThang()));
            pstmt.setDate(7, kt.getThangThu());
            pstmt.setDate(8, kt.getHanNop());
            pstmt.setString(9, kt.getMoTa());
            pstmt.setString(10, kt.getTrangThai());
            if (kt.getDonGiaXeMay() != null) pstmt.setDouble(11, kt.getDonGiaXeMay());
            else pstmt.setNull(11, Types.DECIMAL);
            if (kt.getDonGiaOTo() != null) pstmt.setDouble(12, kt.getDonGiaOTo());
            else pstmt.setNull(12, Types.DECIMAL);
            pstmt.setInt(13, kt.getId());

            boolean ok = pstmt.executeUpdate() > 0;
            if (ok) AuditService.log("SUA", "Khoản thu", "Sửa: " + kt.getMaKhoan() + " – " + kt.getTenKhoan());
            return ok;

        } catch (Exception e) {
            System.err.println("Lỗi KhoanThuDAO: " + e.getMessage());
            return false;
        }
    }

    // 5. Xóa khoản thu
    public boolean xoaKhoanThu(int id) {
        String sql = "DELETE FROM khoan_thu WHERE id = ?";
        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            boolean ok = pstmt.executeUpdate() > 0;
            if (ok) AuditService.log("XOA", "Khoản thu", "Xóa khoản thu id=" + id);
            return ok;
        } catch (Exception e) {
            System.err.println("Lỗi KhoanThuDAO: " + e.getMessage());
            return false;
        }
    }

    // Helper: map ResultSet -> KhoanThu object
    private KhoanThu mapResultSet(ResultSet rs) throws Exception {
        KhoanThu kt = new KhoanThu();
        kt.setId(rs.getInt("id"));
        kt.setMaKhoan(rs.getString("ma_khoan"));
        kt.setTenKhoan(rs.getString("ten_khoan"));
        kt.setLoai(rs.getString("loai"));
        Object soTienObj = rs.getObject("so_tien");
        kt.setSoTien(soTienObj != null ? ((Number) soTienObj).doubleValue() : null);
        try { kt.setCachTinh(rs.getString("cach_tinh")); } catch (Exception ignore) {}
        try { kt.setSoThang(rs.getInt("so_thang")); } catch (Exception ignore) {}
        try { Object o = rs.getObject("don_gia_xe_may"); kt.setDonGiaXeMay(o != null ? ((Number) o).doubleValue() : null); } catch (Exception ignore) {}
        try { Object o = rs.getObject("don_gia_o_to");  kt.setDonGiaOTo(o != null ? ((Number) o).doubleValue() : null); } catch (Exception ignore) {}
        kt.setThangThu(rs.getDate("thang_thu"));
        kt.setHanNop(rs.getDate("han_nop"));
        kt.setMoTa(rs.getString("mo_ta"));
        kt.setTrangThai(rs.getString("trang_thai"));
        return kt;
    }
}