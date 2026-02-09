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
@Serializable object StripeSinglePayment
@Serializable object StripeSubscription
@Serializable object PaymentSuccess
