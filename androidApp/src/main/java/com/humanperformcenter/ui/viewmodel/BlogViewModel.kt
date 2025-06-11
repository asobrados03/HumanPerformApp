package com.humanperformcenter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humanperformcenter.shared.domain.usecase.BlogUseCase
import com.humanperformcenter.ui.viewmodel.state.BlogDetailState
import com.humanperformcenter.ui.viewmodel.state.BlogState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BlogViewModel(
    private val blogUseCase: BlogUseCase
): ViewModel() {
    // Estado interno mutable
    private val _state = MutableStateFlow<BlogState>(BlogState.Idle)
    // Estado público inmutable
    val state: StateFlow<BlogState> = _state.asStateFlow()

    private val _detailState = MutableStateFlow<BlogDetailState>(BlogDetailState.Idle)
    val detailState: StateFlow<BlogDetailState> = _detailState.asStateFlow()


    /**
     * Dispara la carga de blogs desde el caso de uso.
     */
    fun loadBlogs() {
        _state.value = BlogState.Loading
        viewModelScope.launch {
            blogUseCase.readBlogs().onSuccess { blogsList ->
                _state.value = BlogState.Success(blogsList)
            }.onFailure { throwable ->
                _state.value = BlogState.Error(
                    throwable.message.orEmpty().ifEmpty { "Error desconocido al cargar blogs" }
                )
            }
        }
    }

    fun loadBlogDetail(blogId: Int) {
        _detailState.value = BlogDetailState.Loading
        viewModelScope.launch {
            blogUseCase.readBlogById(blogId).onSuccess { blog ->
                _detailState.value = BlogDetailState.Success(blog)
            }.onFailure { throwable ->
                _detailState.value = BlogDetailState.Error(
                    throwable.message.orEmpty().ifEmpty { "Error al cargar el detalle del blog" }
                )
            }
        }
    }
}