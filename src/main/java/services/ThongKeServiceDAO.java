package services;

import models.HoKhau;
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
            System.err.println("Lỗi ThongKeServiceDAO: " + e.getMessage());
        }
        return list;
    }

    // 2. Lấy dữ liệu thống kê theo khoản thu (Dùng LEFT JOIN)
    // FIX: ResultSet được đặt trong try-with-resources để tránh rò rỉ tài nguyên
    public List<ThongKeModel> getThongKeByKhoanThu(int khoanThuId) {
        List<ThongKeModel> list = new ArrayList<>();
        // Lấy thông tin hộ + khoản thu (để TỰ TÍNH số phải nộp) + bản ghi nộp (nếu có)
        String sql =
                "SELECT hk.ma_ho, nk.ho_ten AS ten_chu_ho, " +
                "       hk.so_thanh_vien, hk.dien_tich, hk.so_xe_may, hk.so_o_to, " +
                "       kt.cach_tinh, kt.so_tien AS kt_so_tien, kt.so_thang, kt.don_gia_xe_may, kt.don_gia_o_to, " +
                "       nt.so_tien AS nt_so_tien, nt.ngay_nop, nt.ghi_chu " +
                "FROM ho_khau hk " +
                "JOIN khoan_thu kt ON kt.id = ? " +
                "LEFT JOIN nhan_khau nk ON hk.chu_ho_id = nk.id " +
                "LEFT JOIN nop_tien nt ON hk.id = nt.ho_khau_id AND nt.khoan_thu_id = kt.id " +
                "WHERE hk.trang_thai = 'ACTIVE' " +
                "ORDER BY CAST(REGEXP_REPLACE(hk.ma_ho, '[^0-9]', '') AS UNSIGNED), hk.ma_ho";

        TinhPhiService tinhPhi = new TinhPhiService();

        try (Connection conn = services.db.MysqlConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, khoanThuId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String maHo = rs.getString("ma_ho");
                    String tenChuHo = rs.getString("ten_chu_ho");
                    BigDecimal soTien = rs.getBigDecimal("nt_so_tien");
                    java.sql.Date sqlDate = rs.getDate("ngay_nop");
                    LocalDate ngayNop = (sqlDate != null) ? sqlDate.toLocalDate() : null;
                    String ghiChu = rs.getString("ghi_chu");

                    // Tự tính SỐ PHẢI NỘP theo cách tính của khoản thu cho hộ này
                    HoKhau hk = new HoKhau();
                    hk.setSoThanhVien(rs.getInt("so_thanh_vien"));
                    hk.setDienTich(rs.getDouble("dien_tich"));
                    hk.setSoXeMay(rs.getInt("so_xe_may"));
                    hk.setSoOTo(rs.getInt("so_o_to"));
                    KhoanThu kt = new KhoanThu();
                    kt.setCachTinh(rs.getString("cach_tinh"));
                    Object kts = rs.getObject("kt_so_tien");
                    kt.setSoTien(kts != null ? ((Number) kts).doubleValue() : null);
                    kt.setSoThang(rs.getInt("so_thang"));
                    Object dgXe = rs.getObject("don_gia_xe_may");
                    Object dgOto = rs.getObject("don_gia_o_to");
                    kt.setDonGiaXeMay(dgXe != null ? ((Number) dgXe).doubleValue() : null);
                    kt.setDonGiaOTo(dgOto != null ? ((Number) dgOto).doubleValue() : null);
                    BigDecimal phaiNop = tinhPhi.tinhPhi(kt, hk);

                    String trangThai = (soTien != null) ? "Đã nộp" : "Chưa nộp";
                    if (tenChuHo == null) tenChuHo = "Chưa cập nhật";
                    if (ghiChu == null) ghiChu = "";

                    list.add(new ThongKeModel(maHo, tenChuHo, phaiNop, soTien, ngayNop, trangThai, ghiChu));
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi ThongKeServiceDAO: " + e.getMessage());
        }
        return list;
    }
}