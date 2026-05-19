package services;

import models.KhoanThu;

public class KhoanThuService {
    
    /**
     * Xác thực dữ liệu đầu vào cho Khoản thu mới hoặc cập nhật
     * Trả về thông báo lỗi, hoặc null/rỗng nếu hợp lệ.
     */
    public String validateKhoanThu(String maKhoan, String tenKhoan, String loai, String soTienStr) {
        if (maKhoan == null || maKhoan.trim().isEmpty()) {
            return "Mã khoản thu không được để trống!";
        }
        
        if (tenKhoan == null || tenKhoan.trim().isEmpty()) {
            return "Tên khoản thu không được để trống!";
        }
        
        if (loai == null || loai.trim().isEmpty()) {
            return "Vui lòng chọn loại khoản thu!";
        }
        
        // Bắt buộc (chẳng hạn) thì phải có số tiền
        if ("Bắt buộc".equals(loai)) {
            if (soTienStr == null || soTienStr.trim().isEmpty()) {
                return "Khoản thu Bắt buộc phải nhập số tiền!";
            }
        }
        
        if (soTienStr != null && !soTienStr.trim().isEmpty()) {
            try {
                double tien = Double.parseDouble(soTienStr);
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
