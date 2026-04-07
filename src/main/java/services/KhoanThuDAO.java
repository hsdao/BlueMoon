package services;

import models.KhoanThu;
import services.db.MysqlConnection; // Gọi đường ống kết nối
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class KhoanThuDAO {

    // 1. Hàm lấy tất cả khoản thu mang lên giao diện
    public List<KhoanThu> getAllKhoanThu() {
        List<KhoanThu> danhSach = new ArrayList<>();
        String sql = "SELECT * FROM khoan_thu";

        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                KhoanThu kt = new KhoanThu(); // Tạo 1 cái hộp trống

                // Nhét dữ liệu từ MySQL vào hộp
                kt.setId(rs.getInt("id"));
                kt.setMaKhoan(rs.getString("ma_khoan"));
                kt.setTenKhoan(rs.getString("ten_khoan"));
                kt.setLoai(rs.getString("loai"));

                Object soTienObj = rs.getObject("so_tien");
                kt.setSoTien(soTienObj != null ? ((Number) soTienObj).doubleValue() : null);

                kt.setThangThu(rs.getDate("thang_thu"));
                kt.setHanNop(rs.getDate("han_nop"));
                kt.setMoTa(rs.getString("mo_ta"));
                kt.setTrangThai(rs.getString("trang_thai"));

                danhSach.add(kt); // Bỏ hộp vào danh sách
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return danhSach;
    }

    // 2. Hàm Thêm khoản thu mới
    public boolean themKhoanThu(KhoanThu kt) {
        String sql = "INSERT INTO khoan_thu(ma_khoan, ten_khoan, loai, so_tien, thang_thu, han_nop, mo_ta, trang_thai) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, kt.getMaKhoan());
            pstmt.setString(2, kt.getTenKhoan());
            pstmt.setString(3, kt.getLoai());

            if (kt.getSoTien() != null) pstmt.setDouble(4, kt.getSoTien());
            else pstmt.setNull(4, Types.DECIMAL);

            pstmt.setDate(5, kt.getThangThu());
            pstmt.setDate(6, kt.getHanNop());
            pstmt.setString(7, kt.getMoTa());
            pstmt.setString(8, kt.getTrangThai());

            return pstmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 3. Hàm UPDATE (Sửa thông tin khoản thu)
    public boolean capNhatKhoanThu(KhoanThu kt) {
        // Lệnh UPDATE dùng để sửa, dựa vào id để biết sửa hàng nào
        String sql = "UPDATE khoan_thu SET ma_khoan=?, ten_khoan=?, loai=?, so_tien=?, thang_thu=?, han_nop=?, mo_ta=?, trang_thai=? WHERE id=?";

        try (Connection conn = services.db.MysqlConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, kt.getMaKhoan());
            pstmt.setString(2, kt.getTenKhoan());
            pstmt.setString(3, kt.getLoai());

            if (kt.getSoTien() != null) pstmt.setDouble(4, kt.getSoTien());
            else pstmt.setNull(4, Types.DECIMAL);

            pstmt.setDate(5, kt.getThangThu());
            pstmt.setDate(6, kt.getHanNop());
            pstmt.setString(7, kt.getMoTa());
            pstmt.setString(8, kt.getTrangThai());

            // Tham số thứ 9 chính là id nằm ở cuối câu lệnh SQL
            pstmt.setInt(9, kt.getId());

            return pstmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 4. Hàm DELETE (Xóa khoản thu)
    public boolean xoaKhoanThu(int id) {
        // Xóa cực kỳ cẩn thận, bắt buộc phải có WHERE id = ?
        String sql = "DELETE FROM khoan_thu WHERE id = ?";
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