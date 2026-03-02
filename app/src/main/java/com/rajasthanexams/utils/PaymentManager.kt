package com.rajasthanexams.utils

import android.app.Activity
import android.widget.Toast
import com.rajasthanexams.data.remote.dto.CreateOrderResponse
import com.razorpay.Checkout
import org.json.JSONObject

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

sealed class PaymentResult {
    data class Success(val orderId: String, val paymentId: String, val signature: String) : PaymentResult()
    data class Error(val code: Int, val message: String) : PaymentResult()
}

object PaymentManager {

    private val _paymentResult = MutableSharedFlow<PaymentResult>(extraBufferCapacity = 1)
    val paymentResult: SharedFlow<PaymentResult> = _paymentResult

    fun startPayment(
        activity: Activity,
        orderData: CreateOrderResponse
    ) {
        val checkout = Checkout()
        checkout.setKeyID(orderData.key)
        
        try {
            val options = JSONObject()
            options.put("name", "Rajasthan Exams")
            options.put("description", orderData.description)
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png")
            options.put("order_id", orderData.orderId)
            options.put("theme.color", "#3399cc")
            options.put("currency", "INR")
            options.put("amount", orderData.amount.toString())
            options.put("prefill.email", "student@rajasthanexams.com")
            options.put("prefill.contact", "9999999999")
            
            checkout.open(activity, options)
        } catch (e: Exception) {
            Toast.makeText(activity, "Error starting payment: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    fun onPaymentSuccess(paymentData: com.razorpay.PaymentData?) {
        if (paymentData != null) {
            val orderId = paymentData.orderId
            val paymentId = paymentData.paymentId
            val signature = paymentData.signature
            
            if (orderId != null && paymentId != null && signature != null) {
                 _paymentResult.tryEmit(PaymentResult.Success(orderId, paymentId, signature))
            } else {
                 _paymentResult.tryEmit(PaymentResult.Error(0, "Missing payment details"))
            }
        } else {
            _paymentResult.tryEmit(PaymentResult.Error(0, "Payment data is null"))
        }
    }

    fun onPaymentError(code: Int, response: String) {
        _paymentResult.tryEmit(PaymentResult.Error(code, response))
    }
}
