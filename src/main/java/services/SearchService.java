package services;

import models.HoKhau;
import models.KhoanThu;
import models.NhanKhau;
import models.SearchResult;
import models.SearchResult.Loai;

import java.util.ArrayList;
import java.util.List;

/**
 * Service tìm kiếm toàn cục đa bảng.
 * Tìm đồng thời trên: Hộ khẩu, Nhân khẩu, Khoản thu.
 * Dùng các DAO đã có — không sửa file gốc.
 */
public class SearchService {

    private final HoKhauDAO   hoKhauDAO   = new HoKhauDAO();
    private final NhanKhauDAO nhanKhauDAO = new NhanKhauDAO();
    private final KhoanThuDAO khoanThuDAO = new KhoanThuDAO();

    // =========================================================
    //  TÌM KIẾM TOÀN CỤC
    // =========================================================

    /**
     * Tìm kiếm tất cả bảng theo từ khóa, trả về danh sách kết quả.
     *
     * @param tuKhoa Từ khóa tìm kiếm (không phân biệt hoa thường)
     * @return Danh sách SearchResult tổng hợp từ 3 bảng
     */
    public List<SearchResult> timKiem(String tuKhoa) {
        List<SearchResult> results = new ArrayList<>();
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) return results;

        String kw = tuKhoa.trim().toLowerCase();
        results.addAll(timHoKhau(kw));
        results.addAll(timNhanKhau(kw));
        results.addAll(timKhoanThu(kw));
        return results;
    }

    /**
     * Chỉ tìm trong bảng Hộ khẩu.
     * Tìm theo: mã hộ, địa chỉ.
     */
    public List<SearchResult> timHoKhau(String kw) {
        List<SearchResult> results = new ArrayList<>();
        for (HoKhau hk : hoKhauDAO.getAllHoKhau()) {
            if (matchHoKhau(hk, kw)) {
                results.add(new SearchResult(
                    Loai.HO_KHAU,
                    hk.getId(),
                    hk.getMaHo(),
                    hk.getDiaChi() + " | " + hk.getSoThanhVien() + " thành viên",
                    hk
                ));
            }
        }
        return results;
    }

    /**
     * Chỉ tìm trong bảng Nhân khẩu.
     * Tìm theo: họ tên, CCCD, số điện thoại, nghề nghiệp.
     */
    public List<SearchResult> timNhanKhau(String kw) {
        List<SearchResult> results = new ArrayList<>();
        for (NhanKhau nk : nhanKhauDAO.getAll()) {
            if (matchNhanKhau(nk, kw)) {
                String moTa = "";
                if (nk.getCccd() != null) moTa += "CCCD: " + nk.getCccd();
                if (nk.getGioiTinh() != null) moTa += " | " + nk.getGioiTinh();
                if (nk.getNgheNghiep() != null) moTa += " | " + nk.getNgheNghiep();
                results.add(new SearchResult(
                    Loai.NHAN_KHAU,
                    nk.getId(),
                    nk.getHoTen(),
                    moTa,
                    nk
                ));
            }
        }
        return results;
    }

    /**
     * Chỉ tìm trong bảng Khoản thu.
     * Tìm theo: mã khoản, tên khoản, loại, mô tả.
     */
    public List<SearchResult> timKhoanThu(String kw) {
        List<SearchResult> results = new ArrayList<>();
        for (KhoanThu kt : khoanThuDAO.getAllKhoanThu()) {
            if (matchKhoanThu(kt, kw)) {
                String moTa = kt.getLoai();
                if (kt.getSoTien() != null) moTa += " | " + String.format("%,.0f VNĐ", kt.getSoTien());
                if (kt.getTrangThai() != null) moTa += " | " + kt.getTrangThai();
                results.add(new SearchResult(
                    Loai.KHOAN_THU,
                    kt.getId(),
                    kt.getMaKhoan() + " – " + kt.getTenKhoan(),
                    moTa,
                    kt
                ));
            }
        }
        return results;
    }

    // =========================================================
    //  BỘ LỌC NÂNG CAO – Hộ khẩu
    // =========================================================

    /**
     * Lọc hộ khẩu đa điều kiện.
     *
     * @param tuKhoa    Từ khóa (mã hộ / địa chỉ), để trống = bỏ qua
     * @param trangThai Trạng thái hộ, null = tất cả
     * @return Danh sách HoKhau thỏa mãn bộ lọc
     */
    public List<HoKhau> locHoKhau(String tuKhoa, String trangThai) {
        String kw = tuKhoa == null ? "" : tuKhoa.trim().toLowerCase();
        List<HoKhau> results = new ArrayList<>();
        for (HoKhau hk : hoKhauDAO.getAllHoKhau()) {
            if (!kw.isEmpty() && !matchHoKhau(hk, kw)) continue;
            if (trangThai != null && !trangThai.isEmpty()
                    && !trangThai.equals(hk.getTrangThai())) continue;
            results.add(hk);
        }
        return results;
    }

    // =========================================================
    //  BỘ LỌC NÂNG CAO – Khoản thu
    // =========================================================

    /**
     * Lọc khoản thu đa điều kiện.
     *
     * @param tuKhoa Từ khóa, để trống = bỏ qua
     * @param loai   Loại khoản thu (Bắt buộc / Tự nguyện), null = tất cả
     * @param trangThai Trạng thái, null = tất cả
     * @return Danh sách KhoanThu thỏa mãn
     */
    public List<KhoanThu> locKhoanThu(String tuKhoa, String loai, String trangThai) {
        String kw = tuKhoa == null ? "" : tuKhoa.trim().toLowerCase();
        List<KhoanThu> results = new ArrayList<>();
        for (KhoanThu kt : khoanThuDAO.getAllKhoanThu()) {
            if (!kw.isEmpty() && !matchKhoanThu(kt, kw)) continue;
            if (loai != null && !loai.isEmpty() && !loai.equals(kt.getLoai())) continue;
            if (trangThai != null && !trangThai.isEmpty()
                    && !trangThai.equals(kt.getTrangThai())) continue;
            results.add(kt);
        }
        return results;
    }

    // =========================================================
    //  HELPER – khớp từ khóa
    // =========================================================

    private boolean matchHoKhau(HoKhau hk, String kw) {
        if (hk.getMaHo() != null && hk.getMaHo().toLowerCase().contains(kw)) return true;
        if (hk.getDiaChi() != null && hk.getDiaChi().toLowerCase().contains(kw)) return true;
        if (hk.getSoDienThoaiChuHo() != null && hk.getSoDienThoaiChuHo().contains(kw)) return true;
        return false;
    }

    private boolean matchNhanKhau(NhanKhau nk, String kw) {
        if (nk.getHoTen() != null && nk.getHoTen().toLowerCase().contains(kw)) return true;
        if (nk.getCccd() != null && nk.getCccd().contains(kw)) return true;
        if (nk.getSoDienThoai() != null && nk.getSoDienThoai().contains(kw)) return true;
        if (nk.getNgheNghiep() != null && nk.getNgheNghiep().toLowerCase().contains(kw)) return true;
        if (nk.getDiaChiThuongTru() != null && nk.getDiaChiThuongTru().toLowerCase().contains(kw)) return true;
        return false;
    }

    private boolean matchKhoanThu(KhoanThu kt, String kw) {
        if (kt.getMaKhoan() != null && kt.getMaKhoan().toLowerCase().contains(kw)) return true;
        if (kt.getTenKhoan() != null && kt.getTenKhoan().toLowerCase().contains(kw)) return true;
        if (kt.getLoai() != null && kt.getLoai().toLowerCase().contains(kw)) return true;
        if (kt.getMoTa() != null && kt.getMoTa().toLowerCase().contains(kw)) return true;
        return false;
    }
}
