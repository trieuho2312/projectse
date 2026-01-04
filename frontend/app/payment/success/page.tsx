'use client'

import { useSearchParams, useRouter } from 'next/navigation'
import { useEffect, useState, Suspense } from 'react'

function PaymentSuccessContent() {
  const searchParams = useSearchParams()
  const router = useRouter()
  const [orderCode, setOrderCode] = useState<string | null>(null)

  useEffect(() => {
    const code = searchParams.get('orderCode')
    const status = searchParams.get('status')
    
    if (code) {
      setOrderCode(code)
    }

    // PayOS sẽ redirect về với các tham số orderCode và status
    if (status === 'CANCELLED') {
      router.push('/payment/cancel')
    }
  }, [searchParams, router])

  return (
    <main className="min-h-screen flex items-center justify-center bg-gradient-to-br from-green-50 to-emerald-100 p-4">
      <div className="bg-white rounded-lg shadow-xl p-8 max-w-md w-full text-center">
        <div className="text-green-500 text-6xl mb-4">✓</div>
        <h1 className="text-3xl font-bold text-gray-800 mb-4">
          Thanh Toán Thành Công!
        </h1>
        <p className="text-gray-600 mb-2">
          Cảm ơn bạn đã thanh toán
        </p>
        {orderCode && (
          <p className="text-sm text-gray-500 mb-6">
            Mã đơn hàng: <span className="font-semibold">{orderCode}</span>
          </p>
        )}
        <div className="space-y-3">
          <button
            onClick={() => router.push('/')}
            className="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 px-4 rounded-lg transition duration-200"
          >
            Tạo Đơn Hàng Mới
          </button>
          <button
            onClick={() => window.print()}
            className="w-full bg-gray-200 hover:bg-gray-300 text-gray-800 font-semibold py-3 px-4 rounded-lg transition duration-200"
          >
            In Hóa Đơn
          </button>
        </div>
      </div>
    </main>
  )
}

export default function PaymentSuccessPage() {
  return (
    <Suspense fallback={
      <main className="min-h-screen flex items-center justify-center bg-gradient-to-br from-green-50 to-emerald-100">
        <div className="bg-white rounded-lg shadow-xl p-8 text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-green-600 mx-auto mb-4"></div>
          <p className="text-gray-700">Đang tải...</p>
        </div>
      </main>
    }>
      <PaymentSuccessContent />
    </Suspense>
  )
}
