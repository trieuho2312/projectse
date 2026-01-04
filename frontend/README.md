# Frontend - PayOS Payment Integration

Frontend Next.js để test tích hợp thanh toán PayOS.

## Cài đặt

1. Cài đặt dependencies:
```bash
npm install
```

2. Tạo file `.env.local` từ `.env.local.example` và điền thông tin:
```
NEXT_PUBLIC_API_URL=http://localhost:8080
PAYOS_CLIENT_ID=your_payos_client_id
PAYOS_API_KEY=your_payos_api_key
PAYOS_CHECKSUM_KEY=your_payos_checksum_key
```

3. Chạy development server:
```bash
npm run dev
```

4. Mở trình duyệt tại [http://localhost:3000](http://localhost:3000)

## Cấu trúc

- `/` - Trang chủ để nhập thông tin đơn hàng
- `/checkout` - Tạo link thanh toán PayOS và redirect
- `/payment/success` - Trang thành công sau khi thanh toán
- `/payment/cancel` - Trang hủy thanh toán

## Lưu ý

- Đảm bảo backend đang chạy tại `http://localhost:8080` (hoặc cập nhật `NEXT_PUBLIC_API_URL`)
- Cần có thông tin PayOS credentials từ [PayOS Dashboard](https://payos.vn)
