package com.woocurlee.bookviewadmin.service

import com.woocurlee.bookviewadmin.domain.BlockAction
import com.woocurlee.bookviewadmin.domain.BlockLog
import com.woocurlee.bookviewadmin.domain.Status
import com.woocurlee.bookviewadmin.domain.TargetType
import com.woocurlee.bookviewadmin.domain.User
import com.woocurlee.bookviewadmin.repository.BlockLogRepository
import com.woocurlee.bookviewadmin.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class AdminUserService(
    private val userRepository: UserRepository,
    private val blockLogRepository: BlockLogRepository,
) {
    /**
     * 유저 목록. q 가 있으면 nickname/email 부분일치 검색, 없으면 전체.
     */
    fun list(
        q: String?,
        pageable: Pageable,
    ): Page<User> =
        if (q.isNullOrBlank()) {
            userRepository.findAll(pageable)
        } else {
            userRepository.search(q.trim(), pageable)
        }

    /**
     * 상태 변경: ACTIVE → BLOCK / BLOCK → ACTIVE.
     * 차단 시 BlockLog 에 사유 기록.
     */
    fun toggleStatus(
        id: String,
        reason: String?,
    ): User {
        val user = userRepository.findById(id).orElseThrow { NoSuchElementException("user not found: $id") }
        return if (user.status == Status.ACTIVE) {
            require(!reason.isNullOrBlank()) { "차단 사유는 필수입니다." }
            blockLogRepository.save(
                BlockLog(
                    targetType = TargetType.USER,
                    targetId = id,
                    action = BlockAction.BLOCK,
                    reason = reason.trim(),
                ),
            )
            userRepository.save(user.copy(status = Status.BLOCK))
        } else {
            blockLogRepository.save(BlockLog(targetType = TargetType.USER, targetId = id, action = BlockAction.UNBLOCK))
            userRepository.save(user.copy(status = Status.ACTIVE))
        }
    }
}
