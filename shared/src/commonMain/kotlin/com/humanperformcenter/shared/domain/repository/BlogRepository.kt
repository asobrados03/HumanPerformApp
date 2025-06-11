package com.humanperformcenter.shared.domain.repository

import com.humanperformcenter.shared.data.model.BlogEntry

interface BlogRepository {
    suspend fun readBlogs(): Result<List<BlogEntry>>
}