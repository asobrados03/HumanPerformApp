package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.BlogEntry
import com.humanperformcenter.shared.domain.repository.BlogRepository

class BlogUseCase(private val blogRepository: BlogRepository) {
    suspend fun readBlogs(): Result<List<BlogEntry>> {
        return blogRepository.readBlogs()
    }

    suspend fun readBlogById(blogId: Int): Result<BlogEntry> =
        blogRepository.readBlogs()
            .mapCatching { list ->
                list.first { it.blogId == blogId }  // lanza NoSuchElementException si no lo halla
            }

}
