package com.humanperformcenter.shared.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class BlogEntry(
    @SerialName("blog_id")
    val blogId: Int,
    val banner: String,
    val title: String,
    @SerialName("title_es")
    val titleEs: String,
    val description: String,
    @SerialName("description_es")
    val descriptionEs: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String
)
