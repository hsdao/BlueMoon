package services;

import models.NopTien;
import services.db.MysqlConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO bổ sung cho nghiệp vụ Thu phí.
 * Chứa các query nâng cao không có trong NopTienDAO gốc.
 */
public class ThuPhiDAO {

    private NopTien mapRow(ResultSet rs) throws SQLException {
        NopTien nt = new NopTien();
        nt.setId(rs.getInt("id"));
        nt.setKhoanThuId(rs.getInt("khoan_thu_id"));
        nt.setHoKhauId(rs.getInt("ho_khau_id"));
        nt.setSoTien(rs.getBigDecimal("so_tien"));
        nt.setNguoiThu(rs.getString("nguoi_thu"));
        nt.setGhiChu(rs.getString("ghi_chu"));
        Date d = rs.getDate("ngay_nop");
        if (d != null) nt.setNgayNop(d.toLocalDate());
        return nt;
    }

    /** Lấy danh sách nộp tiền theo khoản thu, sắp xếp mới nhất trước. */
    public List<NopTien> getByKhoanThu(int khoanThuId) {
        List<NopTien> list = new ArrayList<>();
        String sql = "SELECT id,khoan_thu_id,ho_khau_id,so_tien,nguoi_thu,ghi_chu,ngay_nop "
                   + "FROM nop_tien WHERE khoan_thu_id=? ORDER BY ngay_nop DESC";
        try (Connection c = MysqlConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, khoanThuId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy nộp tiền theo khoản thu: " + e.getMessage());
        }
        return list;
    }

    /** Kiểm tra hộ đã nộp khoản thu này chưa (check trùng). */
    public boolean kiemTraDaNop(int khoanThuId, int hoKhauId) {
        String sql = "SELECT COUNT(*) FROM nop_tien WHERE khoan_thu_id=? AND ho_khau_id=?";
        try (Connection c = MysqlConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, khoanThuId);
            ps.setInt(2, hoKhauId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            // Không xác nhận được -> coi như chưa nộp; ràng buộc UNIQUE ở DB vẫn chặn trùng khi insert.
            System.err.println("Lỗi kiểm tra trùng nộp tiền: " + e.getMessage());
            return false;
        }
    }
}
