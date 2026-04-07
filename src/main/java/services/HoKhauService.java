package services;

import models.HoKhau;
import java.util.List;

public class HoKhauService {
    private final HoKhauDAO dao = new HoKhauDAO();
    // Các phương thức tương tác với DAO
    public List<HoKhau> getAllHoKhau() { return dao.getAllHoKhau(); }
    public boolean addHoKhau(HoKhau hk) { return dao.themHoKhau(hk); }
    public boolean updateHoKhau(HoKhau hk) { return dao.capNhatHoKhau(hk); }
    public boolean deleteHoKhau(int id) { return dao.xoaHoKhau(id); }
}