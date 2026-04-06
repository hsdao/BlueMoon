package services;

import models.HoKhau;
import services.db.MysqlConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

// DAO - Interact với bảng ho_khau trong database
public class HoKhauDAO {

    // 1. Lấy danh sách toàn bộ hộ khẩu từ database
    public List<HoKhau> getAllHoKhau() {
        List<HoKhau> danhSach = new ArrayList<>();
        String sql = "SELECT * FROM ho_khau";

        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
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

                danhSach.add(hk);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return danhSach;
    }

    // 2. Thêm hộ khẩu mới vào database
    public boolean themHoKhau(HoKhau hk) {
        String sql = "INSERT INTO ho_khau(ma_ho, chu_ho_id, so_dien_thoai_chu_ho, so_thanh_vien, dia_chi, ngay_tao, trang_thai, ghi_chu) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, hk.getMaHo());

            // Nếu chu_ho_id không có thì ghi NULL vào database
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

    // 3. Cập nhật thông tin hộ khẩu theo ID
    public boolean capNhatHoKhau(HoKhau hk) {
        String sql = "UPDATE ho_khau SET ma_ho=?, chu_ho_id=?, so_dien_thoai_chu_ho=?, so_thanh_vien=?, dia_chi=?, ngay_tao=?, trang_thai=?, ghi_chu=? WHERE id=?";

        try (Connection conn = services.db.MysqlConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, hk.getMaHo());

            if (hk.getChuHoId() != null) pstmt.setInt(2, hk.getChuHoId());
            else pstmt.setNull(2, Types.INTEGER);

            pstmt.setInt(9, hk.getId());

            return pstmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 4. Xóa hộ khẩu theo ID
    public boolean xoaHoKhau(int id) {
        String sql = "DELETE FROM ho_khau WHERE id = ?";
        try (Connection conn = services.db.MysqlConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}