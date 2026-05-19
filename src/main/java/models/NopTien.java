package models;

import java.math.BigDecimal;
import java.time.LocalDate;

public class NopTien {
    private int id;
    private int khoanThuId;
    private int hoKhauId;
    private BigDecimal soTien;
    private String nguoiThu;
    private String ghiChu;
    private LocalDate ngayNop;

    public NopTien() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getKhoanThuId() { return khoanThuId; }
    public void setKhoanThuId(int khoanThuId) { this.khoanThuId = khoanThuId; }
    public int getHoKhauId() { return hoKhauId; }
    public void setHoKhauId(int hoKhauId) { this.hoKhauId = hoKhauId; }
    public BigDecimal getSoTien() { return soTien; }
    public void setSoTien(BigDecimal soTien) { this.soTien = soTien; }
    public String getNguoiThu() { return nguoiThu; }
    public void setNguoiThu(String nguoiThu) { this.nguoiThu = nguoiThu; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public LocalDate getNgayNop() { return ngayNop; }
    public void setNgayNop(LocalDate ngayNop) { this.ngayNop = ngayNop; }
}
