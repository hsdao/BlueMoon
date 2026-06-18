# Hướng dẫn sử dụng – BlueMoon

## 1. Đăng nhập / Đăng ký / Đổi mật khẩu
- **Đăng nhập**: nhập tên đăng nhập, mật khẩu, chọn vai trò (ADMIN/STAFF) rồi bấm *Đăng nhập*.
  Nút 👁 cho phép hiện/ẩn mật khẩu.
- **Đăng ký tài khoản**: bấm *Đăng ký tài khoản* ở màn hình đăng nhập — tài khoản tạo công khai luôn là **STAFF**; quyền **ADMIN** chỉ được cấp trong *Quản lý tài khoản* (do ADMIN thực hiện).
- **Đổi mật khẩu**: menu trái → *Đổi mật khẩu* (nhập mật khẩu cũ + mật khẩu mới).

## 2. Phân quyền
- **ADMIN (Tổ trưởng)**: toàn quyền, gồm *Quản lý tài khoản* và thêm/sửa/xóa hộ khẩu, nhân khẩu.
- **STAFF (Kế toán)**: tập trung nghiệp vụ thu phí; bị ẩn các nút quản trị/sửa-xóa cư dân.

## 3. Quản lý hộ khẩu & nhân khẩu
- **Hộ khẩu**: thêm/sửa/xóa; **diện tích (m²)** lấy tự động theo phòng đã chọn, nhập **số xe máy/ô tô** (dùng để tính phí theo định mức).
- **Nhân khẩu**: thông tin nhân thân (dân tộc, tôn giáo, nghề nghiệp, nơi làm việc, quê quán, địa chỉ thường trú).
- **Biến động nhân khẩu**: ghi nhận nhập khẩu/chuyển đi/khai sinh/khai tử… (lý do là tùy chọn), hệ thống tự ghi log lịch sử và cập nhật trạng thái nhân khẩu.
- **Tạm trú / Tạm vắng**: đăng ký và theo dõi.

## 4. Quản lý khoản thu (Kế toán)
Khi tạo khoản thu cần chọn **Cách tính phí**:
| Cách tính | Ý nghĩa | "Số tiền" nhập là |
|-----------|---------|-------------------|
| Số tiền cố định (FLAT) | Một mức cố định, hoặc nhập tay từng hộ (điện/nước thu hộ) | Số tiền cố định (có thể để trống) |
| Theo số nhân khẩu | Phí = đơn giá × số nhân khẩu × số tháng (vd vệ sinh 6.000đ/người/tháng) | Đơn giá/người/tháng |
| Theo diện tích | Phí = đơn giá × m² × số tháng (vd phí quản lý 7.000đ/m²/tháng) | Đơn giá/m²/tháng |
| Theo số xe (gửi xe) | 70.000đ/xe máy + 1.200.000đ/ô tô × số tháng | (không cần) |

> Không thể **xóa** khoản thu đã có hộ nộp tiền (bảo toàn lịch sử). Muốn ngừng thu thì đổi trạng thái sang `CLOSED`.

## 5. Thu phí
- Chọn **khoản thu** và **hộ khẩu** → hệ thống **tự tính** số tiền phải nộp theo cách tính.
- Nhập người thu, ngày nộp, ghi chú → *Ghi nhận*.
- Mỗi hộ chỉ nộp **một lần** cho mỗi khoản thu (chống trùng).

## 6. Thống kê & Báo cáo
- Chọn khoản thu → xem bảng từng hộ: **Phải nộp / Đã nộp / Trạng thái**, biểu đồ tỉ lệ đã/chưa nộp.
- **Xuất Excel** (.xlsx) và **Xuất PDF** báo cáo.

## 7. Tìm kiếm & Cài đặt
- **Tìm kiếm** toàn cục: hộ khẩu, nhân khẩu, khoản thu (có bộ lọc nâng cao).
- **Cài đặt**: đổi giao diện sáng (PrimerLight / NordLight / CupertinoLight).
