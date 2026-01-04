'use client'

import { useSearchParams, useRouter } from 'next/navigation'
import { useEffect, useState, Suspense, useRef } from 'react'
import axios from 'axios'
import { QRCodeSVG } from 'qrcode.react'

interface PaymentLinkResponse {
  code: number
  message: string
  result: {
    checkoutUrl: string
    orderCode: string
    message: string
    qrCode?: string
  }
}

function CheckoutContent() {
  const searchParams = useSearchParams()
  const router = useRouter()
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [showQRPopup, setShowQRPopup] = useState(false)
  const [qrCode, setQrCode] = useState<string | null>(null)
  const [orderCode, setOrderCode] = useState<string | null>(null)
  const [checkoutUrl, setCheckoutUrl] = useState<string | null>(null)
  const [timeLeft, setTimeLeft] = useState(300) // 5 phút = 300 giây
  const hasRequestedRef = useRef(false) // Đảm bảo chỉ gửi 1 request

  const orderId = searchParams.get('orderId')
  const amount = searchParams.get('amount')
  // Để nội dung chuyển khoản chỉ có mã (ví dụ: CSGX0SVDCO5), không gửi description
  // PayOS sẽ tự động tạo mã từ orderCode
  const description = '' // Rỗng để chỉ hiển thị mã

  // Countdown timer cho QR popup
  useEffect(() => {
    if (!showQRPopup) return

    if (timeLeft <= 0) {
      // Tự động đóng popup khi hết hạn
      const closeTimer = setTimeout(() => {
        handleCloseQRPopup()
      }, 2000) // Đợi 2 giây để user đọc thông báo
      return () => clearTimeout(closeTimer)
    }

    const timer = setInterval(() => {
      setTimeLeft((prev) => {
        if (prev <= 1) {
          return 0
        }
        return prev - 1
      })
    }, 1000)

    return () => clearInterval(timer)
  }, [showQRPopup, timeLeft])

  useEffect(() => {
    if (!amount) {
      setError('Vui lòng nhập số tiền')
      setLoading(false)
      return
    }

    // Tránh gửi nhiều requests (React Strict Mode hoặc double-click)
    if (hasRequestedRef.current) {
      return
    }
    hasRequestedRef.current = true

    let isMounted = true
    let timeoutId: NodeJS.Timeout | null = null

    // Timeout sau 10 giây để tránh treo mãi
    timeoutId = setTimeout(() => {
      if (isMounted) {
        console.error('Request timeout after 10 seconds')
        setError('Request timeout. Vui lòng thử lại.')
        setLoading(false)
      }
    }, 10000)

    const createPaymentLink = async () => {
      try {
        const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
        const returnUrl = `${window.location.origin}/payment/success`
        const cancelUrl = `${window.location.origin}/payment/cancel`

        // Sử dụng endpoint test nếu không có orderId hoặc orderId trống
        const useTestEndpoint = !orderId || orderId.trim() === ''
        const endpoint = useTestEndpoint ? '/payments/payos/test' : '/payments/payos/create'
        
        const requestBody = useTestEndpoint
          ? {
              amount: parseInt(amount, 10),
              description,
              returnUrl,
              cancelUrl,
            }
          : {
              orderId,
              description,
              returnUrl,
              cancelUrl,
            }

        const response = await axios.post<PaymentLinkResponse>(
          `${apiUrl}${endpoint}`,
          requestBody,
          { timeout: 10000 } // 10s timeout
        )

        console.log('Payment response:', response.data)
        console.log('Response code:', response.data.code)
        console.log('Response result:', response.data.result)

        // Clear timeout vì đã có response
        if (timeoutId) {
          clearTimeout(timeoutId)
          timeoutId = null
        }

        // Kiểm tra response format
        if (response.data.code === 1000) {
          const checkoutUrl = response.data.result?.checkoutUrl
          const qrCodeData = response.data.result?.qrCode
          const orderCodeData = response.data.result?.orderCode
          
          if (checkoutUrl && qrCodeData) {
            console.log('Got QR code, showing popup')
            setQrCode(qrCodeData)
            setOrderCode(orderCodeData || null)
            setCheckoutUrl(checkoutUrl)
            setTimeLeft(300) // Reset về 5 phút
            setShowQRPopup(true)
            setLoading(false)
            return
          } else if (checkoutUrl) {
            // Fallback: nếu không có QR code, vẫn redirect như cũ
            console.log('No QR code, redirecting to:', checkoutUrl)
            window.location.replace(checkoutUrl)
            return
          } else {
            console.error('Missing checkoutUrl in response:', response.data)
            setError(response.data.message || 'Không thể tạo link thanh toán')
            setLoading(false)
          }
        } else {
          console.error('Invalid response code:', response.data.code)
          console.error('Full response:', response.data)
          setError(response.data.message || 'Không thể tạo link thanh toán')
          setLoading(false)
        }
      } catch (err: any) {
        // Clear timeout vì đã có response (dù là lỗi)
        if (timeoutId) {
          clearTimeout(timeoutId)
          timeoutId = null
        }
        
        console.error('Error creating payment link:', err)
        console.error('Error response:', err.response?.data)
        console.error('Error status:', err.response?.status)
        
        const errorMessage = err.response?.data?.message || err.message || ''
        const errorCode = err.response?.data?.code
        const errorDesc = err.response?.data?.desc || ''
        const fullError = JSON.stringify(err.response?.data || err.message)
        
        // Nếu lỗi "đơn đã tồn tại" (code 231), retry ngay lập tức
        if (errorCode === 231 || errorDesc.includes('231') || errorMessage.includes('231') || 
            errorMessage.includes('đã tồn tại') || errorDesc.includes('đã tồn tại') ||
            fullError.includes('231') || fullError.includes('đã tồn tại')) {
          console.log('Order already exists, retrying immediately...')
          
          // Retry ngay lập tức (không đợi)
          try {
            const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
            const returnUrl = `${window.location.origin}/payment/success`
            const cancelUrl = `${window.location.origin}/payment/cancel`
            const useTestEndpoint = !orderId || orderId.trim() === ''
            const endpoint = useTestEndpoint ? '/payments/payos/test' : '/payments/payos/create'
            
            const requestBody = useTestEndpoint
              ? {
                  amount: parseInt(amount!, 10),
                  description,
                  returnUrl,
                  cancelUrl,
                }
              : {
                  orderId,
                  description,
                  returnUrl,
                  cancelUrl,
                }

            console.log('Retrying payment request...')
            const retryResponse = await axios.post<PaymentLinkResponse>(
              `${apiUrl}${endpoint}`,
              requestBody,
              { timeout: 10000 } // 10s timeout
            )

            console.log('Retry response:', retryResponse.data)

            if (retryResponse.data.code === 1000 && retryResponse.data.result?.checkoutUrl) {
              const url = retryResponse.data.result.checkoutUrl
              const qrCodeData = retryResponse.data.result?.qrCode
              const orderCodeData = retryResponse.data.result?.orderCode
              
              if (qrCodeData) {
                console.log('Retry successful, showing QR popup')
                setQrCode(qrCodeData)
                setOrderCode(orderCodeData || null)
                setCheckoutUrl(url)
                setTimeLeft(300) // Reset về 5 phút
                setShowQRPopup(true)
                setLoading(false)
                return
              } else {
                // Fallback: nếu không có QR code, vẫn redirect như cũ
                console.log('Retry successful, redirecting to:', url)
                window.location.replace(url)
                return
              }
            } else {
              console.error('Retry failed:', retryResponse.data)
              setError('Không thể tạo link thanh toán. Vui lòng thử lại.')
              setLoading(false)
            }
          } catch (retryErr: any) {
            console.error('Retry error:', retryErr)
            setError('Không thể tạo link thanh toán. Vui lòng thử lại sau.')
            setLoading(false)
          }
        } else {
          setError(
            errorMessage ||
            'Có lỗi xảy ra khi tạo link thanh toán'
          )
          setLoading(false)
        }
      }
    }

    createPaymentLink()

    return () => {
      isMounted = false
      if (timeoutId) {
        clearTimeout(timeoutId)
      }
    }
  }, [orderId, amount, description])

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(price)
  }

  const formatTime = (seconds: number) => {
    const mins = Math.floor(seconds / 60)
    const secs = seconds % 60
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
  }

  const handleCloseQRPopup = () => {
    setShowQRPopup(false)
    setQrCode(null)
    setOrderCode(null)
    setTimeLeft(300)
    router.push('/')
  }

  if (loading) {
    return (
      <main className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100 p-4">
        <div className="bg-white rounded-lg shadow-xl p-8 max-w-md w-full text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <h2 className="text-xl font-semibold text-gray-800 mb-2">Đang xử lý đơn hàng...</h2>
          <p className="text-gray-600">Vui lòng đợi trong giây lát</p>
          {amount && (
            <p className="text-sm text-gray-500 mt-4">
              Tổng tiền: <span className="font-semibold">{formatPrice(parseInt(amount, 10))}</span>
            </p>
          )}
        </div>
      </main>
    )
  }

  if (error) {
    return (
      <main className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100 p-4">
        <div className="bg-white rounded-lg shadow-xl p-8 max-w-md w-full">
          <div className="text-center">
            <div className="text-red-500 text-5xl mb-4">✕</div>
            <h2 className="text-2xl font-bold text-gray-800 mb-4">Lỗi</h2>
            <p className="text-gray-600 mb-6">{error}</p>
            <button
              onClick={() => router.push('/')}
              className="bg-blue-600 hover:bg-blue-700 text-white font-semibold py-2 px-6 rounded-lg transition duration-200"
            >
              Quay lại
            </button>
          </div>
        </div>
      </main>
    )
  }

  return (
    <>
      {showQRPopup && qrCode && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg shadow-2xl max-w-md w-full p-6 relative">
            {/* Close button */}
            <button
              onClick={handleCloseQRPopup}
              className="absolute top-4 right-4 text-gray-500 hover:text-gray-700 text-2xl font-bold"
            >
              ×
            </button>

            {/* Header */}
            <div className="text-center mb-6">
              <h2 className="text-2xl font-bold text-gray-800 mb-2">Quét mã QR để thanh toán</h2>
              {orderCode && (
                <p className="text-sm text-gray-600">Mã đơn hàng: <span className="font-semibold">{orderCode}</span></p>
              )}
              {amount && (
                <p className="text-lg font-semibold text-blue-600 mt-2">
                  {formatPrice(parseInt(amount, 10))}
                </p>
              )}
            </div>

            {/* QR Code */}
            <div className="flex justify-center mb-6">
              <div className="bg-white p-4 rounded-lg border-2 border-gray-200">
                <QRCodeSVG
                  value={qrCode}
                  size={256}
                  level="H"
                  includeMargin={true}
                />
              </div>
            </div>

            {/* Countdown Timer */}
            <div className="text-center mb-6">
              <div className="inline-flex items-center space-x-2 bg-red-50 border border-red-200 rounded-lg px-4 py-2">
                <span className="text-red-600 font-semibold">⏱️</span>
                <span className="text-red-700 font-bold text-lg">
                  {timeLeft > 0 ? formatTime(timeLeft) : '00:00'}
                </span>
                <span className="text-red-600 text-sm">còn lại</span>
              </div>
              {timeLeft === 0 && (
                <p className="text-red-600 text-sm mt-2 font-semibold">
                  Mã QR đã hết hạn. Vui lòng tạo đơn hàng mới.
                </p>
              )}
            </div>

            {/* Instructions */}
            <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-4">
              <p className="text-sm text-blue-800 text-center">
                <strong>Hướng dẫn:</strong> Mở ứng dụng ngân hàng của bạn, quét mã QR và thanh toán.
              </p>
            </div>

            {/* Action buttons */}
            <div className="flex space-x-3">
              <button
                onClick={handleCloseQRPopup}
                className="flex-1 bg-gray-200 hover:bg-gray-300 text-gray-800 font-semibold py-3 px-4 rounded-lg transition duration-200"
              >
                Đóng
              </button>
              {checkoutUrl && (
                <a
                  href={checkoutUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="flex-1 bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 px-4 rounded-lg transition duration-200 text-center"
                >
                  Mở PayOS
                </a>
              )}
            </div>
          </div>
        </div>
      )}
    </>
  )
}

export default function CheckoutPage() {
  return (
    <Suspense fallback={
      <main className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100">
        <div className="bg-white rounded-lg shadow-xl p-8 text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p className="text-gray-700">Đang tải...</p>
        </div>
      </main>
    }>
      <CheckoutContent />
    </Suspense>
  )
}
