package models;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ThongKeModel {
    private String maHoKhau;
    private String tenChuHo;
    private BigDecimal soTienPhaiNop;
    private BigDecimal soTienNop;
    private LocalDate ngayNop;
    private String trangThai;
    private String ghiChu;

    public ThongKeModel(String maHoKhau, String tenChuHo, BigDecimal soTienPhaiNop, BigDecimal soTienNop, LocalDate ngayNop, String trangThai, String ghiChu) {
        this.maHoKhau = maHoKhau;
        this.tenChuHo = tenChuHo;
        this.soTienPhaiNop = soTienPhaiNop;
        this.soTienNop = soTienNop;
        this.ngayNop = ngayNop;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
    }

    // --- Getters (Bắt buộc phải có để TableView đọc được dữ liệu) ---
    public String getMaHoKhau() { return maHoKhau; }
    public String getTenChuHo() { return tenChuHo; }
    public BigDecimal getSoTienPhaiNop() { return soTienPhaiNop; }
    public BigDecimal getSoTienNop() { return soTienNop; }
    public LocalDate getNgayNop() { return ngayNop; }
    public String getTrangThai() { return trangThai; }
    public String getGhiChu() { return ghiChu; }
}