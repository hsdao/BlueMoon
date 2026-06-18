package services;

import application.UserSession;
import models.User;

/**
 * Tiện ích ghi nhật ký thao tác. Tự lấy tài khoản đang đăng nhập.
 * Dùng: {@code AuditService.log("THEM", "Hộ khẩu", "Thêm hộ P1205");}
 */
public final class AuditService {

    private static final AuditDAO dao = new AuditDAO();

    private AuditService() {}

    public static void log(String hanhDong, String doiTuong, String moTa) {
        String username = "system";
        try {
            User u = UserSession.getInstance().getCurrentUser();
            if (u != null) username = u.getUsername();
        } catch (Exception ignore) { /* khi chưa đăng nhập */ }
        dao.ghi(username, hanhDong, doiTuong, moTa);
    }
}
