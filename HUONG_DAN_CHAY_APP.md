# HƯỚNG DẪN CÀI ĐẶT VÀ CHẠY ỨNG DỤNG BLUEMOON

## YÊU CẦU PHẦN MỀM

| Phần mềm | Phiên bản | Link tải |
|----------|-----------|----------|
| Java JDK | 21 | https://www.oracle.com/java/technologies/downloads/#java21 |
| IntelliJ IDEA | Bất kỳ (Community hoặc Ultimate) | https://www.jetbrains.com/idea/download |
| MySQL Server | 8.x | https://dev.mysql.com/downloads/installer/ |
| MySQL Workbench | Bất kỳ | Cài kèm MySQL Installer |

---

## BƯỚC 1: CÀI ĐẶT MySQL

1. Tải **MySQL Installer** về và cài đặt
2. Trong quá trình cài, chọn cài cả **MySQL Server** và **MySQL Workbench**
3. Đặt **Root Password** là: `root` (hoặc tùy ý , nhớ để đổi lại trong config)
4. Port mặc định: `3306` — giữ nguyên
Lưu ý: Nếu password mysql khác trong config thì nhớ sửa lại trong config thành mật khẩu mysql của mình

---

## BƯỚC 2: TẠO DATABASE

1. Mở **MySQL Workbench** → kết nối vào localhost
2. Vào menu **File → Open SQL Script** → chọn file:
   ```
   sql\create_tables.sql
   ```
3. Nhấn **Ctrl + Shift + Enter** để chạy toàn bộ script
4. Làm tương tự với file:
   ```
   sql\seed_data.sql
   ```
5. Kiểm tra thành công bằng lệnh:
   ```sql
   USE bluemoon;
   SHOW TABLES;
   SELECT * FROM users;
   ```
   Phải thấy 2 tài khoản: `admin` và `staff`

> **Lưu ý:** Nếu root password của bạn khác `root`, mở file `src/main/resources/config.properties` và sửa dòng `db.password=` thành đúng password của bạn.

---

## BƯỚC 3: MỞ PROJECT TRONG INTELLIJ

1. Mở **IntelliJ IDEA**
2. Chọn **File → Open** → tìm đến thư mục project (thư mục có file `pom.xml`)
3. IntelliJ sẽ tự nhận ra đây là Maven project và hỏi **"Trust project?"** → chọn **Trust**
4. Chờ IntelliJ tải xong dependencies (thanh tiến trình góc dưới phải) — lần đầu có thể mất 2-5 phút

---

## BƯỚC 4: CHẠY ỨNG DỤNG

1. Trong cây thư mục bên trái, tìm file:
   ```
   src/main/java/application/Launcher.java
   ```
2. Chuột phải vào file → chọn **Run 'Launcher'**
3. Màn hình đăng nhập sẽ hiện ra

> **Quan trọng:** Chạy file `Launcher.java`, KHÔNG phải `Main.java`

---

## BƯỚC 5: ĐĂNG NHẬP

| Tài khoản | Mật khẩu | Role |
|-----------|----------|------|
| `admin` | `admin123` | ADMIN |
| `staff` | `staff123` | STAFF |

Tài khoản **ADMIN** có đầy đủ quyền (xem, thêm, sửa, xóa, xuất báo cáo).  
Tài khoản **STAFF** bị ẩn chức năng Quản lý Tài Khoản.

---

## XỬ LÝ LỖI THƯỜNG GẶP

### Lỗi: "Could not find or load main class application.Main"
→ Chạy `Launcher.java` thay vì `Main.java`

### Lỗi: "Communications link failure" hoặc "Cannot connect to database"
→ MySQL chưa chạy. Mở MySQL Workbench kiểm tra kết nối trước.  
→ Kiểm tra password trong `src/main/resources/config.properties`

### Lỗi: "Sai tài khoản hoặc mật khẩu" khi đăng nhập
→ Chưa chạy `seed_data.sql` hoặc chưa chọn Role trong dropdown  
→ Kiểm tra `SELECT * FROM users;` có dữ liệu chưa

### Lỗi path chứa tiếng Việt (WindowsPathParser)
→ Copy project ra thư mục không có tiếng Việt, ví dụ: `C:\Projects\BlueMoon`

### Màn hình console hiện WARNING về JavaFX
→ Bình thường, bỏ qua. App vẫn chạy được.

---

## CẤU TRÚC CHỨC NĂNG

| Chức năng | Màn hình | ADMIN | STAFF |
|-----------|----------|-------|-------|
| Dashboard tổng quan | Dashboard | ✅ | ✅ |
| Quản lý hộ khẩu | Hộ Khẩu | ✅ | ✅ |
| Quản lý nhân khẩu | Nhân Khẩu | ✅ | ✅ |
| Quản lý khoản thu | Khoản Thu | ✅ | ✅ |
| Thu phí | Thu Phí | ✅ | ✅ |
| Thống kê & xuất báo cáo | Thống Kê | ✅ | ✅ |
| Tạm trú / Tạm vắng | Tạm Trú | ✅ | ✅ |
| Lịch sử biến động | Lịch Sử BD | ✅ | ✅ |
| Lịch sử nộp tiền | Lịch Sử NT | ✅ | ✅ |
| Tìm kiếm tổng hợp | Tìm Kiếm | ✅ | ✅ |
| Quản lý tài khoản | Quản Lý TK | ✅ | ❌ |
| Đổi mật khẩu | Cài Đặt | ✅ | ✅ |
