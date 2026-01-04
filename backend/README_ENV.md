# Cấu hình môi trường với file .env

Dự án sử dụng file `.env` để lưu các cấu hình nhạy cảm thay vì hardcode trong `application.properties`.

## Cách sử dụng

### 1. Tạo file .env

Sao chép file `.env.example` thành `.env`:

```bash
cp .env.example .env
```

### 2. Cập nhật giá trị trong .env

Mở file `.env` và cập nhật các giá trị phù hợp với môi trường của bạn:

```env
# Database
DATABASE_URL=jdbc:postgresql://your-database-url/neondb?sslmode=require
DATABASE_USERNAME=your_username
DATABASE_PASSWORD=your_password

# JWT
JWT_SIGNER_KEY=your_jwt_signer_key

# Email
MAIL_USERNAME=your_email@example.com
MAIL_PASSWORD=your_email_password

# PayOS
PAYOS_CLIENT_ID=your_payos_client_id
PAYOS_API_KEY=your_payos_api_key
PAYOS_CHECKSUM_KEY=your_payos_checksum_key
```

### 3. Chạy ứng dụng

Spring Boot sẽ tự động đọc file `.env` và sử dụng các giá trị đó.

## Lưu ý

- File `.env` đã được thêm vào `.gitignore` để không commit lên git
- File `.env.example` chứa template và có thể commit lên git
- Nếu không có file `.env`, ứng dụng sẽ sử dụng giá trị mặc định từ `application.properties`
- Các biến môi trường có thể override giá trị trong `.env` nếu được set

## Cấu trúc

- `.env` - File chứa các giá trị thực tế (KHÔNG commit)
- `.env.example` - Template file (CÓ THỂ commit)
- `application.properties` - Chứa giá trị mặc định với syntax `${ENV_VAR:default_value}`
