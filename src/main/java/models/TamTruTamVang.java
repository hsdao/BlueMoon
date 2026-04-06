package models;

import java.time.LocalDate;

public class TamTruTamVang {
    private int id;
    private int nhanKhauId;
    private String loai;
    private LocalDate tuNgay;
    private LocalDate denNgay;
    private String diaChiTamTru;
    private String lyDo;
    private String trangThai;

    public TamTruTamVang() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getNhanKhauId() { return nhanKhauId; }
    public void setNhanKhauId(int nhanKhauId) { this.nhanKhauId = nhanKhauId; }
    public String getLoai() { return loai; }
    public void setLoai(String loai) { this.loai = loai; }
    public LocalDate getTuNgay() { return tuNgay; }
    public void setTuNgay(LocalDate tuNgay) { this.tuNgay = tuNgay; }
    public LocalDate getDenNgay() { return denNgay; }
    public void setDenNgay(LocalDate denNgay) { this.denNgay = denNgay; }
    public String getDiaChiTamTru() { return diaChiTamTru; }
    public void setDiaChiTamTru(String diaChiTamTru) { this.diaChiTamTru = diaChiTamTru; }
    public String getLyDo() { return lyDo; }
    public void setLyDo(String lyDo) { this.lyDo = lyDo; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
