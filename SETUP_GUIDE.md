# Hướng dẫn Setup PayOS Integration

## ✅ Đã hoàn thành

### Backend (Spring Boot)
- ✅ Tích hợp PayOS Service
- ✅ PayOS Configuration
- ✅ Payment Controller với endpoints PayOS
- ✅ Webhook handler cho PayOS
- ✅ Error handling

### Frontend (Next.js)
- ✅ Cấu trúc Next.js App Router
- ✅ Trang chủ nhập thông tin đơn hàng
- ✅ Trang checkout tạo link thanh toán
- ✅ Trang success/cancel sau thanh toán
- ✅ UI với Tailwind CSS

## 🚀 Bắt đầu sử dụng

### 1. Backend Setup

#### Cấu hình PayOS credentials

Mở file `backend/src/main/resources/application.properties` và cập nhật:

```properties
payos.client-id=YOUR_PAYOS_CLIENT_ID
payos.api-key=YOUR_PAYOS_API_KEY
payos.checksum-key=YOUR_PAYOS_CHECKSUM_KEY
```

Lấy credentials từ: https://payos.vn

#### Chạy backend

```bash
cd backend
mvn spring-boot:run
```

Backend sẽ chạy tại: http://localhost:8080

### 2. Frontend Setup

#### Cài đặt dependencies

```bash
cd frontend
npm install
```

#### Cấu hình môi trường

Tạo file `frontend/.env.local`:

```
NEXT_PUBLIC_API_URL=http://localhost:8080
```

#### Chạy frontend

```bash
npm run dev
```

Frontend sẽ chạy tại: http://localhost:3000

## 📝 Cách test

1. **Tạo một order** trong hệ thống (qua API hoặc database)
2. **Mở frontend** tại http://localhost:3000
3. **Nhập thông tin**:
   - Mã đơn hàng: `orderId` từ bước 1
   - Số tiền: số tiền của đơn hàng (VND)
   - Mô tả: (tùy chọn)
4. **Click "Tạo Link Thanh Toán"**
5. **Sẽ được redirect** đến PayOS để test thanh toán

## 🔗 API Endpoints

### Tạo link thanh toán PayOS
```
POST /payments/payos/create
Content-Type: application/json

{
  "orderId": "order-uuid",
  "description": "Mô tả đơn hàng",
  "returnUrl": "http://localhost:3000/payment/success",
  "cancelUrl": "http://localhost:3000/payment/cancel"
}
```

### Webhook PayOS
```
POST /payments/payos/webhook
```
(PayOS sẽ gọi endpoint này tự động)

## 📁 Cấu trúc files

### Backend
```
backend/
├── src/main/java/com/example/backend/
│   ├── configuration/
│   │   └── PayOSConfig.java          # Cấu hình PayOS
│   ├── controller/
│   │   └── PaymentController.java    # API endpoints
│   ├── service/
│   │   └── PayOSService.java         # Logic xử lý PayOS
│   └── dto/
│       ├── request/
│       │   └── PayOSPaymentRequest.java
│       └── response/
│           └── PayOSPaymentResponse.java
└── src/main/resources/
    └── application.properties        # Cấu hình PayOS credentials
```

### Frontend
```
frontend/
├── app/
│   ├── page.tsx                      # Trang chủ
│   ├── checkout/
│   │   └── page.tsx                  # Tạo link thanh toán
│   └── payment/
│       ├── success/
│       │   └── page.tsx              # Thanh toán thành công
│       └── cancel/
│           └── page.tsx              # Hủy thanh toán
├── package.json
└── .env.local                        # Cấu hình API URL
```

## ⚠️ Lưu ý quan trọng

1. **PayOS Credentials**: Cần đăng ký tài khoản PayOS và lấy credentials từ dashboard
2. **Webhook URL**: Cần cấu hình webhook URL trong PayOS Dashboard trỏ đến backend
3. **Order Code**: PayOS yêu cầu orderCode là số nguyên duy nhất, service tự động generate
4. **CORS**: Đảm bảo backend cho phép CORS từ frontend (nếu cần)

## 🐛 Troubleshooting

### Backend không compile
- Kiểm tra Java version (cần Java 21)
- Chạy `mvn clean install`

### Frontend không kết nối được backend
- Kiểm tra `NEXT_PUBLIC_API_URL` trong `.env.local`
- Kiểm tra backend đang chạy
- Kiểm tra CORS settings

### PayOS trả về lỗi
- Kiểm tra credentials trong `application.properties`
- Kiểm tra format của orderCode
- Xem logs backend để biết chi tiết

## 📚 Tài liệu tham khảo

- [PayOS Documentation](https://payos.vn/docs)
- [Next.js Documentation](https://nextjs.org/docs)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
