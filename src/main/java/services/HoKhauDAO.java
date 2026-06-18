package services;

import models.HoKhau;
import models.NhanKhau;
import services.db.MysqlConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class HoKhauDAO {

    // 1. Lấy danh sách toàn bộ hộ khẩu
    public List<HoKhau> getAllHoKhau() {
        List<HoKhau> danhSach = new ArrayList<>();
        // Sắp xếp theo SỐ phòng (tách chữ số trong ma_ho) để đúng thứ tự: P901 < P1005 < P1203...
        String sql = "SELECT * FROM ho_khau "
                   + "ORDER BY CAST(REGEXP_REPLACE(ma_ho, '[^0-9]', '') AS UNSIGNED), ma_ho";

        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                danhSach.add(mapResultSet(rs));
            }
        } catch (Exception e) {
            System.err.println("Lỗi HoKhauDAO: " + e.getMessage());
        }
        return danhSach;
    }

    // 2. Đếm tổng số hộ khẩu (dùng cho Dashboard)
    public int countTotal() {
        String sql = "SELECT COUNT(*) FROM ho_khau";
        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            System.err.println("Lỗi HoKhauDAO: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Thêm hộ khẩu MỚI + chủ hộ (nhân khẩu mới) trong MỘT GIAO DỊCH (nguyên tử).
     * Nếu một bước thất bại -> rollback toàn bộ (không để lại hộ thiếu chủ hộ).
     */
    public boolean themHoKhauVoiChuHo(HoKhau hk, NhanKhau chuHo) {
        String insHo = "INSERT INTO ho_khau(ma_ho, chu_ho_id, so_dien_thoai_chu_ho, so_thanh_vien, "
                + "dien_tich, so_xe_may, so_o_to, dia_chi, ngay_tao, trang_thai, ghi_chu) "
                + "VALUES (?, NULL, ?, 1, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = MysqlConnection.getConnection();
            conn.setAutoCommit(false);

            int hoId;
            try (PreparedStatement ps = conn.prepareStatement(insHo, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, hk.getMaHo());
                ps.setString(2, hk.getSoDienThoaiChuHo());
                ps.setDouble(3, hk.getDienTich());
                ps.setInt(4, hk.getSoXeMay());
                ps.setInt(5, hk.getSoOTo());
                ps.setString(6, hk.getDiaChi());
                ps.setTimestamp(7, hk.getNgayTao());
                ps.setString(8, hk.getTrangThai());
                ps.setString(9, hk.getGhiChu());
                if (ps.executeUpdate() == 0) { conn.rollback(); return false; }
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) hoId = keys.getInt(1);
                    else { conn.rollback(); return false; }
                }
            }
            hk.setId(hoId);

            // Tạo chủ hộ (nhân khẩu) trong cùng transaction
            chuHo.setHoKhauId(hoId);
            if (!new NhanKhauDAO().insertWithConnection(conn, chuHo)) { conn.rollback(); return false; }

            // Liên kết chủ hộ vào hộ
            try (PreparedStatement up = conn.prepareStatement("UPDATE ho_khau SET chu_ho_id=? WHERE id=?")) {
                up.setInt(1, chuHo.getId());
                up.setInt(2, hoId);
                up.executeUpdate();
            }
            hk.setChuHoId(chuHo.getId());

            conn.commit();
            AuditService.log("THEM", "Hộ khẩu", "Thêm hộ + chủ hộ: " + hk.getMaHo());
            return true;
        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (Exception ignore) {}
            System.err.println("Lỗi themHoKhauVoiChuHo: " + e.getMessage());
            return false;
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (Exception ignore) {}
        }
    }

    // 5. Cập nhật hộ khẩu
    public boolean capNhatHoKhau(HoKhau hk) {
        String sql = "UPDATE ho_khau SET ma_ho=?, chu_ho_id=?, so_dien_thoai_chu_ho=?, so_thanh_vien=?, dien_tich=?, so_xe_may=?, so_o_to=?, dia_chi=?, ngay_tao=?, trang_thai=?, ghi_chu=? WHERE id=?";

        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, hk.getMaHo());
            if (hk.getChuHoId() != null) pstmt.setInt(2, hk.getChuHoId());
            else pstmt.setNull(2, Types.INTEGER);
            pstmt.setString(3, hk.getSoDienThoaiChuHo());
            pstmt.setInt(4, hk.getSoThanhVien());
            pstmt.setDouble(5, hk.getDienTich());
            pstmt.setInt(6, hk.getSoXeMay());
            pstmt.setInt(7, hk.getSoOTo());
            pstmt.setString(8, hk.getDiaChi());
            pstmt.setTimestamp(9, hk.getNgayTao());
            pstmt.setString(10, hk.getTrangThai());
            pstmt.setString(11, hk.getGhiChu());
            pstmt.setInt(12, hk.getId());

            boolean ok = pstmt.executeUpdate() > 0;
            if (ok) AuditService.log("SUA", "Hộ khẩu", "Sửa hộ: " + hk.getMaHo());
            return ok;

        } catch (Exception e) {
            System.err.println("Lỗi HoKhauDAO: " + e.getMessage());
            return false;
        }
    }

    /**
     * Tính lại so_thanh_vien của TẤT CẢ hộ = số nhân khẩu thực tế trong hộ.
     * Gọi trước khi hiển thị danh sách để số thành viên luôn tự động & chính xác.
     */
    public void recomputeAllSoThanhVien() {
        // Chỉ đếm nhân khẩu CÒN Ở HỘ (loại người đã chuyển đi / đã mất)
        String sql = "UPDATE ho_khau h SET so_thanh_vien = "
                   + "(SELECT COUNT(*) FROM nhan_khau n WHERE n.ho_khau_id = h.id "
                   + " AND (n.trang_thai IS NULL OR n.trang_thai NOT IN ('MOVED','MOVED_OUT','DECEASED')))";
        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Lỗi recompute so_thanh_vien: " + e.getMessage());
        }
    }

    // Cập nhật số thành viên (dùng khi ghi biến động Khai tử / Chuyển đi)
    public boolean updateSoThanhVien(int hoKhauId, int soThanhVienMoi) {
        String sql = "UPDATE ho_khau SET so_thanh_vien=? WHERE id=?";
        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Math.max(0, soThanhVienMoi));
            pstmt.setInt(2, hoKhauId);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Lỗi HoKhauDAO: " + e.getMessage());
            return false;
        }
    }

    // 6. Xóa hộ khẩu
    public boolean xoaHoKhau(int id) {
        String sql = "DELETE FROM ho_khau WHERE id = ?";
        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            boolean ok = pstmt.executeUpdate() > 0;
            if (ok) AuditService.log("XOA", "Hộ khẩu", "Xóa hộ khẩu id=" + id);
            return ok;
        } catch (Exception e) {
            System.err.println("Lỗi HoKhauDAO: " + e.getMessage());
            return false;
        }
    }

    // Helper: map ResultSet -> HoKhau object
    private HoKhau mapResultSet(ResultSet rs) throws Exception {
        HoKhau hk = new HoKhau();
        hk.setId(rs.getInt("id"));
        hk.setMaHo(rs.getString("ma_ho"));
        Object chuHoIdObj = rs.getObject("chu_ho_id");
        hk.setChuHoId(chuHoIdObj != null ? ((Number) chuHoIdObj).intValue() : null);
        hk.setSoDienThoaiChuHo(rs.getString("so_dien_thoai_chu_ho"));
        hk.setSoThanhVien(rs.getInt("so_thanh_vien"));
        try { hk.setDienTich(rs.getDouble("dien_tich")); } catch (Exception ignore) {}
        try { hk.setSoXeMay(rs.getInt("so_xe_may")); } catch (Exception ignore) {}
        try { hk.setSoOTo(rs.getInt("so_o_to")); } catch (Exception ignore) {}
        hk.setDiaChi(rs.getString("dia_chi"));
        hk.setNgayTao(rs.getTimestamp("ngay_tao"));
        hk.setTrangThai(rs.getString("trang_thai"));
        hk.setGhiChu(rs.getString("ghi_chu"));
        return hk;
    }
}