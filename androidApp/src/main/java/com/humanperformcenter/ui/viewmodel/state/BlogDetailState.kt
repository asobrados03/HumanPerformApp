package com.humanperformcenter.ui.viewmodel.state

import com.humanperformcenter.shared.data.model.BlogEntry

sealed class BlogDetailState {
    object Idle : BlogDetailState()
    object Loading : BlogDetailState()
    data class Success(val blog: BlogEntry) : BlogDetailState()
    data class Error(val message: String) : BlogDetailState()
}