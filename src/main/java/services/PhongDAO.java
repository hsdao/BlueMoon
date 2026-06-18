package services;

import models.Phong;
import services.db.MysqlConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/** Truy cập danh mục phòng/căn hộ (dm_phong). */
public class PhongDAO {

    private static final String ORDER =
            " ORDER BY CAST(REGEXP_REPLACE(ma_phong,'[^0-9]','') AS UNSIGNED), ma_phong";

    /** Toàn bộ phòng (cả trống lẫn đã có hộ), sắp theo số phòng. */
    public List<Phong> getAll() {
        return query("SELECT id, ma_phong, tang, dien_tich FROM dm_phong" + ORDER);
    }

    /** Các phòng CÒN TRỐNG (chưa gắn với hộ khẩu nào) — dùng khi thêm hộ khẩu. */
    public List<Phong> getTrong() {
        return query("SELECT p.id, p.ma_phong, p.tang, p.dien_tich FROM dm_phong p "
                + "LEFT JOIN ho_khau h ON h.ma_ho = p.ma_phong "
                + "WHERE h.id IS NULL" + ORDER);
    }

    private List<Phong> query(String sql) {
        List<Phong> list = new ArrayList<>();
        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Phong(rs.getInt("id"), rs.getString("ma_phong"),
                        rs.getInt("tang"), rs.getDouble("dien_tich")));
            }
        } catch (Exception e) {
            System.err.println("Lỗi đọc dm_phong: " + e.getMessage());
        }
        return list;
    }
}
