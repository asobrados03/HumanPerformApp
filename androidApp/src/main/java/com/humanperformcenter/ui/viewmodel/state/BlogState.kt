package com.humanperformcenter.ui.viewmodel.state

import com.humanperformcenter.shared.data.model.BlogEntry

sealed class BlogState {
    object Idle : BlogState()
    object Loading : BlogState()
    data class Success(val blogs: List<BlogEntry>) : BlogState()
    data class Error(val message: String) : BlogState()
}