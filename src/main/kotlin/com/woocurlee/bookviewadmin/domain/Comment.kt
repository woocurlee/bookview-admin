package com.woocurlee.bookviewadmin.domain

import java.time.LocalDateTime
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * comments 컬렉션 (bookview 본 서비스와 공유).
 */
@Document(collection = "comments")
data class Comment(
    @Id
    val id: String? = null,
    val commentNo: Long? = null,
    val reviewId: String? = null,
    val userId: String? = null,
    val content: String? = null,
    val parentId: String? = null,
    val status: Status = Status.ACTIVE,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)
