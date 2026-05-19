package models;

/**
 * Model chứa một kết quả tìm kiếm toàn cục.
 * Dùng chung cho cả 3 loại: Hộ khẩu, Nhân khẩu, Khoản thu.
 */
public class SearchResult {

    /** Loại đối tượng tìm thấy. */
    public enum Loai {
        HO_KHAU("Hộ Khẩu"),
        NHAN_KHAU("Nhân Khẩu"),
        KHOAN_THU("Khoản Thu");

        private final String label;
        Loai(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    private Loai   loai;
    private int    id;
    private String tieuDe;   // Dòng chính hiển thị (tên / mã)
    private String moTa;     // Dòng phụ (địa chỉ / loại / CCCD...)
    private Object duLieuGoc; // Object gốc để có thể navigate

    public SearchResult() {}

    public SearchResult(Loai loai, int id, String tieuDe, String moTa, Object duLieuGoc) {
        this.loai      = loai;
        this.id        = id;
        this.tieuDe    = tieuDe;
        this.moTa      = moTa;
        this.duLieuGoc = duLieuGoc;
    }

    public Loai   getLoai()      { return loai; }
    public void   setLoai(Loai loai) { this.loai = loai; }

    public int    getId()        { return id; }
    public void   setId(int id)  { this.id = id; }

    public String getTieuDe()    { return tieuDe; }
    public void   setTieuDe(String tieuDe) { this.tieuDe = tieuDe; }

    public String getMoTa()      { return moTa; }
    public void   setMoTa(String moTa)   { this.moTa = moTa; }

    public Object getDuLieuGoc() { return duLieuGoc; }
    public void   setDuLieuGoc(Object o) { this.duLieuGoc = o; }

    /** Hiện thị trong TableView cột Loại. */
    public String getLoaiLabel() { return loai != null ? loai.getLabel() : ""; }

    @Override
    public String toString() {
        return "[" + getLoaiLabel() + "] " + tieuDe;
    }
}
