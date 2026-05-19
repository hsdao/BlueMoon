package services;

import models.HoKhau;
import services.db.MysqlConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class HoKhauDAO {

    // 1. Lấy danh sách toàn bộ hộ khẩu
    public List<HoKhau> getAllHoKhau() {
        List<HoKhau> danhSach = new ArrayList<>();
        String sql = "SELECT * FROM ho_khau";

        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                danhSach.add(mapResultSet(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
        return 0;
    }

    // 3. Lấy tên chủ hộ theo chu_ho_id (dùng để hiển thị cột Chủ Hộ)
    public String getTenChuHo(Integer chuHoId) {
        if (chuHoId == null) return "—";
        String sql = "SELECT ho_ten FROM nhan_khau WHERE id = ?";
        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, chuHoId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getString("ho_ten");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "ID: " + chuHoId;
    }

    // 4. Thêm hộ khẩu mới
    public boolean themHoKhau(HoKhau hk) {
        String sql = "INSERT INTO ho_khau(ma_ho, chu_ho_id, so_dien_thoai_chu_ho, so_thanh_vien, dia_chi, ngay_tao, trang_thai, ghi_chu) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, hk.getMaHo());
            if (hk.getChuHoId() != null) pstmt.setInt(2, hk.getChuHoId());
            else pstmt.setNull(2, Types.INTEGER);
            pstmt.setString(3, hk.getSoDienThoaiChuHo());
            pstmt.setInt(4, hk.getSoThanhVien());
            pstmt.setString(5, hk.getDiaChi());
            pstmt.setTimestamp(6, hk.getNgayTao());
            pstmt.setString(7, hk.getTrangThai());
            pstmt.setString(8, hk.getGhiChu());

            return pstmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 5. Cập nhật hộ khẩu
    public boolean capNhatHoKhau(HoKhau hk) {
        String sql = "UPDATE ho_khau SET ma_ho=?, chu_ho_id=?, so_dien_thoai_chu_ho=?, so_thanh_vien=?, dia_chi=?, ngay_tao=?, trang_thai=?, ghi_chu=? WHERE id=?";

        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, hk.getMaHo());
            if (hk.getChuHoId() != null) pstmt.setInt(2, hk.getChuHoId());
            else pstmt.setNull(2, Types.INTEGER);
            pstmt.setString(3, hk.getSoDienThoaiChuHo());
            pstmt.setInt(4, hk.getSoThanhVien());
            pstmt.setString(5, hk.getDiaChi());
            pstmt.setTimestamp(6, hk.getNgayTao());
            pstmt.setString(7, hk.getTrangThai());
            pstmt.setString(8, hk.getGhiChu());
            pstmt.setInt(9, hk.getId());

            return pstmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
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
            e.printStackTrace();
            return false;
        }
    }

    // 6. Xóa hộ khẩu
    public boolean xoaHoKhau(int id) {
        String sql = "DELETE FROM ho_khau WHERE id = ?";
        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
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
        hk.setDiaChi(rs.getString("dia_chi"));
        hk.setNgayTao(rs.getTimestamp("ngay_tao"));
        hk.setTrangThai(rs.getString("trang_thai"));
        hk.setGhiChu(rs.getString("ghi_chu"));
        return hk;
    }
}