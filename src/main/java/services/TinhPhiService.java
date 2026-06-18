package services;

import models.HoKhau;
import models.KhoanThu;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Tính số tiền MỘT hộ gia đình cần nộp cho một khoản thu, theo "cách tính" của khoản thu.
 * Hiện thực yêu cầu UC "Tạo khoản thu": hệ thống tự tính số tiền cần nộp cho từng hộ.
 *
 * <ul>
 *   <li>{@code FLAT}         – số tiền cố định (so_tien). Nếu so_tien null -> nhập tay (0).</li>
 *   <li>{@code PER_NHANKHAU} – đơn giá × số thành viên × số tháng (vd phí vệ sinh 6.000đ/người/tháng).</li>
 *   <li>{@code PER_M2}       – đơn giá × diện tích căn hộ × số tháng (phí dịch vụ/quản lý đ/m²/tháng).</li>
 *   <li>{@code PER_XE}       – 70.000đ/xe máy + 1.200.000đ/ô tô, nhân số tháng (phí gửi xe v2.0).</li>
 * </ul>
 */
public class TinhPhiService {

    public static final String FLAT         = "FLAT";
    public static final String PER_NHANKHAU = "PER_NHANKHAU";
    public static final String PER_M2       = "PER_M2";
    public static final String PER_XE       = "PER_XE";

    /** Định mức phí gửi xe theo đề bài (v2.0). */
    private static final BigDecimal PHI_XE_MAY = BigDecimal.valueOf(70_000);
    private static final BigDecimal PHI_O_TO   = BigDecimal.valueOf(1_200_000);

    /** Nhãn tiếng Việt cho ComboBox chọn cách tính. */
    public static String label(String cachTinh) {
        if (cachTinh == null) return "Số tiền cố định";
        switch (cachTinh) {
            case PER_NHANKHAU: return "Theo số nhân khẩu (đ/người/tháng)";
            case PER_M2:       return "Theo diện tích (đ/m²)";
            case PER_XE:       return "Theo số xe (gửi xe)";
            case FLAT:
            default:           return "Số tiền cố định";
        }
    }

    /**
     * Tính số tiền hộ {@code hk} phải nộp cho khoản thu {@code kt}.
     * @return số tiền (>=0); 0 nếu không xác định (vd quỹ tự nguyện không định mức).
     */
    public BigDecimal tinhPhi(KhoanThu kt, HoKhau hk) {
        if (kt == null || hk == null) return BigDecimal.ZERO;

        String cachTinh = (kt.getCachTinh() == null) ? FLAT : kt.getCachTinh();
        BigDecimal donGia = (kt.getSoTien() != null) ? BigDecimal.valueOf(kt.getSoTien()) : BigDecimal.ZERO;
        int soThang = Math.max(1, kt.getSoThang());

        BigDecimal ketQua;
        switch (cachTinh) {
            case PER_NHANKHAU:
                ketQua = donGia
                        .multiply(BigDecimal.valueOf(Math.max(0, hk.getSoThanhVien())))
                        .multiply(BigDecimal.valueOf(soThang));
                break;

            case PER_M2:
                // đơn giá × diện tích × số tháng (đề: phí dịch vụ/quản lý tính theo đ/m²/THÁNG)
                ketQua = donGia
                        .multiply(BigDecimal.valueOf(Math.max(0, hk.getDienTich())))
                        .multiply(BigDecimal.valueOf(soThang));
                break;

            case PER_XE:
                // Đơn giá lấy từ khoản thu nếu có nhập, ngược lại dùng định mức mặc định.
                BigDecimal giaXeMay = (kt.getDonGiaXeMay() != null)
                        ? BigDecimal.valueOf(kt.getDonGiaXeMay()) : PHI_XE_MAY;
                BigDecimal giaOTo = (kt.getDonGiaOTo() != null)
                        ? BigDecimal.valueOf(kt.getDonGiaOTo()) : PHI_O_TO;
                BigDecimal xeMay = giaXeMay.multiply(BigDecimal.valueOf(Math.max(0, hk.getSoXeMay())));
                BigDecimal oTo   = giaOTo.multiply(BigDecimal.valueOf(Math.max(0, hk.getSoOTo())));
                ketQua = xeMay.add(oTo).multiply(BigDecimal.valueOf(soThang));
                break;

            case FLAT:
            default:
                ketQua = donGia; // cố định, hoặc 0 nếu nhập tay (điện/nước thu hộ, quỹ tự nguyện)
                break;
        }
        // Làm tròn NHẤT QUÁN về đồng cho mọi cách tính.
        return ketQua.setScale(0, RoundingMode.HALF_UP);
    }
}
