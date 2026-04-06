-- DỮ LIỆU MẪU ĐẦY ĐỦ CHO TUẦN 1
USE bluemoon;

-- 1. Danh mục Quan hệ
INSERT INTO quan_he (ten_quan_he) VALUES
    ('Chủ hộ'), ('Vợ'), ('Chồng'), ('Con trai'), ('Con gái'), ('Bố'), ('Mẹ');

-- 2. Hộ khẩu (Tạo 5 hộ khẩu mẫu - chu_ho_id để tạm thời là NULL)
INSERT INTO ho_khau (ma_ho, so_dien_thoai_chu_ho, so_thanh_vien, dia_chi, ngay_tao) VALUES
    ('HK001', '0981001001', '7', 'Phòng 101-A', '2024-01-01'),
    ('HK002', '0981001002', '2', 'Phòng 102-A', '2024-01-05'),
    ('HK003', '0981001003', '1', 'Phòng 201-B', '2024-02-10'),
    ('HK004', '0981001004', '5', 'Phòng 305-C', '2024-03-15'),
    ('HK005', '0981001005', '3', 'Phòng 402-D', '2024-04-20');

-- 3. Nhân khẩu (Tạo 10 nhân khẩu phân bổ vào 5 hộ trên)
INSERT INTO nhan_khau (ho_khau_id, ho_ten, ngay_sinh, gioi_tinh, cccd, quan_he_id, so_dien_thoai) VALUES
    (1, 'Nguyễn Văn Anh', '1980-05-15', 'Nam', '001080123456', 1, '0981001001'), -- Chủ hộ 1
    (1, 'Lê Thị Bình', '1985-10-20', 'Nữ', '001085987654', 2, '0981001011'),
    (1, 'Nguyễn Văn Cường', '2010-02-10', 'Nam', NULL, 4, '0981001012'),
    (2, 'Trần Văn Dũng', '1975-01-01', 'Nam', '001075111222', 1, '0981001002'), -- Chủ hộ 2
    (2, 'Phạm Thị Hoa', '1978-06-12', 'Nữ', '001078333444', 2, '0981001021'),
    (3, 'Lý Thị Giang', '1990-03-03', 'Nữ', '001090555666', 1, '0981001003'), -- Chủ hộ 3
    (4, 'Vũ Văn Hùng', '1988-12-25', 'Nam', '001088777888', 1, '0981001004'), -- Chủ hộ 4
    (4, 'Vũ Thị Lan', '2015-08-08', 'Nữ', NULL, 5, '0981001041'),
    (5, 'Đỗ Văn Minh', '1965-04-30', 'Nam', '001065999000', 1, '0981001005'), -- Chủ hộ 5
    (5, 'Bùi Thị Nụ', '1968-11-11', 'Nữ', '001068121212', 2, '0981001051');

-- 4. Cập nhật chu_ho_id cho bảng ho_khau (Ràng buộc logic)
UPDATE ho_khau SET chu_ho_id = 1 WHERE id = 1;
UPDATE ho_khau SET chu_ho_id = 4 WHERE id = 2;
UPDATE ho_khau SET chu_ho_id = 6 WHERE id = 3;
UPDATE ho_khau SET chu_ho_id = 7 WHERE id = 4;
UPDATE ho_khau SET chu_ho_id = 9 WHERE id = 5;

-- 5. Khoản thu (3 khoản thu mẫu)
INSERT INTO khoan_thu (ma_khoan, ten_khoan, loai, so_tien, thang_thu, trang_thai) VALUES
    ('QL10', 'Phí quản lý tháng 10', 'BAT_BUOC', 200000, '2024-10-01', 'OPEN'),
    ('VS10', 'Phí vệ sinh tháng 10', 'BAT_BUOC', 50000, '2024-10-01', 'OPEN'),
    ('VNN24', 'Quỹ vì người nghèo 2024', 'TU_NGUYEN', NULL, '2024-10-01', 'OPEN');