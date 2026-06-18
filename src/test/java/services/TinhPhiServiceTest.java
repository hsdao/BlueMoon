package services;

import models.HoKhau;
import models.KhoanThu;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Kiểm thử tính tiền phải nộp theo định mức {@link TinhPhiService#tinhPhi}
 * (cốt lõi UC "Tạo khoản thu" – hệ thống tự tính tiền cho từng hộ).
 */
class TinhPhiServiceTest {

    private final TinhPhiService service = new TinhPhiService();

    private HoKhau hoKhauMau() {
        HoKhau hk = new HoKhau();
        hk.setSoThanhVien(7);
        hk.setDienTich(85.5);
        hk.setSoXeMay(2);
        hk.setSoOTo(1);
        return hk;
    }

    private KhoanThu khoanThu(String cachTinh, Double donGia, int soThang) {
        KhoanThu kt = new KhoanThu();
        kt.setCachTinh(cachTinh);
        kt.setSoTien(donGia);
        kt.setSoThang(soThang);
        return kt;
    }

    @Test
    @DisplayName("Phí theo nhân khẩu: 6.000 x 7 x 1 = 42.000")
    void perNhanKhau() {
        KhoanThu kt = khoanThu(TinhPhiService.PER_NHANKHAU, 6000.0, 1);
        assertEquals(42000L, service.tinhPhi(kt, hoKhauMau()).longValue());
    }

    @Test
    @DisplayName("Phí theo diện tích: 7.000 x 85,5 = 598.500")
    void perM2() {
        KhoanThu kt = khoanThu(TinhPhiService.PER_M2, 7000.0, 1);
        assertEquals(598500L, service.tinhPhi(kt, hoKhauMau()).longValue());
    }

    @Test
    @DisplayName("Phí theo diện tích NHIỀU THÁNG: 7.000 x 85,5 x 3 = 1.795.500")
    void perM2NhieuThang() {
        KhoanThu kt = khoanThu(TinhPhiService.PER_M2, 7000.0, 3);
        assertEquals(1795500L, service.tinhPhi(kt, hoKhauMau()).longValue());
    }

    @Test
    @DisplayName("Phí vệ sinh CẢ NĂM (đề: thu 1 lần/năm): 6.000 x 7 x 12 = 504.000")
    void perNhanKhauCaNam() {
        KhoanThu kt = khoanThu(TinhPhiService.PER_NHANKHAU, 6000.0, 12);
        assertEquals(504000L, service.tinhPhi(kt, hoKhauMau()).longValue());
    }

    @Test
    @DisplayName("Phí gửi xe: 2x70.000 + 1x1.200.000 = 1.340.000")
    void perXe() {
        KhoanThu kt = khoanThu(TinhPhiService.PER_XE, null, 1);
        assertEquals(1340000L, service.tinhPhi(kt, hoKhauMau()).longValue());
    }

    @Test
    @DisplayName("Phí cố định: trả về đúng số tiền")
    void flat() {
        KhoanThu kt = khoanThu(TinhPhiService.FLAT, 50000.0, 1);
        assertEquals(50000L, service.tinhPhi(kt, hoKhauMau()).longValue());
    }

    @Test
    @DisplayName("Phí cố định không có số tiền -> 0")
    void flatNull() {
        KhoanThu kt = khoanThu(TinhPhiService.FLAT, null, 1);
        assertEquals(0L, service.tinhPhi(kt, hoKhauMau()).longValue());
    }
}
