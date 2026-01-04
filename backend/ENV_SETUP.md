# Hướng dẫn sử dụng file .env

## ✅ Đã cấu hình

Dự án đã được cấu hình để đọc các biến môi trường từ file `.env` thay vì hardcode trong `application.properties`.

## 🚀 Cách sử dụng

### 1. Tạo file .env

Sao chép file `.env.example` thành `.env`:

```bash
cp .env.example .env
```

Hoặc tạo file `.env` mới trong thư mục `backend/` với nội dung:

```env
# Database
DATABASE_URL=jdbc:postgresql://your-database-url/neondb?sslmode=require
DATABASE_USERNAME=your_username
DATABASE_PASSWORD=your_password

# JWT
JWT_SIGNER_KEY=your_jwt_key

# Email
MAIL_USERNAME=your_email@example.com
MAIL_PASSWORD=your_password

# PayOS
PAYOS_CLIENT_ID=your_client_id
PAYOS_API_KEY=your_api_key
PAYOS_CHECKSUM_KEY=your_checksum_key
```

### 2. Cập nhật giá trị

Mở file `.env` và thay thế các giá trị placeholder bằng thông tin thực tế của bạn.

### 3. Chạy ứng dụng

Spring Boot sẽ tự động đọc file `.env` khi khởi động:

```bash
mvn spring-boot:run
```

## 📝 Lưu ý

- ✅ File `.env` đã được thêm vào `.gitignore` - **KHÔNG commit** file này lên git
- ✅ File `.env.example` có thể commit để làm template
- ✅ Nếu không có file `.env`, ứng dụng sẽ sử dụng giá trị mặc định từ `application.properties`
- ✅ Các biến môi trường hệ thống (system environment variables) sẽ override giá trị trong `.env`

## 🔧 Cách hoạt động

1. `DotenvConfig` được đăng ký như một `ApplicationContextInitializer`
2. Nó load file `.env` từ thư mục gốc của project (`backend/.env`)
3. Các giá trị từ `.env` được thêm vào Spring Environment
4. `application.properties` sử dụng syntax `${ENV_VAR:default_value}` để đọc từ environment

## 📋 Danh sách biến môi trường

Xem file `.env.example` để biết danh sách đầy đủ các biến môi trường cần cấu hình.
