package models;

import java.time.LocalDate;

public class NhanKhau {
    private int id;
    private int hoKhauId;
    private String hoTen;
    private LocalDate ngaySinh;
    private String gioiTinh;
    private String cccd;
    private String danToc;
    private String tonGiao;
    private String ngheNghiep;
    private String noiLamViec;
    private String queQuan;
    private String diaChiThuongTru;
    private int quanHeId;
    private String soDienThoai;
    private String trangThai;

    public NhanKhau() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getHoKhauId() { return hoKhauId; }
    public void setHoKhauId(int hoKhauId) { this.hoKhauId = hoKhauId; }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public LocalDate getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(LocalDate ngaySinh) { this.ngaySinh = ngaySinh; }
    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }
    public String getCccd() { return cccd; }
    public void setCccd(String cccd) { this.cccd = cccd; }
    public String getDanToc() { return danToc; }
    public void setDanToc(String danToc) { this.danToc = danToc; }
    public String getTonGiao() { return tonGiao; }
    public void setTonGiao(String tonGiao) { this.tonGiao = tonGiao; }
    public String getNgheNghiep() { return ngheNghiep; }
    public void setNgheNghiep(String ngheNghiep) { this.ngheNghiep = ngheNghiep; }
    public String getNoiLamViec() { return noiLamViec; }
    public void setNoiLamViec(String noiLamViec) { this.noiLamViec = noiLamViec; }
    public String getQueQuan() { return queQuan; }
    public void setQueQuan(String queQuan) { this.queQuan = queQuan; }
    public String getDiaChiThuongTru() { return diaChiThuongTru; }
    public void setDiaChiThuongTru(String diaChiThuongTru) { this.diaChiThuongTru = diaChiThuongTru; }
    public int getQuanHeId() { return quanHeId; }
    public void setQuanHeId(int quanHeId) { this.quanHeId = quanHeId; }
    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
