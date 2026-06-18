package services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Kiểm thử băm/đối chiếu mật khẩu {@link PasswordUtil}
 * (liên quan chức năng Đăng nhập – Chương 9).
 */
class PasswordUtilTest {

    @Test
    @DisplayName("Băm rồi đối chiếu đúng mật khẩu -> true")
    void hashAndVerify() {
        String stored = PasswordUtil.hash("MatKhau@123");
        assertTrue(PasswordUtil.verify(stored, "MatKhau@123"));
    }

    @Test
    @DisplayName("Sai mật khẩu -> false")
    void wrongPassword() {
        String stored = PasswordUtil.hash("MatKhau@123");
        assertFalse(PasswordUtil.verify(stored, "sai-mat-khau"));
    }

    @Test
    @DisplayName("Không lưu mật khẩu thô (chuỗi băm khác mật khẩu gốc)")
    void notPlaintext() {
        String stored = PasswordUtil.hash("abc123");
        assertNotEquals("abc123", stored);
        assertTrue(PasswordUtil.isHashed(stored));
    }

    @Test
    @DisplayName("Tương thích ngược: dữ liệu cũ chưa băm vẫn đối chiếu được")
    void legacyPlaintext() {
        assertTrue(PasswordUtil.verify("oldplain", "oldplain"));
        assertFalse(PasswordUtil.verify("oldplain", "khac"));
    }

    @Test
    @DisplayName("Đầu vào null không gây lỗi")
    void nullSafe() {
        assertFalse(PasswordUtil.verify(null, "x"));
        assertFalse(PasswordUtil.verify("x", null));
    }
}
