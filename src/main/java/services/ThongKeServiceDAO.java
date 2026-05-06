package services;

import models.KhoanThu;
import models.ThongKeModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ThongKeServiceDAO {

    // 1. Lấy danh sách các khoản thu để nạp vào ComboBox
    public List<KhoanThu> getAllKhoanThu() {
        List<KhoanThu> list = new ArrayList<>();
        String sql = "SELECT id, ten_khoan, so_tien FROM khoan_thu";

        // GIẢ ĐỊNH: Gọi class kết nối DB của nhóm(nằm ở package services.db) để lấy Connection
        try (Connection conn = services.db.MysqlConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                KhoanThu kt = new KhoanThu();
                kt.setId(rs.getInt("id"));
                kt.setTenKhoan(rs.getString("ten_khoan"));
                kt.setSoTien(rs.getDouble("so_tien"));
                list.add(kt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 2. Lấy dữ liệu thống kê theo khoản thu (Dùng LEFT JOIN)
    public List<ThongKeModel> getThongKeByKhoanThu(int khoanThuId) {
        List<ThongKeModel> list = new ArrayList<>();
        String sql = "SELECT hk.ma_ho, nk.ho_ten AS ten_chu_ho, nt.so_tien, nt.ngay_nop, nt.ghi_chu " +
                "FROM ho_khau hk " +
                "LEFT JOIN nhan_khau nk ON hk.chu_ho_id = nk.id " +
                "LEFT JOIN nop_tien nt ON hk.id = nt.ho_khau_id AND nt.khoan_thu_id = ?";

        try (Connection conn = services.db.MysqlConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, khoanThuId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String maHo = rs.getString("ma_ho");
                String tenChuHo = rs.getString("ten_chu_ho");
                BigDecimal soTien = rs.getBigDecimal("so_tien");
                java.sql.Date sqlDate = rs.getDate("ngay_nop");
                LocalDate ngayNop = (sqlDate != null) ? sqlDate.toLocalDate() : null;
                String ghiChu = rs.getString("ghi_chu");

                // Logic xử lý trạng thái
                String trangThai = (soTien != null) ? "Đã nộp" : "Chưa nộp";

                // Nếu tên chủ hộ null (hộ chưa có ai), set mặc định
                if (tenChuHo == null) tenChuHo = "Chưa cập nhật";
                if (ghiChu == null) ghiChu = "";

                list.add(new ThongKeModel(maHo, tenChuHo, soTien, ngayNop, trangThai, ghiChu));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}