package controllers;

import application.Dialogs;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import models.CongNoModel;
import services.CongNoDAO;
import services.ExportPDFService;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;
import java.util.ResourceBundle;

/** Báo cáo công nợ: các hộ chưa nộp các khoản bắt buộc đang mở. */
public class CongNoController implements Initializable {

    @FXML private TextField txtTimKiem;
    @FXML private Label lblTongHop;
    @FXML private TableView<CongNoModel> tblCongNo;
    @FXML private TableColumn<CongNoModel, String>  colMaHo;
    @FXML private TableColumn<CongNoModel, String>  colTenChuHo;
    @FXML private TableColumn<CongNoModel, Integer> colSoKhoan;
    @FXML private TableColumn<CongNoModel, String>  colTongNo;
    @FXML private TableColumn<CongNoModel, String>  colDanhSach;

    private final CongNoDAO dao = new CongNoDAO();
    private final ObservableList<CongNoModel> master = FXCollections.observableArrayList();
    private FilteredList<CongNoModel> filtered;
    private final NumberFormat currency = NumberFormat.getInstance(Locale.of("vi", "VN"));

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colMaHo.setCellValueFactory(new PropertyValueFactory<>("maHo"));
        colTenChuHo.setCellValueFactory(new PropertyValueFactory<>("tenChuHo"));
        colSoKhoan.setCellValueFactory(new PropertyValueFactory<>("soKhoanNo"));
        colDanhSach.setCellValueFactory(new PropertyValueFactory<>("danhSachKhoan"));
        colTongNo.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(
                currency.format(cd.getValue().getTongNo()) + " đ"));

        filtered = new FilteredList<>(master, p -> true);
        tblCongNo.setItems(filtered);
        txtTimKiem.textProperty().addListener((o, a, kw) -> applyFilter(kw));
        loadData();
    }

    private void loadData() {
        new services.HoKhauDAO().recomputeAllSoThanhVien(); // số thành viên mới nhất trước khi tính phí
        master.setAll(dao.tinhCongNo());
        BigDecimal tong = BigDecimal.ZERO;
        for (CongNoModel r : master) tong = tong.add(r.getTongNo());
        lblTongHop.setText("Có " + master.size() + " hộ còn nợ — Tổng công nợ: "
                + currency.format(tong) + " đ");
    }

    private void applyFilter(String kw) {
        if (kw == null || kw.isBlank()) { filtered.setPredicate(p -> true); return; }
        String k = kw.toLowerCase().trim();
        filtered.setPredicate(r ->
                (r.getMaHo() != null && r.getMaHo().toLowerCase().contains(k))
                || (r.getTenChuHo() != null && r.getTenChuHo().toLowerCase().contains(k)));
    }

    @FXML private void onLamMoiClick() { txtTimKiem.clear(); loadData(); }

    @FXML
    private void onXuatPDFClick() {
        if (filtered.isEmpty()) {
            Dialogs.warning("Thông báo", "Không có dữ liệu công nợ để xuất.");
            return;
        }
        FileChooser fc = new FileChooser();
        fc.setTitle("Lưu báo cáo công nợ");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fc.setInitialFileName("Bao_Cao_Cong_No_" + LocalDate.now() + ".pdf");
        File f = fc.showSaveDialog(tblCongNo.getScene().getWindow());
        if (f == null) return;
        try {
            new ExportPDFService().exportCongNo(new java.util.ArrayList<>(filtered), f);
            Dialogs.info("Thành công", "Đã xuất báo cáo:\n" + f.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            Dialogs.error("Lỗi", "Không xuất được PDF: " + e.getMessage());
        }
    }
}
