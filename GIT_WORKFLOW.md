# Git Workflow – Dự án BlueMoon

## Cấu trúc nhánh

- main: bản chính thức, chỉ merge cuối dự án
- dev: nhánh tích hợp, merge từ các nhánh feature
- feature/p1-db: P1 phụ trách
- feature/p2-login: P2 phụ trách
- feature/p3-khoanthu: P3 phụ trách
- feature/p4-hokhau: P4 phụ trách
- feature/p5-tamtru: P5 phụ trách

## Quy trình hàng ngày

1. Đầu buổi: git pull về nhánh của mình
2. Code xong 1 việc nhỏ: commit ngay
3. Cuối buổi: push lên GitHub
4. Xong 1 tính năng: tạo Pull Request về dev
5. Sau khi PR được merge: pull code mới từ dev về

## Commit message

- feat: thêm tính năng mới
- fix: sửa lỗi
- style: thay đổi giao diện
- refactor: cải thiện code
- docs: cập nhật tài liệu
- test: thêm test case
- chore: cập nhật cấu hình

## Quy định bắt buộc

- KHÔNG push thẳng lên main hoặc dev
- KHÔNG dùng git push --force
- Commit message phải rõ ràng, không dùng: fix bug, update, done
- Pull Request phải được P5 review trước khi merge
- P5 review trong vòng 24 giờ sau khi nhận PR