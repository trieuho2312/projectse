# Quick Start Guide - Hướng Dẫn Nhanh

Hướng dẫn nhanh để chạy dự án trong 5 phút.

## ⚡ Bước 1: Cài Đặt Dependencies

### Backend
```bash
cd backend
.\mvnw.cmd clean install  # Windows
# hoặc
./mvnw clean install      # Linux/Mac
```

### Frontend
```bash
cd frontend
npm install
```

## ⚙️ Bước 2: Cấu Hình

### Backend - Tạo file `backend/.env`:
```env
# Database (có thể dùng giá trị mặc định trong application.properties)
DATABASE_URL=jdbc:postgresql://your-host:5432/your-db
DATABASE_USERNAME=your-username
DATABASE_PASSWORD=your-password

# PayOS (BẮT BUỘC - lấy từ PayOS Dashboard)
PAYOS_CLIENT_ID=your-client-id
PAYOS_API_KEY=your-api-key
PAYOS_CHECKSUM_KEY=your-checksum-key-64-chars
```

### Frontend - Tạo file `frontend/.env.local`:
```env
NEXT_PUBLIC_API_URL=http://localhost:8080
```

## 🚀 Bước 3: Chạy

### Terminal 1 - Backend:
```bash
cd backend
.\mvnw.cmd spring-boot:run
```

Đợi đến khi thấy: `Started Project20251BackendApplication`

### Terminal 2 - Frontend:
```bash
cd frontend
npm run dev
```

## Bước 4: Test

1. Mở browser: `http://localhost:3000`
2. Bấm "Thanh toán với PayOS"
3. Popup QR code sẽ hiển thị
4. Quét QR bằng app ngân hàng để test

## Kiểm Tra Nhanh

- Backend: `http://localhost:8080` (nên có response hoặc error page)
- Frontend: `http://localhost:3000` (trang chủ cửa hàng demo)

## Lưu Ý

- **PayOS Credentials là BẮT BUỘC** - không có sẽ không tạo được payment link
- Database phải đang chạy và accessible
- Port 8080 (backend) và 3000 (frontend) phải trống

---
