package services;

import models.NopTien;
import services.db.MysqlConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class NopTienDAO {

    // 1. Lấy toàn bộ danh sách nộp tiền
    public List<NopTien> getAllNopTien() {
        List<NopTien> danhSach = new ArrayList<>();
        String sql = "SELECT * FROM nop_tien";

        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                NopTien nt = new NopTien();

                nt.setId(rs.getInt("id"));
                nt.setKhoanThuId(rs.getInt("khoan_thu_id"));
                nt.setHoKhauId(rs.getInt("ho_khau_id"));
                nt.setSoTien(rs.getBigDecimal("so_tien"));
                nt.setNguoiThu(rs.getString("nguoi_thu"));
                nt.setGhiChu(rs.getString("ghi_chu"));

                if (rs.getDate("ngay_nop") != null) {
                    nt.setNgayNop(rs.getDate("ngay_nop").toLocalDate());
                }

                danhSach.add(nt);
            }

        } catch (Exception e) {
            e.printStackTrace();
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
                pstmt.setDate(6, java.sql.Date.valueOf(nt.getNgayNop()));
            } else {
                pstmt.setNull(6, Types.DATE);
            }

            return pstmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
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
                pstmt.setDate(6, java.sql.Date.valueOf(nt.getNgayNop()));
            } else {
                pstmt.setNull(6, Types.DATE);
            }

            pstmt.setInt(7, nt.getId());

            return pstmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 4. DELETE
    public boolean xoaNopTien(int id) {
        String sql = "DELETE FROM nop_tien WHERE id = ?";

        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 5. Lấy theo ID (bonus thêm cho đầy đủ)
    public NopTien getNopTienById(int id) {
        String sql = "SELECT * FROM nop_tien WHERE id = ?";

        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                NopTien nt = new NopTien();

                nt.setId(rs.getInt("id"));
                nt.setKhoanThuId(rs.getInt("khoan_thu_id"));
                nt.setHoKhauId(rs.getInt("ho_khau_id"));
                nt.setSoTien(rs.getBigDecimal("so_tien"));
                nt.setNguoiThu(rs.getString("nguoi_thu"));
                nt.setGhiChu(rs.getString("ghi_chu"));

                if (rs.getDate("ngay_nop") != null) {
                    nt.setNgayNop(rs.getDate("ngay_nop").toLocalDate());
                }

                return nt;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}