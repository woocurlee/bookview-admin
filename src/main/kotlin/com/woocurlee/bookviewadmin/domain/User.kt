package com.woocurlee.bookviewadmin.domain

import java.time.LocalDateTime
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * users 컬렉션 (bookview 본 서비스와 공유).
 */
@Document(collection = "users")
data class User(
    @Id
    val id: String? = null,
    val userNo: Long? = null,
    val googleId: String? = null,
    val nickname: String? = null,
    val email: String? = null,
    val isNicknameSet: Boolean = false,
    val profileImageUrl: String? = null,
    val status: Status = Status.ACTIVE,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastLoginAt: LocalDateTime? = null,
)
