package com.woocurlee.bookviewadmin.repository

import com.woocurlee.bookviewadmin.domain.BlockAction
import com.woocurlee.bookviewadmin.domain.BlockLog
import com.woocurlee.bookviewadmin.domain.TargetType
import org.springframework.data.mongodb.repository.MongoRepository

interface BlockLogRepository : MongoRepository<BlockLog, String> {
    /** 특정 대상의 최근 BLOCK 사유를 가져온다. */
    fun findFirstByTargetTypeAndTargetIdAndActionOrderByCreatedAtDesc(
        targetType: TargetType,
        targetId: String,
        action: BlockAction,
    ): BlockLog?
}
