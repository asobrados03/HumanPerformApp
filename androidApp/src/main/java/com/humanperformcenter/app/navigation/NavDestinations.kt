package com.humanperformcenter.app.navigation

import kotlinx.serialization.Serializable

@Serializable object Welcome
@Serializable object Register
@Serializable object Login
@Serializable object Splash
@Serializable object NewProduct
@Serializable object Calendar
@Serializable object Stats

@Serializable
data class BlogDetail(val blogId: Int)
@Serializable
data class ProductDetail(val productId: Int)

@Serializable object User
@Serializable object Configuration
@Serializable object ChangePassword
@Serializable object EditProfile
@Serializable object MyProfile
@Serializable object Favorites
@Serializable object Chat
@Serializable object Document
@Serializable object Payment
@Serializable object ViewPayment
