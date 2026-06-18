package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import models.CongNoModel;
import models.HoKhau;
import models.NhanKhau;
import models.Phong;
import services.CongNoDAO;
import services.HoKhauDAO;
import services.NhanKhauDAO;
import services.PhongDAO;

import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Sơ đồ căn hộ: hiển thị TẤT CẢ phòng trong tòa nhà (danh mục dm_phong), xếp theo TẦNG.
 * Màu: xám = phòng trống, xanh = có hộ & đã nộp đủ, đỏ = có hộ & còn nợ.
 * Bấm vào phòng để xem chi tiết.
 */
public class SoDoPhongController implements Initializable {

    @FXML private VBox container;
    @FXML private Label lblSummary;

    private final PhongDAO    phongDAO    = new PhongDAO();
    private final HoKhauDAO   hoKhauDAO   = new HoKhauDAO();
    private final NhanKhauDAO nhanKhauDAO = new NhanKhauDAO();
    private final CongNoDAO   congNoDAO   = new CongNoDAO();
    private final NumberFormat currency = NumberFormat.getInstance(Locale.of("vi", "VN"));

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadData();
    }

    @FXML
    private void onRefresh() { loadData(); }

    private void loadData() {
        container.getChildren().clear();

        hoKhauDAO.recomputeAllSoThanhVien(); // đồng bộ số thành viên trước khi tính nợ/màu
        List<Phong> phongs = phongDAO.getAll();
        // Hộ khẩu theo mã phòng
        Map<String, HoKhau> hoByMaHo = new HashMap<>();
        for (HoKhau hk : hoKhauDAO.getAllHoKhau()) hoByMaHo.put(hk.getMaHo(), hk);
        // Tên chủ hộ
        Map<Integer, String> tenChuHo = nhanKhauDAO.getAll().stream()
                .collect(Collectors.toMap(NhanKhau::getId, NhanKhau::getHoTen, (a, b) -> a));
        // Công nợ theo mã phòng (chỉ hộ còn nợ)
        Map<String, CongNoModel> noMap = new HashMap<>();
        for (CongNoModel c : congNoDAO.tinhCongNo()) noMap.put(c.getMaHo(), c);

        Map<Integer, List<Phong>> theoTang = new TreeMap<>();
        for (Phong p : phongs) theoTang.computeIfAbsent(p.getTang(), k -> new ArrayList<>()).add(p);

        int trong = 0, xanh = 0, do_ = 0;
        BigDecimal tongNo = BigDecimal.ZERO;

        for (Map.Entry<Integer, List<Phong>> e : theoTang.entrySet()) {
            Label lblTang = new Label("Tầng " + e.getKey());
            lblTang.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #24292F;");

            FlowPane grid = new FlowPane(12, 12);
            for (Phong p : e.getValue()) {
                HoKhau hk = hoByMaHo.get(p.getMaPhong());
                if (hk == null) {
                    trong++;
                    grid.getChildren().add(theTrong(p));
                } else {
                    CongNoModel no = noMap.get(p.getMaPhong());
                    if (no != null) { do_++; tongNo = tongNo.add(no.getTongNo()); }
                    else xanh++;
                    grid.getChildren().add(theCoHo(p, hk, no, tenChuHo.get(hk.getChuHoId())));
                }
            }
            container.getChildren().addAll(lblTang, grid);
        }

        lblSummary.setText(String.format(
                "Tổng %d phòng  ·  Trống: %d  ·  Đã nộp đủ: %d  ·  Còn nợ: %d  ·  Tổng công nợ: %s đ",
                phongs.size(), trong, xanh, do_, currency.format(tongNo)));
    }

    // ----- Thẻ phòng trống -----
    private VBox theTrong(Phong p) {
        VBox card = baseCard("#F0F2F5", "#AEB6C2");
        Label ten = new Label(p.getMaPhong());
        ten.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #57606a;");
        Label trang = new Label("Trống");
        trang.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #6E7781;");
        Label dt = new Label(p.getDienTich() + " m²");
        dt.setStyle("-fx-font-size: 11px; -fx-text-fill: #6E7781;");
        card.getChildren().addAll(ten, trang, dt);
        card.setOnMouseClicked(ev -> showTrong(p));
        return card;
    }

    // ----- Thẻ phòng có hộ -----
    private VBox theCoHo(Phong p, HoKhau hk, CongNoModel no, String chuHo) {
        boolean coNo = (no != null);
        VBox card = baseCard(coNo ? "#FFE3E3" : "#DAF5E0", coNo ? "#82071E" : "#2DA44E");

        Label ten = new Label(p.getMaPhong());
        ten.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #24292F;");
        Label chu = new Label(chuHo != null ? chuHo : "—");
        chu.setStyle("-fx-font-size: 11px; -fx-text-fill: #57606a;");
        chu.setWrapText(true);
        String ttText = !coNo ? "Đã nộp đủ"
                : (no.getTongNo().signum() > 0 ? "Nợ: " + currency.format(no.getTongNo()) + " đ"
                                               : "Chưa nộp đủ");
        Label tt = new Label(ttText);
        tt.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: "
                + (coNo ? "#82071E" : "#2DA44E") + ";");

        card.getChildren().addAll(ten, chu, tt);
        card.setOnMouseClicked(ev -> showChiTiet(p, hk, no, chuHo));
        return card;
    }

    private VBox baseCard(String bg, String border) {
        VBox card = new VBox(4);
        card.setPrefSize(160, 92);
        card.setPadding(new Insets(10));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 8;"
                + " -fx-border-color: " + border + "; -fx-border-radius: 8; -fx-cursor: hand;");
        return card;
    }

    private void showTrong(Phong p) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Phòng " + p.getMaPhong());
        a.setHeaderText(null);
        a.setContentText("Phòng TRỐNG\nTầng: " + p.getTang()
                + "\nDiện tích: " + p.getDienTich() + " m²"
                + "\n\nVào màn Hộ khẩu → Thêm mới để gán hộ vào phòng này.");
        a.showAndWait();
    }

    private void showChiTiet(Phong p, HoKhau hk, CongNoModel no, String chuHo) {
        StringBuilder sb = new StringBuilder();
        sb.append("Phòng        : ").append(p.getMaPhong()).append(" (Tầng ").append(p.getTang()).append(")\n");
        sb.append("Diện tích    : ").append(p.getDienTich()).append(" m²\n");
        sb.append("Chủ hộ       : ").append(chuHo == null ? "—" : chuHo).append("\n");
        sb.append("SĐT chủ hộ   : ").append(hk.getSoDienThoaiChuHo() == null ? "—" : hk.getSoDienThoaiChuHo()).append("\n");
        sb.append("Số thành viên: ").append(hk.getSoThanhVien()).append("\n");
        sb.append("Xe           : ").append(hk.getSoXeMay()).append(" xe máy, ")
                .append(hk.getSoOTo()).append(" ô tô\n\n");
        if (no == null) {
            sb.append("Trạng thái   : ĐÃ NỘP ĐỦ các khoản bắt buộc.");
        } else {
            sb.append("CÒN NỢ ").append(no.getSoKhoanNo()).append(" khoản — tổng ")
              .append(currency.format(no.getTongNo())).append(" đ\nCác khoản chưa nộp:\n");
            String ds = no.getDanhSachKhoan();
            if (ds != null && !ds.isBlank())
                for (String ten : ds.split(",")) sb.append("   • ").append(ten.trim()).append("\n");
        }

        TextArea ta = new TextArea(sb.toString());
        ta.setEditable(false); ta.setWrapText(true); ta.setPrefSize(460, 300);
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Chi tiết phòng " + p.getMaPhong());
        a.setHeaderText(null);
        a.getDialogPane().setContent(ta);
        a.getDialogPane().setPrefWidth(500);
        a.setResizable(true);
        a.showAndWait();
    }
}
