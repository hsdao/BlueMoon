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
            float[] columnWidths = {1.5f, 3f, 2.5f, 2f, 2f, 2f};
            Table table = new Table(UnitValue.createPointArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));

            String[] headers = {"Mã Hộ", "Tên Chủ Hộ", "Số Tiền", "Ngày Nộp", "Trạng Thái", "Ghi Chú"};
            for (String h : headers) {
                table.addHeaderCell(new Cell()
                        .add(new Paragraph(h).setBold().setFontSize(10))
                        .setTextAlignment(TextAlignment.CENTER));
            }

            for (ThongKeModel item : data) {
                String soTienStr = item.getSoTienNop() != null
                        ? currencyFmt.format(item.getSoTienNop()) + " đ" : "—";
                String ngayStr = item.getNgayNop() != null
                        ? item.getNgayNop().format(DATE_FMT) : "—";

                table.addCell(cell(item.getMaHoKhau()));
                table.addCell(cell(item.getTenChuHo()));
                table.addCell(cell(soTienStr).setTextAlignment(TextAlignment.RIGHT));
                table.addCell(cell(ngayStr).setTextAlignment(TextAlignment.CENTER));
                table.addCell(cell(item.getTrangThai()).setTextAlignment(TextAlignment.CENTER));
                table.addCell(cell(item.getGhiChu()));
            }

            document.add(table);
        }
    }

    private Cell cell(String text) {
        return new Cell().add(new Paragraph(text != null ? text : "").setFontSize(10));
    }
}