package models;

import java.time.LocalDate;

public class LichSuBienDong {
    private int id;
    private int nhanKhauId;
    private Integer hoKhauId;
    private String loaiBienDong;
    private LocalDate ngayBienDong;
    private String ghiChu;
    private String nguoiThucHien;

    public LichSuBienDong() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getNhanKhauId() { return nhanKhauId; }
    public void setNhanKhauId(int nhanKhauId) { this.nhanKhauId = nhanKhauId; }

    public Integer getHoKhauId() { return hoKhauId; }
    public void setHoKhauId(Integer hoKhauId) { this.hoKhauId = hoKhauId; }

    public String getLoaiBienDong() { return loaiBienDong; }
    public void setLoaiBienDong(String loaiBienDong) { this.loaiBienDong = loaiBienDong; }

    public LocalDate getNgayBienDong() { return ngayBienDong; }
    public void setNgayBienDong(LocalDate ngayBienDong) { this.ngayBienDong = ngayBienDong; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public String getNguoiThucHien() { return nguoiThucHien; }
    public void setNguoiThucHien(String nguoiThucHien) { this.nguoiThucHien = nguoiThucHien; }
}
