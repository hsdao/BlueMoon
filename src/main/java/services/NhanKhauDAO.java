package services;

import models.NhanKhau;
import services.db.MysqlConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class NhanKhauDAO {

    // ================== CRUD ==================

    public List<NhanKhau> getAll() {
        List<NhanKhau> list = new ArrayList<>();
        String sql = "SELECT * FROM nhan_khau ORDER BY id DESC";

        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Lỗi getAll: " + e.getMessage());
        }

        return list;
    }

    public NhanKhau getById(int id) {
        String sql = "SELECT * FROM nhan_khau WHERE id=?";

        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("Lỗi getById: " + e.getMessage());
        }

        return null;
    }

    public boolean insert(NhanKhau n) {
        try (Connection conn = MysqlConnection.getConnection()) {
            return insertWithConnection(conn, n);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ⭐ dùng cho transaction
    public boolean insertWithConnection(Connection conn, NhanKhau n) {
        if (!isValid(n)) return false;

        String sql = "INSERT INTO nhan_khau " +
                "(ho_khau_id, ho_ten, ngay_sinh, gioi_tinh, cccd, dan_toc, ton_giao, " +
                "nghe_nghiep, noi_lam_viec, que_quan, dia_chi_thuong_tru, quan_he_id, so_dien_thoai, trang_thai) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setPreparedStatement(ps, n, false);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        n.setId(rs.getInt(1));
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Lỗi insert: " + e.getMessage());
        }

        return false;
    }

    public boolean update(NhanKhau n) {
        if (!isValid(n)) return false;

        String sql = "UPDATE nhan_khau SET " +
                "ho_khau_id=?, ho_ten=?, ngay_sinh=?, gioi_tinh=?, cccd=?, dan_toc=?, ton_giao=?, " +
                "nghe_nghiep=?, noi_lam_viec=?, que_quan=?, dia_chi_thuong_tru=?, quan_he_id=?, so_dien_thoai=?, trang_thai=? " +
                "WHERE id=?";

        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            setPreparedStatement(ps, n, true);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi update: " + e.getMessage());
        }

        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM nhan_khau WHERE id=?";

        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi delete: " + e.getMessage());
        }

        return false;
    }

    // ================== SEARCH ==================

    public List<NhanKhau> findByHoKhau(int hoKhauId) {
        List<NhanKhau> list = new ArrayList<>();
        String sql = "SELECT * FROM nhan_khau WHERE ho_khau_id=?";

        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, hoKhauId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Lỗi findByHoKhau: " + e.getMessage());
        }

        return list;
    }

    public List<NhanKhau> searchByName(String keyword) {
        List<NhanKhau> list = new ArrayList<>();
        String sql = "SELECT * FROM nhan_khau WHERE ho_ten LIKE ?";

        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Lỗi searchByName: " + e.getMessage());
        }

        return list;
    }

    // ================== SPECIAL ==================

    // chuyển hộ khẩu
    public boolean updateHoKhau(int nhanKhauId, int hoKhauMoi) {
        String sql = "UPDATE nhan_khau SET ho_khau_id=? WHERE id=?";

        try (Connection conn = MysqlConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, hoKhauMoi);
            ps.setInt(2, nhanKhauId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi updateHoKhau: " + e.getMessage());
        }

        return false;
    }

    // ================== HELPER ==================

    private NhanKhau mapResultSet(ResultSet rs) throws SQLException {
        NhanKhau n = new NhanKhau();

        n.setId(rs.getInt("id"));
        n.setHoKhauId(rs.getInt("ho_khau_id"));
        n.setHoTen(rs.getString("ho_ten"));

        Date ns = rs.getDate("ngay_sinh");
        if (ns != null) n.setNgaySinh(ns.toLocalDate());

        n.setGioiTinh(rs.getString("gioi_tinh"));
        n.setCccd(rs.getString("cccd"));
        n.setDanToc(rs.getString("dan_toc"));
        n.setTonGiao(rs.getString("ton_giao"));
        n.setNgheNghiep(rs.getString("nghe_nghiep"));
        n.setNoiLamViec(rs.getString("noi_lam_viec"));
        n.setQueQuan(rs.getString("que_quan"));
        n.setDiaChiThuongTru(rs.getString("dia_chi_thuong_tru"));
        n.setQuanHeId(rs.getInt("quan_he_id"));
        n.setSoDienThoai(rs.getString("so_dien_thoai"));
        n.setTrangThai(rs.getString("trang_thai"));

        return n;
    }

    private void setPreparedStatement(PreparedStatement ps, NhanKhau n, boolean isUpdate) throws SQLException {
        ps.setInt(1, n.getHoKhauId());
        ps.setString(2, n.getHoTen());

        if (n.getNgaySinh() != null) {
            ps.setDate(3, Date.valueOf(n.getNgaySinh()));
        } else {
            ps.setNull(3, Types.DATE);
        }

        ps.setString(4, n.getGioiTinh());
        ps.setString(5, n.getCccd());
        ps.setString(6, n.getDanToc());
        ps.setString(7, n.getTonGiao());
        ps.setString(8, n.getNgheNghiep());
        ps.setString(9, n.getNoiLamViec());
        ps.setString(10, n.getQueQuan());
        ps.setString(11, n.getDiaChiThuongTru());
        ps.setInt(12, n.getQuanHeId());
        ps.setString(13, n.getSoDienThoai());
        ps.setString(14, n.getTrangThai());

        if (isUpdate) {
            ps.setInt(15, n.getId());
        }
    }

    private boolean isValid(NhanKhau n) {
        if (n == null) return false;
        if (n.getHoTen() == null || n.getHoTen().isEmpty()) return false;
        if (n.getHoKhauId() <= 0) return false;
        return true;
    }
}