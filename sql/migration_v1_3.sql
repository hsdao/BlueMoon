-- ============================================================
-- MIGRATION v1.3 — Chuẩn hóa số điện thoại (D1 + D3)
-- Chạy trên DB đã có dữ liệu (không cần nếu nạp lại từ create_tables.sql + seed_data.sql).
-- ============================================================
USE bluemoon;
SET NAMES utf8mb4;

-- D1: Cho phép SĐT nhân khẩu ĐỂ TRỐNG (trẻ sơ sinh/trẻ em/người già không có SĐT).
--     Vẫn UNIQUE khi có giá trị (MySQL cho phép nhiều NULL trong cột UNIQUE).
ALTER TABLE nhan_khau MODIFY so_dien_thoai VARCHAR(11) NULL;

-- ⚠️ CẢNH BÁO: KHÔNG chạy D3 với phiên bản code hiện tại.
--     Mã nguồn hiện vẫn dùng cột `so_dien_thoai_chu_ho` (HoKhau model + HoKhauDAO + seed_data).
--     Xóa cột này sẽ làm hỏng chức năng Hộ khẩu. Lệnh dưới đây được CỐ TÌNH vô hiệu hóa.
--
-- D3 (ĐÃ VÔ HIỆU HÓA): Bỏ cột SĐT chủ hộ trùng lặp ở ho_khau.
-- ALTER TABLE ho_khau DROP COLUMN so_dien_thoai_chu_ho;
