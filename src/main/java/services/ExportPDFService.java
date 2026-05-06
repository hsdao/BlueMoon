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
import java.util.List;

public class ExportPDFService {
    // Đường dẫn font Arial mặc định trên Windows để hỗ trợ Tiếng Việt
    private static final String FONT_PATH = "C:/Windows/Fonts/arial.ttf";

    public void exportThongKe(List<ThongKeModel> data, String titleStr, File file) throws Exception {
        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Load Font hỗ trợ Tiếng Việt (Unicode)
        PdfFont font = PdfFontFactory.createFont(FONT_PATH, PdfEncodings.IDENTITY_H);
        document.setFont(font);

        // 1. Thêm Tiêu đề báo cáo
        Paragraph header = new Paragraph(titleStr.toUpperCase())
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(18)
                .setBold()
                .setMarginBottom(20);
        document.add(header);

        // 2. Tạo bảng 6 cột với tỷ lệ chiều rộng khác nhau
        float[] columnWidths = {1.5f, 3f, 2f, 2f, 2f, 2f};
        Table table = new Table(UnitValue.createPointArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));

        // 3. Thêm Header cho Bảng
        String[] headers = {"Mã Hộ", "Tên Chủ Hộ", "Số Tiền", "Ngày Nộp", "Trạng Thái", "Ghi Chú"};
        for (String h : headers) {
            table.addHeaderCell(new Cell().add(new Paragraph(h).setBold())
                    .setTextAlignment(TextAlignment.CENTER));
        }

        // 4. Đổ dữ liệu từ danh sách vào các dòng
        for (ThongKeModel item : data) {
            table.addCell(new Cell().add(new Paragraph(item.getMaHoKhau())));
            table.addCell(new Cell().add(new Paragraph(item.getTenChuHo())));
            table.addCell(new Cell().add(new Paragraph(item.getSoTienNop() != null ? item.getSoTienNop().toString() : "0")));
            table.addCell(new Cell().add(new Paragraph(item.getNgayNop() != null ? item.getNgayNop().toString() : "")));
            table.addCell(new Cell().add(new Paragraph(item.getTrangThai())));
            table.addCell(new Cell().add(new Paragraph(item.getGhiChu())));
        }

        document.add(table);
        document.close();
    }
}