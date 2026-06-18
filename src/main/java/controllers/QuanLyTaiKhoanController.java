package controllers;

import application.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import models.User;
import services.AuditService;
import services.UserDAO;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class QuanLyTaiKhoanController implements Initializable {

    @FXML private TextField     txtTimKiem;
    @FXML private TextField     txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> cmbRole;
    @FXML private Button        btnThem;
    @FXML private Button        btnLamMoi;

    @FXML private TableView<User>            tblUsers;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String>  colUsername;
    @FXML private TableColumn<User, String>  colRole;
    @FXML private TableColumn<User, Void>    colHanhDong;

    private final UserDAO userDAO = new UserDAO();
    private ObservableList<User> masterList;
    private FilteredList<User>   filteredList;
    private final int currentUserId = UserSession.getInstance().getCurrentUser().getId();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbRole.setItems(FXCollections.observableArrayList("ADMIN", "STAFF"));
        setupColumns();
        loadData();
        txtTimKiem.textProperty().addListener((obs, o, n) -> applyFilter(n));
    }

    private void setupColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        colHanhDong.setCellFactory(col -> new TableCell<>() {
            private final Button btnXoa     = new Button("Xóa");
            private final Button btnDoiRole = new Button("Đổi quyền");
            private final HBox   box        = new HBox(8, btnDoiRole, btnXoa);

            {
                box.setAlignment(Pos.CENTER);
                btnXoa.setStyle("-fx-background-color:#82071E;-fx-text-fill:white;"
                        + "-fx-background-radius:4;-fx-cursor:hand;");
                btnDoiRole.setStyle("-fx-background-color:#0969DA;-fx-text-fill:white;"
                        + "-fx-background-radius:4;-fx-cursor:hand;");
                btnXoa.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
                btnDoiRole.setOnAction(e -> handleChangeRole(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                User u = getTableView().getItems().get(getIndex());
                boolean isSelf = u.getId() == currentUserId;
                btnXoa.setDisable(isSelf);
                btnDoiRole.setDisable(isSelf);
                setGraphic(box);
            }
        });
    }

    private void loadData() {
        masterList   = FXCollections.observableArrayList(userDAO.getAllUsers());
        filteredList = new FilteredList<>(masterList, p -> true);
        tblUsers.setItems(filteredList);
    }

    private void applyFilter(String kw) {
        if (kw == null || kw.isBlank()) {
            filteredList.setPredicate(p -> true);
        } else {
            String lower = kw.toLowerCase().trim();
            filteredList.setPredicate(u ->
                u.getUsername().toLowerCase().contains(lower) ||
                u.getRole().toLowerCase().contains(lower)
            );
        }
    }

    @FXML
    private void onThemClick() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();
        String role     = cmbRole.getValue();
        if (username.isEmpty() || password.isEmpty() || role == null) {
            showWarn("Thiếu thông tin", "Vui lòng nhập đầy đủ thông tin!");
            return;
        }
        if (userDAO.addUser(new User(username, password, role))) {
            AuditService.log("THEM", "Tài khoản", "Thêm TK: " + username + " (" + role + ")");
            showInfo("Thành công", "Đã thêm tài khoản: " + username);
            txtUsername.clear();
            txtPassword.clear();
            cmbRole.setValue(null);
            loadData();
        } else {
            showError("Lỗi", "Không thể thêm tài khoản. Tên đăng nhập có thể đã tồn tại.");
        }
    }

    @FXML
    private void onLamMoiClick() {
        txtTimKiem.clear();
        loadData();
    }

    private void handleDelete(User u) {
        if (u.getId() == currentUserId) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc muốn xóa tài khoản \"" + u.getUsername() + "\"?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (userDAO.deleteUser(u.getId())) {
                AuditService.log("XOA", "Tài khoản", "Xóa TK: " + u.getUsername());
                loadData();
                showInfo("Thành công", "Đã xóa tài khoản: " + u.getUsername());
            } else {
                showError("Lỗi", "Không thể xóa tài khoản.");
            }
        }
    }

    private void handleChangeRole(User u) {
        if (u.getId() == currentUserId) return;
        String newRole = "ADMIN".equals(u.getRole()) ? "STAFF" : "ADMIN";
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Đổi quyền");
        confirm.setHeaderText(null);
        confirm.setContentText("Đổi quyền của \"" + u.getUsername()
                + "\" từ " + u.getRole() + " sang " + newRole + "?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            u.setRole(newRole);
            if (userDAO.updateUser(u)) {
                AuditService.log("DOI_QUYEN", "Tài khoản", u.getUsername() + " -> " + newRole);
                loadData();
                showInfo("Thành công", "Đã đổi quyền: " + u.getUsername() + " -> " + newRole);
            } else {
                showError("Lỗi", "Không thể đổi quyền.");
            }
        }
    }

    private void showInfo(String t, String m)  { alert(Alert.AlertType.INFORMATION, t, m); }
    private void showWarn(String t, String m)  { alert(Alert.AlertType.WARNING, t, m); }
    private void showError(String t, String m) { alert(Alert.AlertType.ERROR, t, m); }
    private void alert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}
