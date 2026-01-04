# Hướng dẫn tích hợp PayOS

## Tổng quan

Dự án đã được tích hợp PayOS để xử lý thanh toán tự động. Bao gồm:
- Backend: Spring Boot với PayOSService
- Frontend: Next.js với giao diện test thanh toán

## Backend Setup

### 1. Cấu hình PayOS trong `application.properties`

Cập nhật file `backend/src/main/resources/application.properties`:

```properties
# PayOS Configuration
payos.client-id=YOUR_PAYOS_CLIENT_ID
payos.api-key=YOUR_PAYOS_API_KEY
payos.checksum-key=YOUR_PAYOS_CHECKSUM_KEY
payos.base-url=https://api-merchant.payos.vn
```

Lấy thông tin credentials từ [PayOS Dashboard](https://payos.vn)

### 2. API Endpoints

#### Tạo link thanh toán
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

Response:
```json
{
  "code": 1000,
  "result": {
    "checkoutUrl": "https://pay.payos.vn/web/...",
    "orderCode": "123456789",
    "message": "Tạo link thanh toán thành công"
  }
}
```

#### Webhook nhận kết quả thanh toán
```
POST /payments/payos/webhook
Content-Type: application/json

{
  "code": "00",
  "desc": "Success",
  "data": {
    "orderCode": 123456789,
    "amount": 100000,
    "description": "...",
    "accountNumber": "...",
    "reference": "...",
    "transactionDateTime": "...",
    "currency": "VND",
    "paymentLinkId": "...",
    "code": "00",
    "desc": "Success",
    "counterAccountBankId": null,
    "counterAccountBankName": null,
    "counterAccountName": null,
    "counterAccountNumber": null,
    "virtualAccountName": null,
    "virtualAccountNumber": null
  },
  "signature": "..."
}
```

## Frontend Setup

### 1. Cài đặt dependencies

```bash
cd frontend
npm install
```

### 2. Cấu hình môi trường

Tạo file `frontend/.env.local`:

```
NEXT_PUBLIC_API_URL=http://localhost:8080
```

### 3. Chạy development server

```bash
npm run dev
```

Frontend sẽ chạy tại [http://localhost:3000](http://localhost:3000)

## Luồng thanh toán

1. **Trang chủ** (`/`): Nhập thông tin đơn hàng (orderId, amount, description)
2. **Checkout** (`/checkout`): Tự động tạo link PayOS và redirect
3. **Thanh toán PayOS**: Người dùng thanh toán trên PayOS
4. **Success** (`/payment/success`): PayOS redirect về khi thanh toán thành công
5. **Cancel** (`/payment/cancel`): PayOS redirect về khi hủy thanh toán

## Testing

### Test với đơn hàng thật

1. Tạo một order trong hệ thống (qua API hoặc database)
2. Lấy `orderId` của order đó
3. Vào frontend, nhập `orderId` và `amount`
4. Click "Tạo Link Thanh Toán"
5. Sẽ được redirect đến PayOS để test thanh toán

### Test với PayOS Sandbox

PayOS cung cấp môi trường sandbox để test. Sử dụng credentials từ PayOS Dashboard (môi trường test).

## Lưu ý quan trọng

1. **Webhook URL**: Cần cấu hình webhook URL trong PayOS Dashboard trỏ đến:
   ```
   https://your-domain.com/payments/payos/webhook
   ```

2. **Return URLs**: Đảm bảo `returnUrl` và `cancelUrl` là các URL công khai có thể truy cập được

3. **Order Code**: PayOS yêu cầu `orderCode` là số nguyên duy nhất. Service tự động generate từ `orderId`

4. **Checksum**: Service tự động tạo checksum theo chuẩn PayOS

5. **CORS**: Đảm bảo backend cho phép CORS từ frontend domain

## Troubleshooting

### Lỗi "Payment failed"
- Kiểm tra PayOS credentials trong `application.properties`
- Kiểm tra format của `orderCode` (phải là số nguyên)
- Kiểm tra logs trong backend để xem chi tiết lỗi

### Frontend không kết nối được backend
- Kiểm tra `NEXT_PUBLIC_API_URL` trong `.env.local`
- Kiểm tra backend đang chạy tại port 8080
- Kiểm tra CORS configuration

### Webhook không nhận được
- Kiểm tra webhook URL trong PayOS Dashboard
- Kiểm tra endpoint `/payments/payos/webhook` có thể truy cập công khai
- Kiểm tra logs backend
