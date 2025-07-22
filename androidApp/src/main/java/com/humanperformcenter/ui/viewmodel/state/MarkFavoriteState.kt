package com.humanperformcenter.ui.viewmodel.state

sealed class MarkFavoriteState {
    object Idle : MarkFavoriteState()
    object Loading : MarkFavoriteState()
    data class Success(val message: String) : MarkFavoriteState()
    data class Error(val message: String) : MarkFavoriteState()
}