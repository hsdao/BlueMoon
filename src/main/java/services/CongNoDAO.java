package services;

import models.CongNoModel;
import models.HoKhau;
import models.KhoanThu;
import services.db.MysqlConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Tính công nợ: các hộ CHƯA nộp những khoản BẮT BUỘC đang mở. */
public class CongNoDAO {

    private final TinhPhiService tinhPhiService = new TinhPhiService();

    public List<CongNoModel> tinhCongNo() {
        // Mỗi dòng = 1 cặp (hộ, khoản thu) bắt buộc đang mở mà hộ CHƯA nộp
        String sql =
            "SELECT hk.id, hk.ma_ho, hk.so_thanh_vien, hk.dien_tich, hk.so_xe_may, hk.so_o_to, " +
            "       nk.ho_ten AS ten_chu_ho, " +
            "       kt.ten_khoan, kt.cach_tinh, kt.so_tien AS kt_so_tien, kt.so_thang, " +
            "       kt.don_gia_xe_may, kt.don_gia_o_to " +
            "FROM ho_khau hk " +
            "JOIN khoan_thu kt ON kt.trang_thai='OPEN' AND kt.loai='BAT_BUOC' " +
            "LEFT JOIN nhan_khau nk ON hk.chu_ho_id = nk.id " +
            "LEFT JOIN nop_tien nt ON nt.ho_khau_id = hk.id AND nt.khoan_thu_id = kt.id " +
            "WHERE nt.id IS NULL AND hk.trang_thai='ACTIVE' " +
            "ORDER BY hk.ma_ho";

        Map<Integer, CongNoModel> map = new LinkedHashMap<>();
        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int hoId = rs.getInt("id");
                CongNoModel row = map.computeIfAbsent(hoId, k -> {
                    String ten = rs2safe(rs, "ten_chu_ho");
                    return new CongNoModel(rs2safe(rs, "ma_ho"), ten == null ? "—" : ten);
                });

                HoKhau hk = new HoKhau();
                hk.setSoThanhVien(rs.getInt("so_thanh_vien"));
                hk.setDienTich(rs.getDouble("dien_tich"));
                hk.setSoXeMay(rs.getInt("so_xe_may"));
                hk.setSoOTo(rs.getInt("so_o_to"));
                KhoanThu kt = new KhoanThu();
                kt.setCachTinh(rs.getString("cach_tinh"));
                Object st = rs.getObject("kt_so_tien");
                kt.setSoTien(st != null ? ((Number) st).doubleValue() : null);
                kt.setSoThang(rs.getInt("so_thang"));
                Object dgXe = rs.getObject("don_gia_xe_may");
                Object dgOto = rs.getObject("don_gia_o_to");
                kt.setDonGiaXeMay(dgXe != null ? ((Number) dgXe).doubleValue() : null);
                kt.setDonGiaOTo(dgOto != null ? ((Number) dgOto).doubleValue() : null);
                BigDecimal tien = tinhPhiService.tinhPhi(kt, hk);

                row.cong(rs.getString("ten_khoan"), tien);
            }
        } catch (Exception e) {
            System.err.println("Lỗi CongNoDAO: " + e.getMessage());
        }
        return new ArrayList<>(map.values());
    }

    private String rs2safe(ResultSet rs, String col) {
        try { return rs.getString(col); } catch (Exception e) { return null; }
    }
}
