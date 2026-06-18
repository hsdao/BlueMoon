package services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Kiểm thử chức năng "Nộp tiền cho khoản thu" (Chương 9) – kiểm thử hộp đen
 * cho {@link ThuPhiService#validate}.
 */
class ThuPhiServiceTest {

    private final ThuPhiService service = new ThuPhiService();

    @Test
    @DisplayName("Dữ liệu hợp lệ -> không lỗi")
    void valid() {
        assertNull(service.validate(1, 1, "100000", "admin", LocalDate.now()));
    }

    @Test
    @DisplayName("Chưa chọn khoản thu")
    void noKhoanThu() {
        assertEquals("Vui lòng chọn khoản thu!",
                service.validate(0, 1, "1000", "admin", LocalDate.now()));
    }

    @Test
    @DisplayName("Chưa chọn hộ khẩu")
    void noHoKhau() {
        assertEquals("Vui lòng chọn hộ khẩu!",
                service.validate(1, 0, "1000", "admin", LocalDate.now()));
    }

    @Test
    @DisplayName("Số tiền để trống")
    void emptyAmount() {
        assertEquals("Số tiền không được để trống!",
                service.validate(1, 1, "", "admin", LocalDate.now()));
    }

    @Test
    @DisplayName("Số tiền bằng 0")
    void zeroAmount() {
        assertEquals("Số tiền phải lớn hơn 0!",
                service.validate(1, 1, "0", "admin", LocalDate.now()));
    }

    @Test
    @DisplayName("Số tiền không hợp lệ")
    void invalidAmount() {
        assertEquals("Số tiền phải là số hợp lệ!",
                service.validate(1, 1, "abc", "admin", LocalDate.now()));
    }

    @Test
    @DisplayName("Thiếu người thu")
    void missingCollector() {
        assertEquals("Tên người thu không được để trống!",
                service.validate(1, 1, "1000", "", LocalDate.now()));
    }

    @Test
    @DisplayName("Thiếu ngày nộp")
    void missingDate() {
        assertEquals("Vui lòng chọn ngày nộp!",
                service.validate(1, 1, "1000", "admin", null));
    }
}
