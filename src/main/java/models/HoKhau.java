package models;

import java.sql.Timestamp;

public class HoKhau {
    private int id;
    private String maHo;
    private Integer chuHoId; // Dùng Integer vì lúc mới tạo hộ khẩu có thể chưa có chủ hộ (NULL)
    private String soDienThoaiChuHo;
    private int soThanhVien;
    private String diaChi;
    private Timestamp ngayTao; // Dùng Timestamp cho ngày giờ
    private String trangThai;
    private String ghiChu;

    public HoKhau() {}

    // --- Getter và Setter ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMaHo() { return maHo; }
    public void setMaHo(String maHo) { this.maHo = maHo; }

    public Integer getChuHoId() { return chuHoId; }
    public void setChuHoId(Integer chuHoId) { this.chuHoId = chuHoId; }

    public String getSoDienThoaiChuHo() { return soDienThoaiChuHo; }
    public void setSoDienThoaiChuHo(String soDienThoaiChuHo) { this.soDienThoaiChuHo = soDienThoaiChuHo; }

    public int getSoThanhVien() { return soThanhVien; }
    public void setSoThanhVien(int soThanhVien) { this.soThanhVien = soThanhVien; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public Timestamp getNgayTao() { return ngayTao; }
    public void setNgayTao(Timestamp ngayTao) { this.ngayTao = ngayTao; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
