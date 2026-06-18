-- =====================================================================
-- MIGRATION v1.1 -> v1.2  (chạy nếu KHÔNG tạo lại DB từ đầu)
-- Cách đơn giản & chắc chắn: DROP DATABASE rồi chạy create_tables.sql + seed_data.sql.
-- =====================================================================
USE bluemoon;
SET NAMES utf8mb4;

-- 1) Bug: cho phép "đến ngày" của tạm trú/tạm vắng được trống
ALTER TABLE tam_tru_tam_vang MODIFY den_ngay DATE NULL;

-- 2) Ràng buộc giá trị (MySQL 8.0.16+). Nếu dữ liệu cũ có giá trị lạ, hãy chuẩn hoá trước.
ALTER TABLE khoan_thu
    ADD CONSTRAINT chk_kt_loai      CHECK (loai IN ('BAT_BUOC','TU_NGUYEN')),
    ADD CONSTRAINT chk_kt_cachtinh  CHECK (cach_tinh IN ('FLAT','PER_NHANKHAU','PER_M2','PER_XE')),
    ADD CONSTRAINT chk_kt_trangthai CHECK (trang_thai IN ('OPEN','CLOSED'));
ALTER TABLE ho_khau
    ADD CONSTRAINT chk_hk_trangthai CHECK (trang_thai IN ('ACTIVE','INACTIVE'));
ALTER TABLE nhan_khau
    ADD CONSTRAINT chk_nk_gioitinh  CHECK (gioi_tinh IN ('Nam','Nữ','Khác')),
    ADD CONSTRAINT chk_nk_trangthai CHECK (trang_thai IN ('PERMANENT','TEMPORARY','MOVED_OUT','DECEASED'));

-- 3) Bảng nhật ký thao tác
CREATE TABLE IF NOT EXISTS audit_log (
      id INT PRIMARY KEY AUTO_INCREMENT,
      username VARCHAR(50),
      hanh_dong VARCHAR(30) NOT NULL,
      doi_tuong VARCHAR(50) NOT NULL,
      mo_ta VARCHAR(500),
      thoi_gian TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4) Index bổ sung
CREATE INDEX idx_nhan_khau_hoten ON nhan_khau(ho_ten);
CREATE INDEX idx_nhan_khau_cccd  ON nhan_khau(cccd);
CREATE INDEX idx_nop_tien_ngay   ON nop_tien(ngay_nop);
CREATE INDEX idx_audit_thoigian  ON audit_log(thoi_gian);
