package models;

import java.sql.Timestamp;

/** Một dòng nhật ký thao tác (ai làm gì, khi nào). */
public class AuditLog {
    private int id;
    private String username;
    private String hanhDong;   // THEM | SUA | XOA | DANG_NHAP | THU_PHI ...
    private String doiTuong;   // "Hộ khẩu", "Nhân khẩu", "Khoản thu" ...
    private String moTa;
    private Timestamp thoiGian;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getHanhDong() { return hanhDong; }
    public void setHanhDong(String hanhDong) { this.hanhDong = hanhDong; }
    public String getDoiTuong() { return doiTuong; }
    public void setDoiTuong(String doiTuong) { this.doiTuong = doiTuong; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public Timestamp getThoiGian() { return thoiGian; }
    public void setThoiGian(Timestamp thoiGian) { this.thoiGian = thoiGian; }
}
