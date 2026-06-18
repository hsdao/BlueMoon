package application;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;

/**
 * Tiện ích thêm nút con mắt 👁 để hiện/ẩn mật khẩu cho một {@link PasswordField}.
 *
 * <p>Cách hoạt động: chồng một {@link TextField} (hiện mật khẩu rõ) lên trên
 * PasswordField, hai ô dùng chung nội dung (bind 2 chiều). Nút toggle sẽ bật/tắt
 * ô nào hiển thị. Tham chiếu PasswordField gốc trong controller vẫn dùng được
 * (getText/clear hoạt động bình thường nhờ ràng buộc 2 chiều).</p>
 */
public final class PasswordToggle {

    private PasswordToggle() {}

    /** Gắn nút hiện/ẩn cho ô mật khẩu. Gọi trong initialize() của controller. */
    public static void install(PasswordField pf) {
        if (pf == null) return;
        Parent parent = pf.getParent();
        if (!(parent instanceof Pane pane)) return;

        int idx = pane.getChildren().indexOf(pf);
        if (idx < 0) return;
        pane.getChildren().remove(pf);

        // Ô hiển thị mật khẩu dạng chữ rõ, dùng chung nội dung với PasswordField
        TextField clearField = new TextField();
        clearField.textProperty().bindBidirectional(pf.textProperty());
        clearField.setPromptText(pf.getPromptText());
        clearField.getStyleClass().addAll(pf.getStyleClass());
        if (pf.getStyle() != null && !pf.getStyle().isEmpty()) clearField.setStyle(pf.getStyle());
        clearField.setPrefWidth(pf.getPrefWidth());
        clearField.setPrefHeight(pf.getPrefHeight());
        clearField.setVisible(false);
        clearField.setManaged(false);

        StackPane stack = new StackPane(pf, clearField);

        ToggleButton eye = new ToggleButton("👁");
        eye.setFocusTraversable(false);
        eye.setTooltip(new Tooltip("Hiện / ẩn mật khẩu"));
        eye.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 14px; -fx-padding: 0 8 0 8;");
        eye.selectedProperty().addListener((obs, was, showing) -> {
            clearField.setVisible(showing);
            clearField.setManaged(showing);
            pf.setVisible(!showing);
            pf.setManaged(!showing);
            eye.setText(showing ? "🙈" : "👁");
        });

        HBox box = new HBox(4, stack, eye);
        box.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(stack, Priority.ALWAYS);

        pane.getChildren().add(idx, box);
    }
}
