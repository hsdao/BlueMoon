package models;

import java.math.BigDecimal;

/** Một dòng báo cáo công nợ: một hộ còn nợ bao nhiêu khoản, tổng tiền nợ. */
public class CongNoModel {
    private String maHo;
    private String tenChuHo;
    private int soKhoanNo;
    private BigDecimal tongNo;
    private String danhSachKhoan; // tên các khoản còn nợ

    public CongNoModel(String maHo, String tenChuHo) {
        this.maHo = maHo;
        this.tenChuHo = tenChuHo;
        this.soKhoanNo = 0;
        this.tongNo = BigDecimal.ZERO;
        this.danhSachKhoan = "";
    }

    public void cong(String tenKhoan, BigDecimal tien) {
        this.soKhoanNo++;
        this.tongNo = this.tongNo.add(tien == null ? BigDecimal.ZERO : tien);
        this.danhSachKhoan = this.danhSachKhoan.isEmpty() ? tenKhoan : this.danhSachKhoan + ", " + tenKhoan;
    }

    public String getMaHo() { return maHo; }
    public String getTenChuHo() { return tenChuHo; }
    public int getSoKhoanNo() { return soKhoanNo; }
    public BigDecimal getTongNo() { return tongNo; }
    public String getDanhSachKhoan() { return danhSachKhoan; }
}
