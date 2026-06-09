package com.woocurlee.bookviewadmin.repository

import com.woocurlee.bookviewadmin.domain.Comment
import com.woocurlee.bookviewadmin.domain.Status
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

interface CommentRepository : MongoRepository<Comment, String> {
    // content 부분일치 (대소문자 무시)
    @Query("{ 'content': { '\$regex': ?0, '\$options': 'i' } }")
    fun search(
        keyword: String,
        pageable: Pageable,
    ): Page<Comment>

    fun countByStatus(status: Status): Long
}
