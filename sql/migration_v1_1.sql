-- =====================================================================
-- MIGRATION v1.0 -> v1.1
-- Chạy file này nếu DB "bluemoon" ĐÃ tồn tại và có dữ liệu (không muốn drop).
-- Nếu tạo mới hoàn toàn thì chỉ cần chạy create_tables.sql (đã có sẵn cột mới).
-- =====================================================================
USE bluemoon;

-- 1) ho_khau: diện tích + số xe (phí theo m2 / phí gửi xe)
ALTER TABLE ho_khau
    ADD COLUMN dien_tich DECIMAL(8,2) DEFAULT 0 AFTER so_thanh_vien,
    ADD COLUMN so_xe_may INT DEFAULT 0          AFTER dien_tich,
    ADD COLUMN so_o_to   INT DEFAULT 0          AFTER so_xe_may;

-- (Các trường nhân thân ĐANG DÙNG: dan_toc, ton_giao, nghe_nghiep, noi_lam_viec,
--  que_quan, dia_chi_thuong_tru — đã có sẵn trong create_tables.sql.)

-- 2) khoan_thu: cách tính phí + số tháng
ALTER TABLE khoan_thu
    ADD COLUMN cach_tinh VARCHAR(20) NOT NULL DEFAULT 'FLAT' AFTER so_tien,
    ADD COLUMN so_thang  INT DEFAULT 1                       AFTER cach_tinh;
