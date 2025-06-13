package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.BlogEntry
import com.humanperformcenter.shared.domain.repository.BlogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class BlogUseCase(private val blogRepository: BlogRepository) {
    suspend fun readBlogs(): Result<List<BlogEntry>> = withContext(Dispatchers.IO) {
        return@withContext blogRepository.readBlogs()
    }

    suspend fun readBlogById(blogId: Int): Result<BlogEntry> = withContext(Dispatchers.IO) {
        return@withContext blogRepository.readBlogs()
            .mapCatching { list ->
                list.first { it.blogId == blogId }
            }
    }
}
