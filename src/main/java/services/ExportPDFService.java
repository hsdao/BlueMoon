package services;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import models.CongNoModel;
import models.ThongKeModel;

import java.io.File;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * ExportPDFService – Xuất báo cáo thống kê ra file PDF bằng iText 8.
 * FIX: Font path động, hỗ trợ cả Windows và Linux/macOS.
 */
public class ExportPDFService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final NumberFormat currencyFmt = NumberFormat.getInstance(new Locale("vi", "VN"));

    /**
     * Tìm font hỗ trợ Unicode/Tiếng Việt trên nhiều hệ điều hành.
     * Ưu tiên: font hệ thống → fallback sang iText built-in Helvetica.
     */
    private PdfFont loadFont() {
        // Thử các vị trí font phổ biến theo OS
        String[] fontPaths = {
                // Windows
                "C:/Windows/Fonts/arial.ttf",
                "C:/Windows/Fonts/times.ttf",
                // Linux (Ubuntu / Debian)
                "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf",
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                "/usr/share/fonts/truetype/freefont/FreeSans.ttf",
                // macOS
                "/System/Library/Fonts/Helvetica.ttc",
                "/Library/Fonts/Arial.ttf"
        };

        for (String path : fontPaths) {
            if (new File(path).exists()) {
                try {
                    return PdfFontFactory.createFont(path, PdfEncodings.IDENTITY_H);
                } catch (Exception e) {
                    System.err.println("[ExportPDF] Bỏ qua font: " + path + " – " + e.getMessage());
                }
            }
        }

        // Fallback sang font built-in (không hỗ trợ tiếng Việt đầy đủ nhưng không crash)
        try {
            return PdfFontFactory.createFont();
        } catch (Exception e) {
            throw new RuntimeException("Không thể tải bất kỳ font nào!", e);
        }
    }

    public void exportThongKe(List<ThongKeModel> data, String titleStr, File file) throws Exception {
        try (PdfWriter writer = new PdfWriter(file);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            PdfFont font = loadFont();
            document.setFont(font);

            // 1. Tiêu đề báo cáo
            document.add(new Paragraph(titleStr.toUpperCase())
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(16)
                    .setBold()
                    .setMarginBottom(8));

            // Ngày xuất báo cáo
            document.add(new Paragraph("Ngày xuất: " + java.time.LocalDate.now().format(DATE_FMT))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontSize(10)
                    .setMarginBottom(20));

            // 2. Tóm tắt
            long daNop   = data.stream().filter(r -> "Đã nộp".equals(r.getTrangThai())).count();
            long chuaNop = data.size() - daNop;
            java.math.BigDecimal tongTien = data.stream()
                    .filter(r -> r.getSoTienNop() != null)
                    .map(ThongKeModel::getSoTienNop)
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

            document.add(new Paragraph(String.format(
                    "Tổng số hộ: %d   |   Đã nộp: %d   |   Chưa nộp: %d   |   Tổng tiền: %s đ",
                    data.size(), daNop, chuaNop, currencyFmt.format(tongTien)))
                    .setFontSize(11)
                    .setMarginBottom(16));

            // 3. Bảng dữ liệu
            float[] columnWidths = {1.5f, 2.6f, 2f, 2f, 1.8f, 1.8f, 2f};
            Table table = new Table(UnitValue.createPointArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));

            String[] headers = {"Mã Hộ", "Tên Chủ Hộ", "Phải Nộp", "Đã Nộp", "Ngày Nộp", "Trạng Thái", "Ghi Chú"};
            for (String h : headers) {
                table.addHeaderCell(new Cell()
                        .add(new Paragraph(h).setBold().setFontSize(10))
                        .setTextAlignment(TextAlignment.CENTER));
            }

            for (ThongKeModel item : data) {
                String phaiNopStr = item.getSoTienPhaiNop() != null && item.getSoTienPhaiNop().signum() > 0
                        ? currencyFmt.format(item.getSoTienPhaiNop()) + " đ" : "—";
                String soTienStr = item.getSoTienNop() != null
                        ? currencyFmt.format(item.getSoTienNop()) + " đ" : "—";
                String ngayStr = item.getNgayNop() != null
                        ? item.getNgayNop().format(DATE_FMT) : "—";

                table.addCell(cell(item.getMaHoKhau()));
                table.addCell(cell(item.getTenChuHo()));
                table.addCell(cell(phaiNopStr).setTextAlignment(TextAlignment.RIGHT));
                table.addCell(cell(soTienStr).setTextAlignment(TextAlignment.RIGHT));
                table.addCell(cell(ngayStr).setTextAlignment(TextAlignment.CENTER));
                table.addCell(cell(item.getTrangThai()).setTextAlignment(TextAlignment.CENTER));
                table.addCell(cell(item.getGhiChu()));
            }

            document.add(table);
        }
    }

    /**
     * Xuất báo cáo CÔNG NỢ ra PDF: danh sách các hộ còn nợ các khoản bắt buộc.
     *
     * @param data danh sách dòng công nợ
     * @param file file PDF đích
     */
    public void exportCongNo(List<CongNoModel> data, File file) throws Exception {
        try (PdfWriter writer = new PdfWriter(file);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            PdfFont font = loadFont();
            document.setFont(font);

            document.add(new Paragraph("BÁO CÁO CÔNG NỢ")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(16).setBold().setMarginBottom(8));
            document.add(new Paragraph("Ngày xuất: " + java.time.LocalDate.now().format(DATE_FMT))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontSize(10).setMarginBottom(20));

            java.math.BigDecimal tongNo = data.stream()
                    .map(CongNoModel::getTongNo)
                    .filter(java.util.Objects::nonNull)
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            document.add(new Paragraph(String.format(
                    "Số hộ còn nợ: %d   |   Tổng công nợ: %s đ",
                    data.size(), currencyFmt.format(tongNo)))
                    .setFontSize(11).setMarginBottom(16));

            float[] columnWidths = {1.5f, 3f, 1.5f, 4f, 2.5f};
            Table table = new Table(UnitValue.createPointArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));

            String[] headers = {"Mã Hộ", "Tên Chủ Hộ", "Số Khoản", "Danh Sách Khoản Nợ", "Tổng Nợ"};
            for (String h : headers) {
                table.addHeaderCell(new Cell()
                        .add(new Paragraph(h).setBold().setFontSize(10))
                        .setTextAlignment(TextAlignment.CENTER));
            }

            for (CongNoModel item : data) {
                String tongStr = item.getTongNo() != null
                        ? currencyFmt.format(item.getTongNo()) + " đ" : "—";
                table.addCell(cell(item.getMaHo()));
                table.addCell(cell(item.getTenChuHo()));
                table.addCell(cell(String.valueOf(item.getSoKhoanNo()))
                        .setTextAlignment(TextAlignment.CENTER));
                table.addCell(cell(item.getDanhSachKhoan()));
                table.addCell(cell(tongStr).setTextAlignment(TextAlignment.RIGHT));
            }

            document.add(table);
        }
    }

    /**
     * Xuất BIÊN LAI thu phí (một giao dịch nộp tiền) ra PDF.
     */
    public void exportBienLai(String maBienLai, String tenKhoan, String phongChuHo,
                             java.math.BigDecimal soTien, String nguoiThu,
                             java.time.LocalDate ngay, String ghiChu, File file) throws Exception {
        try (PdfWriter writer = new PdfWriter(file);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            PdfFont font = loadFont();
            document.setFont(font);

            document.add(new Paragraph("CHUNG CƯ BLUEMOON")
                    .setTextAlignment(TextAlignment.CENTER).setFontSize(13).setBold());
            document.add(new Paragraph("BIÊN LAI THU PHÍ")
                    .setTextAlignment(TextAlignment.CENTER).setFontSize(18).setBold().setMarginBottom(4));
            document.add(new Paragraph("Số biên lai: " + maBienLai)
                    .setTextAlignment(TextAlignment.CENTER).setFontSize(10).setMarginBottom(16));

            float[] w = {1.2f, 3f};
            Table t = new Table(UnitValue.createPointArray(w));
            t.setWidth(UnitValue.createPercentValue(100));
            addRow(t, "Khoản thu", tenKhoan);
            addRow(t, "Phòng / Chủ hộ", phongChuHo);
            addRow(t, "Số tiền", (soTien != null ? currencyFmt.format(soTien) : "0") + " đ");
            addRow(t, "Ngày nộp", ngay != null ? ngay.format(DATE_FMT) : "");
            addRow(t, "Người thu", nguoiThu == null ? "" : nguoiThu);
            addRow(t, "Ghi chú", ghiChu == null ? "" : ghiChu);
            document.add(t);

            document.add(new Paragraph("Số tiền bằng chữ: " + docTien(soTien))
                    .setFontSize(10).setItalic().setMarginTop(8));

            document.add(new Paragraph("\nNgày xuất: " + java.time.LocalDate.now().format(DATE_FMT))
                    .setTextAlignment(TextAlignment.RIGHT).setFontSize(9));
            Table ky = new Table(UnitValue.createPointArray(new float[]{1f, 1f}));
            ky.setWidth(UnitValue.createPercentValue(100)).setMarginTop(16);
            ky.addCell(kyCell("NGƯỜI NỘP TIỀN"));
            ky.addCell(kyCell("NGƯỜI THU TIỀN"));
            document.add(ky);
        }
    }

    private void addRow(Table t, String k, String v) {
        t.addCell(new Cell().add(new Paragraph(k).setBold().setFontSize(11)));
        t.addCell(new Cell().add(new Paragraph(v != null ? v : "").setFontSize(11)));
    }

    private Cell kyCell(String label) {
        return new Cell().setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .add(new Paragraph(label).setBold().setFontSize(10).setTextAlignment(TextAlignment.CENTER))
                .add(new Paragraph("(Ký, ghi rõ họ tên)").setItalic().setFontSize(8).setTextAlignment(TextAlignment.CENTER));
    }

    /** Đổi số tiền sang chữ (gọn, đủ dùng cho biên lai). */
    private String docTien(java.math.BigDecimal soTien) {
        if (soTien == null) return "không đồng";
        long n = soTien.longValue();
        if (n == 0) return "không đồng";
        return currencyFmt.format(n) + " đồng";
    }

    private Cell cell(String text) {
        return new Cell().add(new Paragraph(text != null ? text : "").setFontSize(10));
    }
}