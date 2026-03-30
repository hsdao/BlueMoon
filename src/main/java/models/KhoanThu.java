package models;

import java.math.BigDecimal;
import java.time.LocalDate;

public class KhoanThu {
    private int id;
    private String maKhoan;
    private String tenKhoan;
    private String loai;
    private BigDecimal soTien;
    private LocalDate thangThu;
    private LocalDate hanNop;
    private String moTa;
    private String trangThai;

    public KhoanThu() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getMaKhoan() { return maKhoan; }
    public void setMaKhoan(String maKhoan) { this.maKhoan = maKhoan; }
    public String getTenKhoan() { return tenKhoan; }
    public void setTenKhoan(String tenKhoan) { this.tenKhoan = tenKhoan; }
    public String getLoai() { return loai; }
    public void setLoai(String loai) { this.loai = loai; }
    public BigDecimal getSoTien() { return soTien; }
    public void setSoTien(BigDecimal soTien) { this.soTien = soTien; }
    public LocalDate getThangThu() { return thangThu; }
    public void setThangThu(LocalDate thangThu) { this.thangThu = thangThu; }
    public LocalDate getHanNop() { return hanNop; }
    public void setHanNop(LocalDate hanNop) { this.hanNop = hanNop; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
