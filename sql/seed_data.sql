-- =====================================================================
-- DỮ LIỆU MẪU ĐẦY ĐỦ (sinh tự động, nhất quán với toàn bộ tính năng)
-- Chạy SAU create_tables.sql.
-- =====================================================================
USE bluemoon;
SET NAMES utf8mb4;

-- 1) Danh mục quan hệ
INSERT INTO quan_he (ten_quan_he) VALUES
    ('Chủ hộ'),('Vợ'),('Chồng'),('Con trai'),('Con gái'),('Bố'),('Mẹ'),('Cháu');

-- 2) Danh mục phòng: 12 tầng x 8 phòng = 96 phòng (diện tích cố định)
INSERT INTO dm_phong (ma_phong, tang, dien_tich)
SELECT CONCAT('P', t.f, LPAD(r.n,2,'0')), t.f, 50 + r.n*6
FROM (SELECT 1 f UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6
      UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10 UNION SELECT 11 UNION SELECT 12) t
CROSS JOIN (SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
            UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8) r;

-- 3) Hộ khẩu: chiếm 1/3 số phòng (số còn lại để TRỐNG)
INSERT INTO ho_khau (ma_ho, so_dien_thoai_chu_ho, dien_tich, so_xe_may, so_o_to, dia_chi, ngay_tao, trang_thai)
SELECT ma_phong, CONCAT('08', LPAD(id,8,'0')), dien_tich, MOD(id,3), MOD(id,2),
       CONCAT('Phòng ', ma_phong), '2024-01-15', 'ACTIVE'
FROM dm_phong WHERE MOD(id,3)=0;

-- 4) Nhân khẩu: 2-3 người/hộ (hộ id chẵn có 3 người); người s=1 là chủ hộ
INSERT INTO nhan_khau (ho_khau_id, ho_ten, ngay_sinh, gioi_tinh, cccd, quan_he_id, so_dien_thoai, trang_thai)
SELECT h.id,
  CONCAT(ELT(1+MOD(h.id+seq.s,5),'Nguyễn','Trần','Lê','Phạm','Hoàng'),' ',
         ELT(1+MOD(h.id*seq.s,4),'Văn','Thị','Hữu','Ngọc'),' ',
         ELT(1+MOD(h.id+seq.s*7,6),'An','Bình','Cường','Dũng','Hà','Minh')),
  DATE_ADD('1965-01-01', INTERVAL ((h.id*131 + seq.s*37) % 18000) DAY),
  IF(MOD(seq.s,2)=0,'Nữ','Nam'),
  LPAD((h.id*1000 + seq.s), 12, '0'),
  IF(seq.s=1, 1, 2 + MOD(h.id+seq.s,6)),
  CONCAT('09', LPAD(h.id*10 + seq.s, 8, '0')),
  'PERMANENT'
FROM ho_khau h
CROSS JOIN (SELECT 1 s UNION ALL SELECT 2 UNION ALL SELECT 3) seq
WHERE seq.s <= 2 OR MOD(h.id,2)=0;

-- 5) Gán chủ hộ + SĐT chủ hộ + số thành viên (tự tính)
UPDATE ho_khau h
   SET chu_ho_id = (SELECT MIN(n.id) FROM nhan_khau n WHERE n.ho_khau_id=h.id AND n.quan_he_id=1);
UPDATE ho_khau h
   LEFT JOIN nhan_khau n ON n.id = h.chu_ho_id
   SET h.so_dien_thoai_chu_ho = COALESCE(n.so_dien_thoai, h.so_dien_thoai_chu_ho);
UPDATE ho_khau h
   SET h.so_thanh_vien = (SELECT COUNT(*) FROM nhan_khau n WHERE n.ho_khau_id=h.id);

-- 6) Khoản thu: đủ các cách tính
INSERT INTO khoan_thu (ma_khoan, ten_khoan, loai, so_tien, cach_tinh, so_thang, don_gia_xe_may, don_gia_o_to, thang_thu, han_nop, trang_thai) VALUES
    ('QL2026',  'Phí quản lý 2026',        'BAT_BUOC', 7000,   'PER_M2',       1, NULL,  NULL,    '2026-01-01', '2026-01-31', 'OPEN'),
    ('VS2026',  'Phí vệ sinh 2026',        'BAT_BUOC', 6000,   'PER_NHANKHAU', 1, NULL,  NULL,    '2026-01-01', '2026-01-31', 'OPEN'),
    ('GX2026',  'Phí gửi xe 2026',         'BAT_BUOC', NULL,   'PER_XE',       1, 70000, 1200000, '2026-01-01', '2026-01-31', 'OPEN'),
    ('DV2026',  'Phí dịch vụ 2026',        'BAT_BUOC', 3000,   'PER_M2',       1, NULL,  NULL,    '2026-01-01', '2026-01-31', 'OPEN'),
    ('DN2026',  'Thu hộ điện nước T1',     'BAT_BUOC', NULL,   'FLAT',         1, NULL,  NULL,    '2026-01-01', '2026-01-31', 'OPEN'),
    ('QUY2026', 'Quỹ vì người nghèo 2026', 'TU_NGUYEN', NULL,  'FLAT',         1, NULL,  NULL,    '2026-01-01', NULL,         'OPEN');

-- 7) Nộp tiền: ~60% hộ đã nộp các khoản BẮT BUỘC (tạo công nợ hỗn hợp xanh/đỏ)
INSERT IGNORE INTO nop_tien (khoan_thu_id, ho_khau_id, so_tien, nguoi_thu, ghi_chu, ngay_nop)
SELECT kt.id, hk.id,
  CASE kt.cach_tinh
    WHEN 'PER_M2'       THEN kt.so_tien * hk.dien_tich
    WHEN 'PER_NHANKHAU' THEN kt.so_tien * hk.so_thanh_vien
    WHEN 'PER_XE'       THEN COALESCE(kt.don_gia_xe_may,70000)*hk.so_xe_may
                            + COALESCE(kt.don_gia_o_to,1200000)*hk.so_o_to
    ELSE COALESCE(kt.so_tien, 150000)
  END,
  IF(MOD(hk.id,2)=0,'admin','staff'),
  'Dữ liệu mẫu',
  DATE_SUB(CURDATE(), INTERVAL MOD(hk.id*7, 50) DAY)   -- rải ~50 ngày gần đây (có cả tháng này)
FROM ho_khau hk
JOIN khoan_thu kt ON kt.trang_thai='OPEN' AND kt.loai='BAT_BUOC'
WHERE MOD(hk.id,5) < 3;

-- 8) Một vài hộ đóng góp tự nguyện
INSERT IGNORE INTO nop_tien (khoan_thu_id, ho_khau_id, so_tien, nguoi_thu, ghi_chu, ngay_nop)
SELECT (SELECT id FROM khoan_thu WHERE ma_khoan='QUY2026'), hk.id,
       50000 * (1+MOD(hk.id,4)), 'admin', 'Ủng hộ tự nguyện', DATE_SUB(CURDATE(), INTERVAL 8 DAY)
FROM ho_khau hk WHERE MOD(hk.id,4)=0;

-- 9) Tạm trú / Tạm vắng (10 bản ghi)
INSERT INTO tam_tru_tam_vang (nhan_khau_id, loai, tu_ngay, den_ngay, dia_chi_tam_tru, ly_do, trang_thai)
SELECT nk.id,
       IF(RAND()<0.5,'Tạm trú','Tạm vắng'),
       DATE_ADD('2025-06-01', INTERVAL FLOOR(RAND()*200) DAY),
       IF(RAND()<0.5, DATE_ADD('2026-01-01', INTERVAL FLOOR(RAND()*120) DAY), NULL),
       ELT(FLOOR(RAND()*4)+1,'Quận Cầu Giấy, Hà Nội','Quận 1, TP.HCM','TP. Đà Nẵng','Tỉnh Hải Dương'),
       ELT(FLOOR(RAND()*4)+1,'Đi công tác','Về quê','Đi học','Thăm người thân'),
       'ACTIVE'
FROM nhan_khau nk ORDER BY RAND() LIMIT 10;

-- 10) Lịch sử biến động nhân khẩu (12 bản ghi)
INSERT INTO lich_su_bien_dong (nhan_khau_id, ho_khau_id, loai_bien_dong, ngay_bien_dong, ghi_chu, nguoi_thuc_hien)
SELECT nk.id, nk.ho_khau_id,
       ELT(FLOOR(RAND()*3)+1,'Thêm nhân khẩu','Chuyển đi','Khai tử'),
       DATE_ADD('2024-06-01', INTERVAL FLOOR(RAND()*500) DAY),
       'Dữ liệu mẫu', 'Ban quản trị'
FROM nhan_khau nk WHERE nk.ho_khau_id IS NOT NULL ORDER BY RAND() LIMIT 12;

-- 11) Tài khoản (mật khẩu thô; tự băm khi đăng nhập lần đầu)
INSERT INTO users (username, password, role, full_name) VALUES
    ('admin', 'admin123', 'ADMIN', 'Quản trị viên'),
    ('staff', 'staff123', 'STAFF', 'Nhân viên thu phí');

-- 12. Nhật ký thao tác mẫu (để màn Nhật ký có sẵn dữ liệu khi demo)
INSERT INTO audit_log (username, hanh_dong, doi_tuong, mo_ta, thoi_gian) VALUES
    ('admin','THEM','Hộ khẩu','Thêm hộ + chủ hộ',            DATE_SUB(NOW(), INTERVAL 3 DAY)),
    ('admin','THEM','Khoản thu','Thêm: Phí quản lý 2026',     DATE_SUB(NOW(), INTERVAL 3 DAY)),
    ('staff','THU_PHI','Nộp tiền','Thu phí quản lý',          DATE_SUB(NOW(), INTERVAL 2 DAY)),
    ('admin','DOI_QUYEN','Tài khoản','staff -> STAFF',        DATE_SUB(NOW(), INTERVAL 1 DAY)),
    ('staff','THU_PHI','Nộp tiền','Thu phí vệ sinh',          DATE_SUB(NOW(), INTERVAL 1 DAY));
