package models;

public class QuanHe {
    private int id;
    private String tenQuanHe;

    public QuanHe() {}
    public QuanHe(int id, String tenQuanHe) {
        this.id = id;
        this.tenQuanHe = tenQuanHe;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTenQuanHe() { return tenQuanHe; }
    public void setTenQuanHe(String tenQuanHe) { this.tenQuanHe = tenQuanHe; }
}