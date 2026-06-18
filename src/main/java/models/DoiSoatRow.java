package models;

import java.math.BigDecimal;

/** Một dòng đối soát quỹ: nhóm (người thu / ngày), số giao dịch, tổng tiền. */
public class DoiSoatRow {
    private final String nhom;
    private final int soGiaoDich;
    private final BigDecimal tong;

    public DoiSoatRow(String nhom, int soGiaoDich, BigDecimal tong) {
        this.nhom = nhom;
        this.soGiaoDich = soGiaoDich;
        this.tong = tong == null ? BigDecimal.ZERO : tong;
    }

    public String getNhom() { return nhom; }
    public int getSoGiaoDich() { return soGiaoDich; }
    public BigDecimal getTong() { return tong; }
}
