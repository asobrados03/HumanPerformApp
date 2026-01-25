package com.humanperformcenter.shared.presentation.ui

sealed class MarkFavoriteState {
    object Idle : MarkFavoriteState()
    object Loading : MarkFavoriteState()
    data class Success(val message: String) : MarkFavoriteState()
    data class Error(val message: String) : MarkFavoriteState()
}