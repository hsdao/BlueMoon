package models;

/**
 * Bộ giá trị CHUẨN cho cột {@code nhan_khau.trang_thai}.
 *
 * <p>Trước đây trạng thái bị ghi bằng 2 bộ từ vựng khác nhau:
 * form dùng {@code PERMANENT/TEMPORARY/MOVED_OUT}, còn màn hình Lịch sử biến động
 * lại ghi {@code ACTIVE/MOVED/DECEASED}. Lớp này gom về MỘT bộ mã thống nhất và
 * cung cấp nhãn tiếng Việt để hiển thị.</p>
 */
public final class NhanKhauStatus {

    public static final String PERMANENT = "PERMANENT"; // Thường trú
    public static final String TEMPORARY = "TEMPORARY"; // Tạm trú
    public static final String MOVED_OUT = "MOVED_OUT"; // Đã chuyển đi
    public static final String DECEASED  = "DECEASED";  // Đã mất

    private NhanKhauStatus() {}

    /**
     * Đổi mã trạng thái sang nhãn tiếng Việt để hiển thị.
     * Chấp nhận cả mã cũ ({@code ACTIVE}, {@code MOVED}) để không vỡ dữ liệu sẵn có.
     */
    public static String label(String code) {
        if (code == null) return "";
        switch (code) {
            case PERMANENT: return "Thường trú";
            case TEMPORARY: return "Tạm trú";
            case MOVED_OUT: return "Đã chuyển đi";
            case DECEASED:  return "Đã mất";
            // Tương thích ngược dữ liệu cũ
            case "ACTIVE":  return "Thường trú";
            case "MOVED":   return "Đã chuyển đi";
            default:        return code; // giá trị lạ -> hiện nguyên văn
        }
    }
}
