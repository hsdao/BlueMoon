package services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Kiểm thử chức năng "Tạo khoản thu" (Chương 9) – kiểm thử hộp đen theo bảng
 * quyết định cho {@link KhoanThuService#validateKhoanThu}.
 */
class KhoanThuServiceTest {

    private final KhoanThuService service = new KhoanThuService();

    @Test
    @DisplayName("Dữ liệu hợp lệ -> không lỗi")
    void valid() {
        assertNull(service.validateKhoanThu("KT01", "Phí quản lý", "BAT_BUOC", "1000"));
    }

    @Test
    @DisplayName("Khoản tự nguyện để trống số tiền vẫn hợp lệ")
    void emptyAmountAllowedForVoluntary() {
        assertNull(service.validateKhoanThu("KT01", "Quỹ từ thiện", "TU_NGUYEN", ""));
    }

    @Test
    @DisplayName("Thiếu mã khoản thu")
    void missingCode() {
        assertEquals("Mã khoản thu không được để trống!",
                service.validateKhoanThu("", "Phí", "BAT_BUOC", "1000"));
    }

    @Test
    @DisplayName("Thiếu tên khoản thu")
    void missingName() {
        assertEquals("Tên khoản thu không được để trống!",
                service.validateKhoanThu("KT01", "", "BAT_BUOC", "1000"));
    }

    @Test
    @DisplayName("Thiếu loại khoản thu")
    void missingType() {
        assertEquals("Vui lòng chọn loại khoản thu!",
                service.validateKhoanThu("KT01", "Phí", null, "1000"));
    }

    @Test
    @DisplayName("Khoản bắt buộc để trống số tiền -> báo lỗi")
    void mandatoryNeedsAmount() {
        assertEquals("Khoản thu Bắt buộc phải nhập số tiền / đơn giá!",
                service.validateKhoanThu("KT01", "Phí", "BAT_BUOC", ""));
    }

    @Test
    @DisplayName("Số tiền âm")
    void negativeAmount() {
        assertEquals("Số tiền không được âm!",
                service.validateKhoanThu("KT01", "Phí", "BAT_BUOC", "-5"));
    }

    @Test
    @DisplayName("Số tiền không phải số")
    void invalidAmount() {
        assertEquals("Số tiền phải là số hợp lệ!",
                service.validateKhoanThu("KT01", "Phí", "BAT_BUOC", "abc"));
    }
}
