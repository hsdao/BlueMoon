# FXML Convention – Dự án BlueMoon

## Quy ước đặt tên fx:id

| Loại control | Prefix | Ví dụ |
|---|---|---|
| TextField / PasswordField | txt | txtEmail, txtMatKhau |
| Button | btn | btnDangNhap, btnLuu, btnHuy |
| TableView | tbl | tblKhoanThu, tblHoKhau |
| ComboBox | cmb | cmbLoaiKhoanThu, cmbVaiTro |
| Label | lbl | lblThongBao |
| DatePicker | dtp | dtpNgayNop |
| TextArea | txa | txaGhiChu |
| CheckBox | chk | chkBatBuoc |
| Chart | chart | chartThuTheoThang |

## Kích thước cửa sổ

| Màn hình | Kích thước |
|---|---|
| Đăng nhập, Tạo khoản thu, Thu phí, Tạm trú | 1120 x 645 |
| Dashboard, Thống kê, Hộ khẩu, Nhân khẩu | 1440 x 1024 |

## Theme mặc định

- Theme: PrimerLight (AtlantaFX 2.1.0)
- Set 1 lần duy nhất trong Main.java
- KHÔNG set lại trong Controller

## Lưu ý

- Sai fx:id convention → app crash → P5 sẽ từ chối merge PR