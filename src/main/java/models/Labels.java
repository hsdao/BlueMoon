package models;

/**
 * Chuyển các mã lưu trong DB sang nhãn tiếng Việt để hiển thị.
 * (Mã vẫn lưu nguyên trong DB; chỉ đổi khi hiện ra giao diện.)
 */
public final class Labels {

    private Labels() {}

    // ----- Mã CHUẨN lưu trong DB (nguồn chân lý duy nhất) -----
    public static final String LOAI_BAT_BUOC  = "BAT_BUOC";
    public static final String LOAI_TU_NGUYEN = "TU_NGUYEN";
    public static final String KT_OPEN   = "OPEN";
    public static final String KT_CLOSED = "CLOSED";

    /** Loại khoản thu: BAT_BUOC / TU_NGUYEN. */
    public static String khoanThuLoai(String code) {
        if (code == null) return "";
        switch (code) {
            case LOAI_BAT_BUOC:  return "Bắt buộc";
            case LOAI_TU_NGUYEN: return "Tự nguyện";
            default:             return code; // đã là nhãn hoặc giá trị lạ
        }
    }

    /**
     * Đổi NHÃN tiếng Việt (từ ComboBox) sang MÃ để lưu DB.
     * Chấp nhận sẵn mã hợp lệ để gọi nhiều lần không hỏng (idempotent).
     */
    public static String khoanThuLoaiCode(String label) {
        if (label == null) return null;
        switch (label.trim()) {
            case "Bắt buộc":
            case LOAI_BAT_BUOC:  return LOAI_BAT_BUOC;
            case "Tự nguyện":
            case LOAI_TU_NGUYEN: return LOAI_TU_NGUYEN;
            default:             return label.trim();
        }
    }

    /** Trạng thái khoản thu: OPEN / CLOSED. */
    public static String khoanThuTrangThai(String code) {
        if (code == null) return "";
        switch (code) {
            case KT_OPEN:   return "Đang mở";
            case KT_CLOSED: return "Đã đóng";
            default:        return code;
        }
    }

    /** Đổi NHÃN trạng thái khoản thu (ComboBox) sang MÃ để lưu DB. Mặc định OPEN. */
    public static String khoanThuTrangThaiCode(String label) {
        if (label == null) return KT_OPEN;
        switch (label.trim()) {
            case "Đã đóng":
            case KT_CLOSED: return KT_CLOSED;
            default:        return KT_OPEN;   // "Đang mở" hoặc giá trị khác
        }
    }

    /** Trạng thái hộ khẩu: ACTIVE / INACTIVE. */
    public static String hoKhauTrangThai(String code) {
        if (code == null) return "";
        switch (code) {
            case "ACTIVE":   return "Đang hoạt động";
            case "INACTIVE": return "Ngừng hoạt động";
            default:         return code;
        }
    }
}
