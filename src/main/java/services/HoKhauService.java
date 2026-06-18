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
        // Mã hộ / phòng (lấy từ danh mục phòng nên không ràng buộc định dạng cứng)
        if (isBlank(maHo)) {
            return "Vui lòng chọn phòng cho hộ khẩu!";
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