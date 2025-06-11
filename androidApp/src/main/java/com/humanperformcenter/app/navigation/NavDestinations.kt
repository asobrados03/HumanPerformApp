package com.humanperformcenter.app.navigation

import kotlinx.serialization.Serializable

@Serializable object Welcome
@Serializable object Register
@Serializable object Login
@Serializable object NewProduct
@Serializable object Calendar
@Serializable object NewBlog

@Serializable
data class BlogDetail(val blogId: Int)

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
@Serializable object Entrenamiento
@Serializable object Nutricion
@Serializable object Fisioterapia
@Serializable object Pilates
@Serializable object Presoterapia
@Serializable object Opositores
@Serializable object Taquilla
@Serializable object AlterG