package com.humanperformcenter.shared.data.model.user

import kotlinx.serialization.Serializable

@Serializable
data class DeleteProfilePicRequest(
    val email: String,
    val profilePictureName: String?
)
