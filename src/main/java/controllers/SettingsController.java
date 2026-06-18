package controllers;

import atlantafx.base.theme.CupertinoLight;
import atlantafx.base.theme.NordLight;
import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * SettingsController – Màn hình cài đặt Theme AtlantaFX.
 * Tuần 4 (P5): Cho phép chọn theme PrimerLight / NordDark / CupertinoDark.
 * Lưu preference vào file settings.properties để load lại khi mở app.
 */
public class SettingsController implements Initializable {

    @FXML private ComboBox<String> cmbTheme;
    @FXML private Label            lblStatus;

    /** Tên các theme hiển thị trong ComboBox */
    private static final String[] THEME_NAMES = {
            "PrimerLight (Mặc định)",
            "NordLight (Nord sáng)",
            "CupertinoLight (macOS sáng)"
    };

    /** Đường dẫn file lưu preference */
    private static final String PREFS_FILE = "settings.properties";
    private static final String KEY_THEME  = "theme";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbTheme.setItems(FXCollections.observableArrayList(THEME_NAMES));
        // Load theme đang dùng từ file
        String saved = loadThemePreference();
        for (int i = 0; i < THEME_NAMES.length; i++) {
            if (THEME_NAMES[i].startsWith(saved)) {
                cmbTheme.getSelectionModel().select(i);
                break;
            }
        }
        if (cmbTheme.getSelectionModel().isEmpty()) {
            cmbTheme.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void handleApply() {
        String selected = cmbTheme.getValue();
        if (selected == null) return;

        String stylesheet;
        String themeKey;

        if (selected.startsWith("NordLight")) {
            stylesheet = new NordLight().getUserAgentStylesheet();
            themeKey   = "NordLight";
        } else if (selected.startsWith("CupertinoLight")) {
            stylesheet = new CupertinoLight().getUserAgentStylesheet();
            themeKey   = "CupertinoLight";
        } else {
            stylesheet = new PrimerLight().getUserAgentStylesheet();
            themeKey   = "PrimerLight";
        }

        // Áp dụng theme ngay lập tức
        Application.setUserAgentStylesheet(stylesheet);

        // Lưu vào file preference
        saveThemePreference(themeKey);

        lblStatus.setText("✅  Đã áp dụng theme: " + selected.split(" ")[0]);
        lblStatus.setStyle("-fx-text-fill: #2da44e;");
    }

    // ===================================================================
    //  PERSISTENCE
    // ===================================================================

    /**
     * Lưu tên theme vào settings.properties trong thư mục chạy app.
     */
    private void saveThemePreference(String themeName) {
        try {
            Properties props = new Properties();
            props.setProperty(KEY_THEME, themeName);
            try (FileOutputStream fos = new FileOutputStream(PREFS_FILE)) {
                props.store(fos, "BlueMoon User Preferences");
            }
        } catch (Exception e) {
            System.err.println("[Settings] Không thể lưu preference: " + e.getMessage());
        }
    }

    /**
     * Đọc tên theme đã lưu. Trả về "PrimerLight" nếu chưa có file.
     */
    public static String loadThemePreference() {
        File f = new File(PREFS_FILE);
        if (!f.exists()) return "PrimerLight";
        try (FileInputStream fis = new FileInputStream(f)) {
            Properties props = new Properties();
            props.load(fis);
            return props.getProperty(KEY_THEME, "PrimerLight");
        } catch (Exception e) {
            return "PrimerLight";
        }
    }

    /**
     * Áp dụng theme đã lưu lúc khởi động app.
     * Gọi từ Main.java trước khi show Stage.
     */
    public static void applyStoredTheme() {
        String theme = loadThemePreference();
        String stylesheet = switch (theme) {
            case "NordLight"      -> new NordLight().getUserAgentStylesheet();
            case "CupertinoLight" -> new CupertinoLight().getUserAgentStylesheet();
            default               -> new PrimerLight().getUserAgentStylesheet();
        };
        Application.setUserAgentStylesheet(stylesheet);
    }
}
