package services;

import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.KhoanThu;
import models.NopTien;
import models.HoKhau;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * NotificationService – kiểm tra các khoản thu đã quá hạn nộp (han_nop)
 * mà chưa có bất kỳ bản ghi nộp tiền nào, sau đó hiển thị popup cảnh báo
 * cho tổ trưởng / kế toán khi mở màn hình chính.
 *
 * <p>Nghiệp vụ (P5 – Tuần 3):</p>
 * <ul>
 *   <li>Quét toàn bộ khoản thu có {@code han_nop} trước ngày hôm nay</li>
 *   <li>Với mỗi khoản thu đó, liệt kê các hộ khẩu <em>chưa nộp</em></li>
 *   <li>Hiện {@link Alert} tóm tắt số lượng + danh sách hộ chưa nộp</li>
 * </ul>
 */
public class NotificationService {

    private final KhoanThuDAO khoanThuDAO = new KhoanThuDAO();
    private final NopTienDAO  nopTienDAO  = new NopTienDAO();
    private final HoKhauDAO   hoKhauDAO   = new HoKhauDAO();

    // ===================================================================
    //  PUBLIC API
    // ===================================================================

    /**
     * Kiểm tra và hiển thị popup thông báo các hộ chưa nộp quá hạn.
     * Gọi từ màn hình chính ngay sau khi đăng nhập thành công.
     * Không crash dù DB không kết nối được.
     *
     * @param ownerStage Stage cha để popup hiển thị đúng vị trí (có thể null)
     */
    public void checkAndNotify(Stage ownerStage) {
        try {
            List<String> warnings = buildWarningMessages();
            if (!warnings.isEmpty()) {
                showWarningPopup(warnings, ownerStage);
            }
        } catch (Exception e) {
            // Không làm crash app khi không có DB
            System.err.println("[NotificationService] Bỏ qua lỗi khi kiểm tra thông báo: "
                    + e.getMessage());
        }
    }

    // ===================================================================
    //  CORE LOGIC
    // ===================================================================

    /**
     * Xây danh sách cảnh báo: mỗi khoản thu quá hạn kèm số hộ chưa nộp.
     *
     * @return danh sách chuỗi mô tả (rỗng nếu không có gì quá hạn)
     */
    public List<String> buildWarningMessages() {
        List<String> messages = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // 1. Lấy toàn bộ khoản thu
        List<KhoanThu> allKhoanThu = khoanThuDAO.getAllKhoanThu();

        // 2. Lấy toàn bộ bản ghi đã nộp (1 lần để tránh N+1)
        List<NopTien> allNopTien = nopTienDAO.getAllNopTien();

        // 3. Lấy toàn bộ hộ khẩu
        List<HoKhau> allHoKhau = hoKhauDAO.getAllHoKhau();

        for (KhoanThu kt : allKhoanThu) {
            // Bỏ qua nếu chưa đến hạn
            if (kt.getHanNop() == null) continue;
            LocalDate hanNop = kt.getHanNop().toLocalDate();
            if (!hanNop.isBefore(today)) continue;

            // Tìm id các hộ đã nộp cho khoản thu này
            List<Integer> daNopIds = allNopTien.stream()
                    .filter(nt -> nt.getKhoanThuId() == kt.getId())
                    .map(NopTien::getHoKhauId)
                    .distinct()
                    .collect(Collectors.toList());

            // Hộ chưa nộp = tất cả hộ - đã nộp
            List<String> chuaNopMaHo = allHoKhau.stream()
                    .filter(hk -> !daNopIds.contains(hk.getId()))
                    .map(HoKhau::getMaHo)
                    .collect(Collectors.toList());

            if (!chuaNopMaHo.isEmpty()) {
                String msg = String.format(
                        "• Khoản \"%s\" (hạn: %s) – %d hộ chưa nộp: %s",
                        kt.getTenKhoan(),
                        hanNop.toString(),
                        chuaNopMaHo.size(),
                        chuaNopMaHo.size() <= 5
                                ? String.join(", ", chuaNopMaHo)
                                : String.join(", ", chuaNopMaHo.subList(0, 5)) + "..."
                );
                messages.add(msg);
            }
        }

        return messages;
    }

    // ===================================================================
    //  HIỂN THỊ POPUP
    // ===================================================================

    /**
     * Hiện Alert cảnh báo chứa danh sách khoản thu quá hạn.
     *
     * @param warnings   danh sách chuỗi cảnh báo (không rỗng)
     * @param ownerStage Stage chủ (có thể null)
     */
    private void showWarningPopup(List<String> warnings, Stage ownerStage) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Thông báo – Hộ chưa nộp phí quá hạn");
        alert.setHeaderText("⚠  Có " + warnings.size()
                + " khoản thu quá hạn chưa được thu đầy đủ:");

        // Nội dung cuộn được nếu nhiều khoản
        VBox content = new VBox(8);
        content.setPadding(new Insets(8));
        for (String w : warnings) {
            Label lbl = new Label(w);
            lbl.setWrapText(true);
            lbl.setMaxWidth(520);
            content.getChildren().add(lbl);
        }

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(Math.min(warnings.size() * 52.0 + 16, 260));
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        alert.getDialogPane().setContent(scroll);
        alert.getDialogPane().setPrefWidth(580);

        if (ownerStage != null) {
            alert.initOwner(ownerStage);
        }

        // Hiện sau 500ms để màn hình chính render xong trước
        PauseTransition delay = new PauseTransition(Duration.millis(500));
        delay.setOnFinished(e -> alert.showAndWait());
        delay.play();
    }
}
