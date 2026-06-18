package services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Kiểm thử hộp đen validate Hộ khẩu {@link HoKhauService#validateHoKhau}. */
class HoKhauServiceTest {

    private final HoKhauService service = new HoKhauService();

    @Test
    @DisplayName("Hộ khẩu hợp lệ")
    void valid() {
        assertNull(service.validateHoKhau("HK001", "0981001001", "Phòng 101-A", "5"));
    }

    @Test
    @DisplayName("Chưa chọn phòng (mã hộ trống)")
    void invalidCode() {
        assertEquals("Vui lòng chọn phòng cho hộ khẩu!",
                service.validateHoKhau("", "0981001001", "Phòng 101", "5"));
    }

    @Test
    @DisplayName("Số điện thoại sai định dạng")
    void invalidPhone() {
        assertEquals("Số điện thoại phải bắt đầu bằng 0 và có 10-11 chữ số!",
                service.validateHoKhau("HK001", "123", "Phòng 101", "5"));
    }

    @Test
    @DisplayName("Địa chỉ để trống")
    void emptyAddress() {
        assertEquals("Địa chỉ không được để trống!",
                service.validateHoKhau("HK001", "0981001001", "", "5"));
    }

    @Test
    @DisplayName("Số thành viên âm")
    void negativeMembers() {
        assertEquals("Số thành viên không được âm!",
                service.validateHoKhau("HK001", "0981001001", "Phòng 101", "-1"));
    }
}
