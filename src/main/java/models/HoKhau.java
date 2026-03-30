package models;

import java.time.LocalDate;

public class HoKhau {
    private int id;
    private String maHo;
    private int chuHoId;
    private String soDienThoaiChuHo;
    private String diaChi;
    private int soThanhVien;
    private LocalDate ngayTao;
    private String trangThai;
    private String ghiChu;

    public HoKhau() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getMaHo() { return maHo; }
    public void setMaHo(String maHo) { this.maHo = maHo; }
    public int getChuHoId() { return chuHoId; }
    public void setChuHoId(int chuHoId) { this.chuHoId = chuHoId; }
    public String getSoDienThoaiChuHo() { return soDienThoaiChuHo; }
    public void setSoDienThoaiChuHo(String soDienThoaiChuHo) { this.soDienThoaiChuHo = soDienThoaiChuHo; }
    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
    public int getSoThanhVien() { return soThanhVien; }
    public void setSoThanhVien(int soThanhVien) { this.soThanhVien = soThanhVien; }
    public LocalDate getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDate ngayTao) { this.ngayTao = ngayTao; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
