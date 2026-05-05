package services;

import models.NopTien;

import java.math.BigDecimal;

/**
 * Service xử lý nghiệp vụ Thu phí.
 */
public class ThuPhiService {

    private final NopTienDAO nopTienDAO = new NopTienDAO();
    private final ThuPhiDAO  thuPhiDAO  = new ThuPhiDAO();

    /**
     * Validate dữ liệu trước khi ghi nhận nộp tiền.
     * @return Thông báo lỗi, hoặc null nếu hợp lệ.
     */
    public String validate(int khoanThuId, int hoKhauId,
                           String soTienStr, String nguoiThu, Object ngayNop) {
        if (khoanThuId <= 0)  return "Vui lòng chọn khoản thu!";
        if (hoKhauId   <= 0)  return "Vui lòng chọn hộ khẩu!";
        if (soTienStr  == null || soTienStr.trim().isEmpty())
            return "Số tiền không được để trống!";
        try {
            BigDecimal st = new BigDecimal(soTienStr.trim().replace(",", ""));
            if (st.compareTo(BigDecimal.ZERO) <= 0) return "Số tiền phải lớn hơn 0!";
        } catch (NumberFormatException e) {
            return "Số tiền phải là số hợp lệ!";
        }
        if (nguoiThu == null || nguoiThu.trim().isEmpty())
            return "Tên người thu không được để trống!";
        if (ngayNop == null) return "Vui lòng chọn ngày nộp!";
        return null;
    }

    /** Kiểm tra hộ đã nộp khoản thu này chưa. */
    public boolean kiemTraDaNop(int khoanThuId, int hoKhauId) {
        return thuPhiDAO.kiemTraDaNop(khoanThuId, hoKhauId);
    }

    /** Ghi nhận nộp tiền vào DB. */
    public boolean ghiNhanNopTien(NopTien nt) {
        return nopTienDAO.themNopTien(nt);
    }
}
