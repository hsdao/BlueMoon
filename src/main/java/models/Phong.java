package models;

/** Một phòng/căn hộ trong danh mục cố định của tòa nhà (dm_phong). */
public class Phong {
    private int id;
    private String maPhong;
    private int tang;
    private double dienTich;

    public Phong() {}

    public Phong(int id, String maPhong, int tang, double dienTich) {
        this.id = id; this.maPhong = maPhong; this.tang = tang; this.dienTich = dienTich;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMaPhong() { return maPhong; }
    public void setMaPhong(String maPhong) { this.maPhong = maPhong; }

    public int getTang() { return tang; }
    public void setTang(int tang) { this.tang = tang; }

    public double getDienTich() { return dienTich; }
    public void setDienTich(double dienTich) { this.dienTich = dienTich; }
}
