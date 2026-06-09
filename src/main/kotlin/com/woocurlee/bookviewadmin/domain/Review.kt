package com.woocurlee.bookviewadmin.domain

import java.time.LocalDateTime
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * reviews 컬렉션 (bookview 본 서비스와 공유).
 */
@Document(collection = "reviews")
data class Review(
    @Id
    val id: String? = null,
    val reviewNo: Long? = null,
    val userId: String? = null,
    val title: String? = null,
    val bookTitle: String? = null,
    val bookAuthor: String? = null,
    val bookIsbn: String? = null,
    val bookThumbnail: String? = null,
    val rating: Int = 0,
    val quote: String? = null,
    val content: String? = null,
    val likeCount: Long = 0,
    val status: Status = Status.ACTIVE,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)
