package services;

public class HoKhauService {
     // Xác thực đầu vào khi thêm mới hoặc cập nhật hộ khẩu.
     // @return null / rỗng nếu hợp lệ; thông báo lỗi nếu không hợp lệ.
    public String validateHoKhau(
            String maHo,
            String soDienThoai,
            String diaChi,
            String soThanhVienStr
    ) {
        // Mã hộ
        if (isBlank(maHo)) {
            return "Mã hộ khẩu không được để trống!";
        }
        if (!maHo.trim().matches("^HK[0-9]{3,}$")) {
            return "Mã hộ khẩu phải theo định dạng HKxxx (VD: HK001)!";
        }

        // Số điện thoại
        if (isBlank(soDienThoai)) {
            return "Số điện thoại chủ hộ không được để trống!";
        }
        if (!soDienThoai.trim().matches("^0[0-9]{9,10}$")) {
            return "Số điện thoại phải bắt đầu bằng 0 và có 10-11 chữ số!";
        }

        // Địa chỉ
        if (isBlank(diaChi)) {
            return "Địa chỉ không được để trống!";
        }

        // Số thành viên
        if (isBlank(soThanhVienStr)) {
            return "Số thành viên không được để trống!";
        }
        try {
            int soTv = Integer.parseInt(soThanhVienStr.trim());
            if (soTv < 0) return "Số thành viên không được âm!";
        } catch (NumberFormatException e) {
            return "Số thành viên phải là số nguyên!";
        }

        return null; // Hợp lệ
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}