package models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Kiểm thử chuẩn hoá trạng thái nhân khẩu {@link NhanKhauStatus#label}. */
class NhanKhauStatusTest {

    @Test
    @DisplayName("Mã chuẩn -> nhãn tiếng Việt")
    void canonicalLabels() {
        assertEquals("Thường trú",   NhanKhauStatus.label(NhanKhauStatus.PERMANENT));
        assertEquals("Tạm trú",      NhanKhauStatus.label(NhanKhauStatus.TEMPORARY));
        assertEquals("Đã chuyển đi", NhanKhauStatus.label(NhanKhauStatus.MOVED_OUT));
        assertEquals("Đã mất",       NhanKhauStatus.label(NhanKhauStatus.DECEASED));
    }

    @Test
    @DisplayName("Tương thích mã cũ ACTIVE/MOVED")
    void legacyCodes() {
        assertEquals("Thường trú",   NhanKhauStatus.label("ACTIVE"));
        assertEquals("Đã chuyển đi", NhanKhauStatus.label("MOVED"));
    }

    @Test
    @DisplayName("null -> rỗng; mã lạ -> giữ nguyên")
    void edgeCases() {
        assertEquals("", NhanKhauStatus.label(null));
        assertEquals("XYZ", NhanKhauStatus.label("XYZ"));
    }
}
