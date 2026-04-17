package controllers;

import models.KhoanThu;
import services.KhoanThuDAO;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller cho màn hình Danh sách Khoản Thu (KhoanThu.fxml).
 * Nhiệm vụ (P3):
 *  - Hiển thị toàn bộ khoản thu trong TableView
 *  - Tìm kiếm theo mã / tên khoản thu
 *  - Mở form Thêm mới khoản thu
 *  - Mở form Sửa khoản thu đang được chọn
 *  - Xóa khoản thu với xác nhận từ người dùng
 */
public class KhoanThuController implements Initializable {

    // --- Các thành phần giao diện (map từ fx:id trong KhoanThu.fxml) ---
    @FXML private TextField txtTimKiem;
    @FXML private TableView<KhoanThu> tblKhoanThu;
    @FXML private Button btnThemMoi;
    @FXML private Button btnSua;
    @FXML private Button btnXoa;

    // --- Dữ liệu & DAO ---
    private ObservableList<KhoanThu> khoanThuList;
    private final KhoanThuDAO dao = new KhoanThuDAO();

    /**
     * Được gọi tự động ngay khi FXML load xong.
     * Thứ tự: tải dữ liệu → gán bảng → gắn tìm kiếm.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadDataFromDB();
        setupSearch();
    }

    // =====================================================================
    //  DỮ LIỆU
    // =====================================================================

    /**
     * Tải toàn bộ khoản thu từ DB rồi đặt vào TableView.
     * Được gọi lại sau mỗi thao tác Thêm / Sửa / Xóa để làm mới danh sách.
     */
    public void loadDataFromDB() {
        List<KhoanThu> dbList = dao.getAllKhoanThu();
        khoanThuList = FXCollections.observableArrayList(dbList);
        tblKhoanThu.setItems(khoanThuList);
        // Khi reload lại dữ liệu, gắn lại filter để ô tìm kiếm vẫn còn hoạt động
        setupSearch();
    }

    // =====================================================================
    //  TÌM KIẾM
    // =====================================================================

    /**
     * Gắn listener vào ô tìm kiếm.
     * Lọc theo mã khoản hoặc tên khoản (không phân biệt chữ hoa/thường).
     */
    private void setupSearch() {
        if (khoanThuList == null) return;

        FilteredList<KhoanThu> filteredData = new FilteredList<>(khoanThuList, p -> true);

        txtTimKiem.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(kt -> {
                // Nếu ô tìm kiếm rỗng → hiện tất cả
                if (newValue == null || newValue.isBlank()) {
                    return true;
                }
                String keyword = newValue.toLowerCase().trim();

                // Tìm theo Mã khoản
                if (kt.getMaKhoan() != null && kt.getMaKhoan().toLowerCase().contains(keyword)) {
                    return true;
                }
                // Tìm theo Tên khoản thu
                if (kt.getTenKhoan() != null && kt.getTenKhoan().toLowerCase().contains(keyword)) {
                    return true;
                }
                return false;
            });
        });

        tblKhoanThu.setItems(filteredData);
    }

    // =====================================================================
    //  XỬ LÝ SỰ KIỆN BUTTON (map từ onAction trong FXML)
    // =====================================================================

    /**
     * Nút "Thêm Khoản Thu" → mở FormKhoanThu ở chế độ thêm mới.
     */
    @FXML
    private void onThemMoiClick() {
        openFormDialog(null);
    }

    /**
     * Nút "Sửa" → lấy khoản thu đang được chọn rồi mở form ở chế độ sửa.
     * Nếu chưa chọn hàng nào, hiện thông báo nhắc nhở.
     */
    @FXML
    private void onSuaClick() {
        KhoanThu selected = tblKhoanThu.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Chưa chọn khoản thu", "Vui lòng chọn một khoản thu trong bảng để sửa.");
            return;
        }
        openFormDialog(selected);
    }

    /**
     * Nút "Xóa" → xác nhận rồi xóa khoản thu đang được chọn.
     * Nếu chưa chọn hàng nào, hiện thông báo nhắc nhở.
     */
    @FXML
    private void onXoaClick() {
        KhoanThu selected = tblKhoanThu.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Chưa chọn khoản thu", "Vui lòng chọn một khoản thu trong bảng để xóa.");
            return;
        }

        // Hỏi xác nhận trước khi xóa
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText(null);
        confirm.setContentText(
            "Bạn có chắc muốn xóa khoản thu \"" + selected.getTenKhoan() + "\" không?\n"
            + "Hành động này không thể hoàn tác."
        );

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = dao.xoaKhoanThu(selected.getId());
            if (success) {
                loadDataFromDB();
                showInfo("Thành công", "Đã xóa khoản thu \"" + selected.getTenKhoan() + "\".");
            } else {
                showError("Lỗi", "Không thể xóa. Khoản thu này có thể đã có người nộp tiền.");
            }
        }
    }

    // =====================================================================
    //  MỞ FORM THÊM / SỬA
    // =====================================================================

    /**
     * Mở cửa sổ FormKhoanThu.fxml dạng modal.
     *
     * @param khoanThu null → chế độ Thêm mới; không null → chế độ Sửa
     */
    private void openFormDialog(KhoanThu khoanThu) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/FormKhoanThu.fxml"));
            Parent root = loader.load();

            // Lấy controller của form và truyền dữ liệu vào
            FormKhoanThuController formController = loader.getController();
            formController.setParentController(this);   // để form gọi lại loadDataFromDB()

            if (khoanThu != null) {
                formController.setEditData(khoanThu);   // chế độ sửa
            } else {
                formController.setAddMode();             // chế độ thêm mới
            }

            Stage stage = new Stage();
            stage.setTitle(khoanThu == null ? "Thêm Khoản Thu Mới" : "Sửa Khoản Thu");
            stage.initModality(Modality.APPLICATION_MODAL); // chặn cửa sổ cha khi form đang mở
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi hệ thống", "Không thể mở form khoản thu:\n" + e.getMessage());
        }
    }

    // =====================================================================
    //  HELPER – HIỂN THỊ THÔNG BÁO
    // =====================================================================

    private void showWarning(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
