package com.humanperformcenter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.humanperformcenter.shared.domain.usecase.BlogUseCase

class BlogViewModelFactory(
    private val blogUseCase: BlogUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(BlogViewModel::class.java))
        @Suppress("UNCHECKED_CAST")
        return BlogViewModel(blogUseCase) as T
    }
}
