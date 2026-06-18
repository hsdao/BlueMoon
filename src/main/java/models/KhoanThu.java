package models;

import java.sql.Date;

public class KhoanThu {
    // Các biến này Private để bảo vệ dữ liệu (Tính Đóng Gói trong OOP)
    private int id;
    private String maKhoan;
    private String tenKhoan;
    private String loai;
    private Double soTien; // Dùng Double (chữ D to) để chứa được giá trị NULL
    private String cachTinh;   // FLAT / PER_NHANKHAU / PER_M2 / PER_XE (xem TinhPhiService)
    private int soThang = 1;   // số tháng thu (mặc định 1), dùng cho phí theo tháng
    private Double donGiaXeMay; // đơn giá/xe máy/tháng (chỉ dùng khi PER_XE; null = mặc định 70.000)
    private Double donGiaOTo;   // đơn giá/ô tô/tháng  (chỉ dùng khi PER_XE; null = mặc định 1.200.000)
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

    public String getCachTinh() { return cachTinh; }
    public void setCachTinh(String cachTinh) { this.cachTinh = cachTinh; }

    public int getSoThang() { return soThang; }
    public void setSoThang(int soThang) { this.soThang = soThang; }

    public Double getDonGiaXeMay() { return donGiaXeMay; }
    public void setDonGiaXeMay(Double donGiaXeMay) { this.donGiaXeMay = donGiaXeMay; }

    public Double getDonGiaOTo() { return donGiaOTo; }
    public void setDonGiaOTo(Double donGiaOTo) { this.donGiaOTo = donGiaOTo; }

    public Date getThangThu() { return thangThu; }
    public void setThangThu(Date thangThu) { this.thangThu = thangThu; }

    public Date getHanNop() { return hanNop; }
    public void setHanNop(Date hanNop) { this.hanNop = hanNop; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    // --- Nhãn tiếng Việt cho hiển thị trên bảng (không ảnh hưởng dữ liệu DB) ---
    public String getLoaiLabel()      { return Labels.khoanThuLoai(loai); }
    public String getTrangThaiLabel() { return Labels.khoanThuTrangThai(trangThai); }
}
