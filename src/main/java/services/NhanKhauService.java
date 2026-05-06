package services;

public class NhanKhauService {
    public String validateNhanKhau(
            String hoTen,
            String ngaySinhStr,
            String gioiTinh,
            String soDienThoai,
            String cccd,
            int    hoKhauId
    ) {
        if (isBlank(hoTen))
            return "Họ và tên không được để trống!";
        if (hoTen.trim().length() < 2)
            return "Họ và tên phải có ít nhất 2 ký tự!";
        if (isBlank(gioiTinh))
            return "Vui lòng chọn giới tính!";
        if (isBlank(soDienThoai))
            return "Số điện thoại không được để trống!";
        if (!soDienThoai.trim().matches("^0[0-9]{9,10}$"))
            return "Số điện thoại phải bắt đầu bằng 0 và có 10-11 chữ số!";
        if (hoKhauId <= 0)
            return "Vui lòng chọn hộ khẩu!";
        if (!isBlank(cccd) && !cccd.trim().matches("^[0-9]{9}$|^[0-9]{12}$"))
            return "CCCD phải có 9 hoặc 12 chữ số!";

        return null;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}