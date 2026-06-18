package controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import models.DoiSoatRow;
import services.NopTienDAO;

import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/** Đối soát quỹ: tổng thu theo người thu và theo ngày — giúp khớp tiền mặt thực thu. */
public class DoiSoatQuyController implements Initializable {

    @FXML private Label lblTong;
    @FXML private TableView<DoiSoatRow> tblNguoiThu;
    @FXML private TableColumn<DoiSoatRow, String>  colNgNhom;
    @FXML private TableColumn<DoiSoatRow, Integer> colNgSL;
    @FXML private TableColumn<DoiSoatRow, String>  colNgTong;
    @FXML private TableView<DoiSoatRow> tblNgay;
    @FXML private TableColumn<DoiSoatRow, String>  colNgayNhom;
    @FXML private TableColumn<DoiSoatRow, Integer> colNgaySL;
    @FXML private TableColumn<DoiSoatRow, String>  colNgayTong;

    private final NopTienDAO dao = new NopTienDAO();
    private final NumberFormat currency = NumberFormat.getInstance(Locale.of("vi", "VN"));

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colNgNhom.setCellValueFactory(new PropertyValueFactory<>("nhom"));
        colNgSL.setCellValueFactory(new PropertyValueFactory<>("soGiaoDich"));
        colNgTong.setCellValueFactory(c -> new SimpleStringProperty(currency.format(c.getValue().getTong())));

        colNgayNhom.setCellValueFactory(new PropertyValueFactory<>("nhom"));
        colNgaySL.setCellValueFactory(new PropertyValueFactory<>("soGiaoDich"));
        colNgayTong.setCellValueFactory(c -> new SimpleStringProperty(currency.format(c.getValue().getTong())));

        loadData();
    }

    @FXML
    private void onRefresh() { loadData(); }

    private void loadData() {
        List<DoiSoatRow> theoNguoi = dao.tongTheoNguoiThu();
        List<DoiSoatRow> theoNgay  = dao.tongTheoNgay();
        tblNguoiThu.setItems(FXCollections.observableArrayList(theoNguoi));
        tblNgay.setItems(FXCollections.observableArrayList(theoNgay));

        BigDecimal tong = theoNguoi.stream()
                .map(DoiSoatRow::getTong).reduce(BigDecimal.ZERO, BigDecimal::add);
        int soGD = theoNguoi.stream().mapToInt(DoiSoatRow::getSoGiaoDich).sum();
        lblTong.setText("Tổng thu toàn quỹ: " + currency.format(tong) + " đ  ·  " + soGD + " giao dịch");
    }
}
