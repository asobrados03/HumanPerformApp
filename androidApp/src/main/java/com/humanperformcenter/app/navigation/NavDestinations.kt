package com.humanperformcenter.app.navigation

import kotlinx.serialization.Serializable

@Serializable object Welcome
@Serializable object Register
@Serializable object Login
@Serializable object Splash
@Serializable object EnterEmail
@Serializable object PasswordResetInfo
@Serializable object Service
@Serializable data class HireProduct(val serviceId: Int)
@Serializable object Calendar
@Serializable object Stats
@Serializable data class ActiveProductDetail(val productId: Int)
@Serializable data class ProductDetail(val productId: Int)
@Serializable object User
@Serializable object AddCoupon
@Serializable object Configuration
@Serializable object ChangePassword
@Serializable object EditProfile
@Serializable object MyProfile
@Serializable object FavoriteCoach
@Serializable object Document
@Serializable object ViewPaymentMethod
@Serializable object ElectronicWallet
@Serializable data class StripeSinglePayment(
    val productPrice: Double,
    val productId: Int,
    val couponCode: String? = null
)
@Serializable data class StripeSubscription(
    val productPrice: Double,
    val productId: Int,
    val priceId: String,
    val couponCode: String? = null
)
@Serializable object PaymentSuccess
