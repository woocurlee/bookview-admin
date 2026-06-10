package com.woocurlee.bookviewadmin.domain

import java.time.LocalDateTime
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

enum class TargetType { USER, REVIEW, COMMENT }

enum class BlockAction { BLOCK, UNBLOCK }

/**
 * 어드민 차단/해제 이력.
 * 각 도메인 컬렉션에 blockReason 을 두지 않고 여기에 기록한다.
 */
@Document(collection = "block_logs")
data class BlockLog(
    @Id
    val id: String? = null,
    val targetType: TargetType,
    val targetId: String,
    val action: BlockAction,
    val reason: String? = null, // BLOCK 시에만 기록
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
