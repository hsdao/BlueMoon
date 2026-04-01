package models;

import java.sql.Date;

public class KhoanThu {
    // Các biến này Private để bảo vệ dữ liệu (Tính Đóng Gói trong OOP)
    private int id;
    private String maKhoan;
    private String tenKhoan;
    private String loai;
    private Double soTien; // Dùng Double (chữ D to) để chứa được giá trị NULL
    private Date thangThu;
    private Date hanNop;
    private String moTa;
    private String trangThai;

    // Hàm khởi tạo rỗng (Bắt buộc phải có)
    public KhoanThu() {}

    // --- Các hàm Getter/Setter để lấy và sửa dữ liệu ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMaKhoan() { return maKhoan; }
    public void setMaKhoan(String maKhoan) { this.maKhoan = maKhoan; }

    public String getTenKhoan() { return tenKhoan; }
    public void setTenKhoan(String tenKhoan) { this.tenKhoan = tenKhoan; }

    public String getLoai() { return loai; }
    public void setLoai(String loai) { this.loai = loai; }

    public Double getSoTien() { return soTien; }
    public void setSoTien(Double soTien) { this.soTien = soTien; }

    public Date getThangThu() { return thangThu; }
    public void setThangThu(Date thangThu) { this.thangThu = thangThu; }

    public Date getHanNop() { return hanNop; }
    public void setHanNop(Date hanNop) { this.hanNop = hanNop; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}