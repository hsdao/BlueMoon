package services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Kiểm thử hộp đen validate Nhân khẩu {@link NhanKhauService#validateNhanKhau}. */
class NhanKhauServiceTest {

    private final NhanKhauService service = new NhanKhauService();

    @Test
    @DisplayName("Nhân khẩu hợp lệ")
    void valid() {
        assertNull(service.validateNhanKhau(
                "Nguyễn Văn A", null, "Nam", "0981001001", "001080123456", 1));
    }

    @Test
    @DisplayName("Họ tên quá ngắn")
    void nameTooShort() {
        assertEquals("Họ và tên phải có ít nhất 2 ký tự!",
                service.validateNhanKhau("A", null, "Nam", "0981001001", null, 1));
    }

    @Test
    @DisplayName("CCCD sai định dạng (không phải 9/12 số)")
    void invalidCccd() {
        assertEquals("CCCD phải có 9 hoặc 12 chữ số!",
                service.validateNhanKhau("Nguyễn Văn A", null, "Nam", "0981001001", "123", 1));
    }

    @Test
    @DisplayName("Chưa chọn hộ khẩu")
    void noHoKhau() {
        assertEquals("Vui lòng chọn hộ khẩu!",
                service.validateNhanKhau("Nguyễn Văn A", null, "Nam", "0981001001", null, 0));
    }

    @Test
    @DisplayName("Số điện thoại sai định dạng")
    void invalidPhone() {
        assertEquals("Số điện thoại phải bắt đầu bằng 0 và có 10-11 chữ số!",
                service.validateNhanKhau("Nguyễn Văn A", null, "Nam", "abc", null, 1));
    }
}
