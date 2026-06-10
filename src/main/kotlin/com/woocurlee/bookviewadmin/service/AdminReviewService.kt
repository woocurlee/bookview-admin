package com.woocurlee.bookviewadmin.service

import com.woocurlee.bookviewadmin.domain.BlockAction
import com.woocurlee.bookviewadmin.domain.BlockLog
import com.woocurlee.bookviewadmin.domain.Review
import com.woocurlee.bookviewadmin.domain.Status
import com.woocurlee.bookviewadmin.domain.TargetType
import com.woocurlee.bookviewadmin.repository.BlockLogRepository
import com.woocurlee.bookviewadmin.repository.ReviewRepository
import com.woocurlee.bookviewadmin.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

/**
 * 리뷰 목록 한 행 (작성자 nickname 결합).
 * blockReason 은 상세 조회 시에만 채워진다.
 */
data class ReviewRow(
    val review: Review,
    val authorNickname: String,
    val blockReason: String? = null,
)

@Service
class AdminReviewService(
    private val reviewRepository: ReviewRepository,
    private val userRepository: UserRepository,
    private val blockLogRepository: BlockLogRepository,
) {
    /**
     * 리뷰 목록. q 가 있으면 bookTitle/bookAuthor 부분일치 검색.
     * status 가 있으면 해당 상태만 조회.
     * 각 행에 작성자 nickname 을 결합한다.
     */
    fun list(
        q: String?,
        status: Status?,
        pageable: Pageable,
    ): Page<ReviewRow> {
        val keyword = q?.trim()?.takeIf { it.isNotBlank() }
        val page =
            when {
                status != null && keyword != null -> reviewRepository.searchByStatus(keyword, status, pageable)
                status != null -> reviewRepository.findByStatus(status, pageable)
                keyword != null -> reviewRepository.search(keyword, pageable)
                else -> reviewRepository.findAll(pageable)
            }
        val nicknames = nicknamesOf(page.content.mapNotNull { it.userId })
        return page.map { ReviewRow(it, nicknames[it.userId] ?: "-") }
    }

    /**
     * 리뷰 상세. 작성자 nickname + 최근 차단 사유를 함께 반환한다.
     */
    fun findById(id: String): ReviewRow {
        val review = reviewRepository.findById(id).orElseThrow { NoSuchElementException("review not found: $id") }
        val nicknames = nicknamesOf(listOfNotNull(review.userId))
        val blockReason =
            if (review.status == Status.BLOCK) {
                blockLogRepository
                    .findFirstByTargetTypeAndTargetIdAndActionOrderByCreatedAtDesc(
                        TargetType.REVIEW,
                        id,
                        BlockAction.BLOCK,
                    )?.reason
            } else {
                null
            }
        return ReviewRow(review, nicknames[review.userId] ?: "-", blockReason)
    }

    /**
     * 상태 변경: ACTIVE → BLOCK / BLOCK → ACTIVE.
     * 차단 시 BlockLog 에 사유 기록.
     */
    fun toggleStatus(
        id: String,
        reason: String?,
    ): Review {
        val review = reviewRepository.findById(id).orElseThrow { NoSuchElementException("review not found: $id") }
        return if (review.status == Status.ACTIVE) {
            require(!reason.isNullOrBlank()) { "차단 사유는 필수입니다." }
            blockLogRepository.save(
                BlockLog(
                    targetType = TargetType.REVIEW,
                    targetId = id,
                    action = BlockAction.BLOCK,
                    reason = reason.trim(),
                ),
            )
            reviewRepository.save(review.copy(status = Status.BLOCK))
        } else {
            blockLogRepository.save(
                BlockLog(targetType = TargetType.REVIEW, targetId = id, action = BlockAction.UNBLOCK),
            )
            reviewRepository.save(review.copy(status = Status.ACTIVE))
        }
    }

    private fun nicknamesOf(userIds: List<String>): Map<String, String> =
        userRepository
            .findAllByGoogleIdIn(userIds.distinct())
            .associate { (it.googleId ?: "") to (it.nickname ?: "-") }
}
