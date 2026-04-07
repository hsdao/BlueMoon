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
   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
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
     dia_chi VARCHAR(255) NOT NULL,
     so_thanh_vien INT DEFAULT 0,
     ngay_tao DATE NOT NULL,
     trang_thai VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
     ghi_chu TEXT
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
      so_dien_thoai VARCHAR(11) NOT NULL UNIQUE,
      trang_thai VARCHAR(20) NOT NULL DEFAULT 'PERMANENT',
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
   thang_thu DATE,
   han_nop DATE,
   mo_ta TEXT,
   trang_thai VARCHAR(20) NOT NULL DEFAULT 'OPEN'
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
      den_ngay DATE NOT NULL,
      dia_chi_tam_tru VARCHAR(255),
      ly_do TEXT,
      trang_thai VARCHAR(20) DEFAULT 'ACTIVE',
      FOREIGN KEY (nhan_khau_id) REFERENCES nhan_khau(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- INDEXES
CREATE INDEX idx_nhan_khau_ho_khau ON nhan_khau(ho_khau_id);
CREATE INDEX idx_nop_tien_khoan_thu ON nop_tien(khoan_thu_id);
CREATE INDEX idx_nop_tien_ho_khau ON nop_tien(ho_khau_id);
CREATE INDEX idx_tam_tru_nhan_khau ON tam_tru_tam_vang(nhan_khau_id);