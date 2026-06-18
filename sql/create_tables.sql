-- DATABASE bluemoon
CREATE DATABASE IF NOT EXISTS bluemoon
       CHARACTER SET utf8mb4
       COLLATE utf8mb4_unicode_ci;

USE bluemoon;
-- 1. USERS
CREATE TABLE users (
   id INT PRIMARY KEY AUTO_INCREMENT,
   username VARCHAR(50) NOT NULL UNIQUE,
   password VARCHAR(255) NOT NULL,
   role VARCHAR(20) NOT NULL DEFAULT 'STAFF',
   full_name VARCHAR(100),
   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Bảng quan_he
CREATE TABLE quan_he (
     id INT PRIMARY KEY AUTO_INCREMENT,
     ten_quan_he VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Households
CREATE TABLE ho_khau (
     id INT PRIMARY KEY AUTO_INCREMENT,
     ma_ho VARCHAR(20) NOT NULL UNIQUE,
     chu_ho_id INT,
     so_dien_thoai_chu_ho VARCHAR(11) NOT NULL UNIQUE,
     so_thanh_vien INT DEFAULT 0,
     dien_tich DECIMAL(8,2) DEFAULT 0,   -- diện tích căn hộ (m2) — phí theo m2
     so_xe_may INT DEFAULT 0,            -- số xe máy — phí gửi xe
     so_o_to   INT DEFAULT 0,            -- số ô tô  — phí gửi xe
     dia_chi VARCHAR(255) NOT NULL,
     ngay_tao DATE NOT NULL,
     trang_thai VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
     ghi_chu TEXT,
     CONSTRAINT chk_hk_trangthai CHECK (trang_thai IN ('ACTIVE','INACTIVE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. RESIDENTS
CREATE TABLE nhan_khau(
      id INT PRIMARY KEY AUTO_INCREMENT,
      ho_khau_id INT,
      ho_ten VARCHAR(100) NOT NULL,
      ngay_sinh DATE NOT NULL,
      gioi_tinh VARCHAR(10) NOT NULL,
      cccd VARCHAR(20),
      dan_toc VARCHAR(50),
      ton_giao VARCHAR(50),
      nghe_nghiep VARCHAR(100),
      noi_lam_viec VARCHAR(255),
      que_quan VARCHAR(255),
      dia_chi_thuong_tru VARCHAR(255),
      quan_he_id INT, -- Đã sửa thành INT
      so_dien_thoai VARCHAR(11) UNIQUE,  -- cho phép trống (trẻ em/người già); vẫn UNIQUE khi có
      trang_thai VARCHAR(20) NOT NULL DEFAULT 'PERMANENT',
      CONSTRAINT chk_nk_gioitinh  CHECK (gioi_tinh IN ('Nam','Nữ','Khác')),
      CONSTRAINT chk_nk_trangthai CHECK (trang_thai IN ('PERMANENT','TEMPORARY','MOVED_OUT','DECEASED')),
      FOREIGN KEY (ho_khau_id) REFERENCES ho_khau(id) ON DELETE SET NULL,
      FOREIGN KEY (quan_he_id) REFERENCES quan_he(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Fees
CREATE TABLE khoan_thu (
   id INT PRIMARY KEY AUTO_INCREMENT,
   ma_khoan VARCHAR(20) NOT NULL UNIQUE,
   ten_khoan VARCHAR(255) NOT NULL UNIQUE,
   loai VARCHAR(20) NOT NULL,
   so_tien DECIMAL(15,2),
   cach_tinh VARCHAR(20) NOT NULL DEFAULT 'FLAT', -- FLAT/PER_NHANKHAU/PER_M2/PER_XE
   so_thang INT DEFAULT 1,
   don_gia_xe_may DECIMAL(15,2),  -- đơn giá/xe máy/tháng (PER_XE); NULL = mặc định 70.000
   don_gia_o_to   DECIMAL(15,2),  -- đơn giá/ô tô/tháng  (PER_XE); NULL = mặc định 1.200.000
   thang_thu DATE,
   han_nop DATE,
   mo_ta TEXT,
   trang_thai VARCHAR(20) NOT NULL DEFAULT 'OPEN',
   CONSTRAINT chk_kt_loai      CHECK (loai IN ('BAT_BUOC','TU_NGUYEN')),
   CONSTRAINT chk_kt_cachtinh  CHECK (cach_tinh IN ('FLAT','PER_NHANKHAU','PER_M2','PER_XE')),
   CONSTRAINT chk_kt_trangthai CHECK (trang_thai IN ('OPEN','CLOSED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. PAYMENTS
CREATE TABLE nop_tien (
      id INT PRIMARY KEY AUTO_INCREMENT,
      khoan_thu_id INT NOT NULL,
      ho_khau_id INT NOT NULL,
      so_tien DECIMAL(15,2) NOT NULL,
      nguoi_thu VARCHAR(100),
      ghi_chu TEXT,
      ngay_nop DATE NOT NULL,
      UNIQUE KEY unique_payment (khoan_thu_id, ho_khau_id),
      FOREIGN KEY (khoan_thu_id) REFERENCES khoan_thu(id) ON DELETE CASCADE,
      FOREIGN KEY (ho_khau_id) REFERENCES ho_khau(id) ON DELETE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. RESIDENCY CHANGES
CREATE TABLE lich_su_bien_dong(
      id INT PRIMARY KEY AUTO_INCREMENT,
      nhan_khau_id INT NOT NULL,
      ho_khau_id INT,
      loai_bien_dong VARCHAR(50) NOT NULL,
      ngay_bien_dong DATE NOT NULL,
      ghi_chu TEXT,
      nguoi_thuc_hien VARCHAR(100),
      FOREIGN KEY (nhan_khau_id) REFERENCES nhan_khau(id) ON DELETE CASCADE,
      FOREIGN KEY (ho_khau_id) REFERENCES ho_khau(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. TEMPORARY RESIDENCY-ABSENCE
CREATE TABLE tam_tru_tam_vang (
      id INT PRIMARY KEY AUTO_INCREMENT,
      nhan_khau_id INT NOT NULL, -- Đã bổ sung INT
      loai VARCHAR(20) NOT NULL,
      tu_ngay DATE NOT NULL,
      den_ngay DATE,                 -- có thể để trống (tạm vắng chưa xác định ngày về)
      dia_chi_tam_tru VARCHAR(255),
      ly_do TEXT,
      trang_thai VARCHAR(20) DEFAULT 'ACTIVE',
      FOREIGN KEY (nhan_khau_id) REFERENCES nhan_khau(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7b. DANH MỤC PHÒNG / CĂN HỘ (cố định của tòa nhà; phòng chưa gắn hộ khẩu = trống)
CREATE TABLE dm_phong (
   id INT PRIMARY KEY AUTO_INCREMENT,
   ma_phong VARCHAR(20) NOT NULL UNIQUE,
   tang INT,
   dien_tich DECIMAL(8,2) NOT NULL DEFAULT 0  -- diện tích cố định theo phòng
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. AUDIT LOG (nhật ký thao tác)
CREATE TABLE audit_log (
      id INT PRIMARY KEY AUTO_INCREMENT,
      username VARCHAR(50),
      hanh_dong VARCHAR(30) NOT NULL,
      doi_tuong VARCHAR(50) NOT NULL,
      mo_ta VARCHAR(500),
      thoi_gian TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- INDEXES
CREATE INDEX idx_nhan_khau_ho_khau ON nhan_khau(ho_khau_id);
CREATE INDEX idx_nhan_khau_hoten   ON nhan_khau(ho_ten);
CREATE INDEX idx_nhan_khau_cccd    ON nhan_khau(cccd);
CREATE INDEX idx_nop_tien_khoan_thu ON nop_tien(khoan_thu_id);
CREATE INDEX idx_nop_tien_ho_khau ON nop_tien(ho_khau_id);
CREATE INDEX idx_nop_tien_ngay    ON nop_tien(ngay_nop);
CREATE INDEX idx_tam_tru_nhan_khau ON tam_tru_tam_vang(nhan_khau_id);
CREATE INDEX idx_audit_thoigian   ON audit_log(thoi_gian);