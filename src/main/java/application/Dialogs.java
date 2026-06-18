package application;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * Tiện ích hiển thị hộp thoại dùng chung cho toàn bộ controller.
 *
 * <p>Trước đây mỗi controller tự dựng {@link Alert} (setTitle/setHeaderText/
 * setContentText/showAndWait) gây trùng lặp mã (code smell "Duplicate Code").
 * Gom về một nơi để dễ bảo trì và đồng nhất giao diện thông báo.</p>
 */
public final class Dialogs {

    private Dialogs() {}

    public static void info(String title, String content)    { show(AlertType.INFORMATION, title, content); }
    public static void warning(String title, String content) { show(AlertType.WARNING, title, content); }
    public static void error(String title, String content)   { show(AlertType.ERROR, title, content); }

    public static void show(AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }
}
