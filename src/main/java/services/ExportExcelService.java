package services;

import models.ThongKeModel;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExportExcelService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void exportThongKe(List<ThongKeModel> data, String reportTitle, File file) throws Exception {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Thong Ke");

            // Title row
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(reportTitle);
            CellStyle titleStyle = wb.createCellStyle();
            Font titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

            // Header row
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            String[] headers = {
                "Ma Ho Khau", "Ten Chu Ho", "Phai Nop (VND)", "Da Nop (VND)",
                "Ngay Nop", "Trang Thai", "Ghi Chu"
            };
            Row headerRow = sheet.createRow(1);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            CellStyle dataStyle = wb.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            int rowNum = 2;
            for (ThongKeModel row : data) {
                Row dataRow = sheet.createRow(rowNum++);
                dataRow.createCell(0).setCellValue(row.getMaHoKhau()  != null ? row.getMaHoKhau()  : "");
                dataRow.createCell(1).setCellValue(row.getTenChuHo()  != null ? row.getTenChuHo()  : "");
                BigDecimal phaiNop = row.getSoTienPhaiNop();
                dataRow.createCell(2).setCellValue(phaiNop != null ? phaiNop.doubleValue() : 0);
                BigDecimal soTien = row.getSoTienNop();
                dataRow.createCell(3).setCellValue(soTien != null ? soTien.doubleValue() : 0);
                dataRow.createCell(4).setCellValue(
                    row.getNgayNop() != null ? row.getNgayNop().format(DATE_FMT) : "");
                dataRow.createCell(5).setCellValue(row.getTrangThai() != null ? row.getTrangThai() : "");
                dataRow.createCell(6).setCellValue(row.getGhiChu()    != null ? row.getGhiChu()    : "");
                for (int c = 0; c < headers.length; c++) dataRow.getCell(c).setCellStyle(dataStyle);
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                int width = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, Math.min(width + 512, 15000));
            }

            try (FileOutputStream out = new FileOutputStream(file)) {
                wb.write(out);
            }
        }
    }
}
