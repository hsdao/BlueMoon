package services;

import models.KhoanThu;
import models.Labels;

public class KhoanThuService {

    /**
     * Xác thực dữ liệu đầu vào cho Khoản thu mới hoặc cập nhật.
     * Tham số {@code loai} dùng MÃ chuẩn (BAT_BUOC / TU_NGUYEN).
     * Trả về thông báo lỗi, hoặc null/rỗng nếu hợp lệ.
     */
    public String validateKhoanThu(String maKhoan, String tenKhoan, String loai, String soTienStr) {
        // Giữ tương thích: cách tính null -> áp dụng quy tắc cũ (bắt buộc cần số tiền)
        return validateKhoanThu(maKhoan, tenKhoan, loai, soTienStr, null);
    }

    /**
     * Bản đầy đủ có thêm {@code cachTinh}: khoản tính THEO XE (PER_XE) không cần nhập
     * "số tiền/đơn giá" ở ô chung (đã có ô đơn giá xe máy/ô tô riêng).
     */
    public String validateKhoanThu(String maKhoan, String tenKhoan, String loai,
                                   String soTienStr, String cachTinh) {
        if (maKhoan == null || maKhoan.trim().isEmpty()) {
            return "Mã khoản thu không được để trống!";
        }

        if (tenKhoan == null || tenKhoan.trim().isEmpty()) {
            return "Tên khoản thu không được để trống!";
        }

        if (loai == null || loai.trim().isEmpty()) {
            return "Vui lòng chọn loại khoản thu!";
        }

        boolean trong = (soTienStr == null || soTienStr.trim().isEmpty());
        if (cachTinh == null) {
            // Tương thích bản 4 tham số: BẮT BUỘC thì cần số tiền
            if (Labels.LOAI_BAT_BUOC.equals(loai) && trong) {
                return "Khoản thu Bắt buộc phải nhập số tiền / đơn giá!";
            }
        } else {
            // Chỉ cách tính TỰ ĐỘNG theo m²/nhân khẩu mới bắt buộc nhập ĐƠN GIÁ.
            // FLAT (vd thu hộ điện/nước - chốt khi thu) và PER_XE (đơn giá xe riêng) được để trống.
            boolean canDonGia = "PER_M2".equals(cachTinh) || "PER_NHANKHAU".equals(cachTinh);
            if (canDonGia && trong) {
                return "Cách tính theo m² / theo nhân khẩu cần nhập ĐƠN GIÁ!";
            }
        }

        if (soTienStr != null && !soTienStr.trim().isEmpty()) {
            try {
                // Cho phép nhập dấu phẩy ngăn cách nghìn (vd 1,000,000) — nhất quán với form.
                double tien = Double.parseDouble(soTienStr.trim().replace(",", ""));
                if (tien < 0) {
                    return "Số tiền không được âm!";
                }
            } catch (NumberFormatException e) {
                return "Số tiền phải là số hợp lệ!";
            }
        }
        
        return null; // Hợp lệ
    }
}
