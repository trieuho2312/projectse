'use client'

import { useRouter } from 'next/navigation'
import { useState } from 'react'

interface CartItem {
  id: string
  name: string
  price: number
  quantity: number
  image?: string
}

export default function Home() {
  const router = useRouter()
  const [cartItems] = useState<CartItem[]>([
    {
      id: '1',
      name: 'Sản phẩm demo',
      price: 50000,
      quantity: 1,
    }
  ])
  const [isProcessing, setIsProcessing] = useState(false)

  const totalAmount = cartItems.reduce((sum, item) => sum + (item.price * item.quantity), 0)

  const handleCheckout = () => {
    if (totalAmount > 0 && !isProcessing) {
      setIsProcessing(true)
      const params = new URLSearchParams()
      params.append('amount', totalAmount.toString())
      router.push(`/checkout?${params.toString()}`)
    }
  }

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(price)
  }

  return (
    <main className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 py-8 px-4">
      <div className="max-w-4xl mx-auto">
        {/* Header */}
        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
          <h1 className="text-3xl font-bold text-gray-800 mb-2">🛍️ Cửa Hàng Demo</h1>
          <p className="text-gray-600">Thanh toán đơn hàng với PayOS</p>
        </div>

        {/* Cart Items */}
        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
          <h2 className="text-xl font-semibold text-gray-800 mb-4">Giỏ hàng của bạn</h2>
          
          <div className="space-y-4">
            {cartItems.map((item) => (
              <div key={item.id} className="flex items-center justify-between p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition">
                <div className="flex-1">
                  <h3 className="font-medium text-gray-800">{item.name}</h3>
                  <p className="text-sm text-gray-500">Số lượng: {item.quantity}</p>
                </div>
                <div className="text-right">
                  <p className="font-semibold text-gray-800">{formatPrice(item.price * item.quantity)}</p>
                  <p className="text-sm text-gray-500">{formatPrice(item.price)}/sản phẩm</p>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Order Summary */}
        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
          <h2 className="text-xl font-semibold text-gray-800 mb-4">Tóm tắt đơn hàng</h2>
          
          <div className="space-y-3">
            <div className="flex justify-between text-gray-600">
              <span>Tạm tính:</span>
              <span>{formatPrice(totalAmount)}</span>
            </div>
            <div className="flex justify-between text-gray-600">
              <span>Phí vận chuyển:</span>
              <span className="text-green-600">Miễn phí</span>
            </div>
            <div className="border-t border-gray-200 pt-3 mt-3">
              <div className="flex justify-between items-center">
                <span className="text-lg font-semibold text-gray-800">Tổng cộng:</span>
                <span className="text-2xl font-bold text-blue-600">{formatPrice(totalAmount)}</span>
              </div>
            </div>
          </div>
        </div>

        {/* Checkout Button */}
        <div className="bg-white rounded-lg shadow-md p-6">
          <button
            onClick={handleCheckout}
            disabled={totalAmount === 0 || isProcessing}
            className="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed text-white font-semibold py-4 px-6 rounded-lg transition duration-200 shadow-md hover:shadow-lg text-lg"
          >
            {isProcessing ? 'Đang xử lý...' : 'Thanh toán với PayOS'}
          </button>
          <p className="text-center text-sm text-gray-500 mt-4">
            {isProcessing ? 'Vui lòng đợi...' : 'Bạn sẽ được chuyển đến trang thanh toán PayOS'}
          </p>
        </div>
      </div>
    </main>
  )
}
