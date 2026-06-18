# Hướng dẫn cài đặt – BlueMoon

Phần mềm quản lý thu phí chung cư BlueMoon (JavaFX 21 + MySQL).

## 1. Yêu cầu môi trường
- **JDK 21** (kiểm tra: `java -version`)
- **Apache Maven 3.8+** (kiểm tra: `mvn -version`)
- **MySQL Server 8.x** đang chạy
- Hệ điều hành: Windows / Linux / macOS

## 2. Tạo cơ sở dữ liệu
Mở MySQL client và chạy lần lượt:
```sql
SOURCE sql/create_tables.sql;   -- tạo database "bluemoon" + các bảng
SOURCE sql/seed_data.sql;       -- nạp dữ liệu mẫu + tài khoản mặc định
```
> Nếu DB đã tồn tại từ phiên bản cũ và **không muốn mất dữ liệu**, chạy thêm
> `sql/migration_v1_1.sql` (thêm các cột mới: diện tích, số xe, cách tính phí…).

## 3. Cấu hình kết nối
Copy file mẫu rồi điền thông tin MySQL của máy bạn:
```bash
cp src/main/resources/config.properties.example src/main/resources/config.properties
```
Sửa `config.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/bluemoon?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=<tài khoản MySQL>
db.password=<mật khẩu MySQL>
```
> `config.properties` đã được `.gitignore` để không lộ mật khẩu lên Git.

## 4. Chạy ứng dụng
```bash
mvn clean javafx:run
```

## 5. Chạy kiểm thử đơn vị
```bash
mvn test
```

## 6. Sinh tài liệu API (Javadoc)
```bash
mvn javadoc:javadoc
# Kết quả: target/site/apidocs/index.html
```

## 7. Đóng gói (tùy chọn)
```bash
mvn clean package      # tạo target/BlueMoon-1.0-SNAPSHOT.jar
```

## Tài khoản mặc định
| Username | Password   | Vai trò |
|----------|------------|---------|
| `admin`  | `admin123` | ADMIN   |
| `staff`  | `staff123` | STAFF   |

> Mật khẩu trong `seed_data.sql` để dạng thô cho dễ kiểm thử; **lần đăng nhập đầu tiên hệ
> thống sẽ tự băm lại (salted SHA-256)** và lưu dạng băm. Nên đổi mật khẩu sau khi đăng nhập.

## Sự cố thường gặp
- **Không kết nối được DB** → kiểm tra MySQL đang chạy, đúng user/password trong `config.properties`.
- **Không đăng nhập được** → đảm bảo đã chạy `seed_data.sql` (tạo tài khoản mặc định).
- **Lỗi font/diacritics khi xuất PDF** → cài font hệ thống (Arial/Times trên Windows; DejaVu trên Linux).
