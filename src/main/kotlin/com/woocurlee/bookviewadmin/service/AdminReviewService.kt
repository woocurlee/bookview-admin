package com.woocurlee.bookviewadmin.service

import com.woocurlee.bookviewadmin.domain.Review
import com.woocurlee.bookviewadmin.domain.Status
import com.woocurlee.bookviewadmin.repository.ReviewRepository
import com.woocurlee.bookviewadmin.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

/**
 * 리뷰 목록 한 행 (작성자 nickname 결합).
 */
data class ReviewRow(
    val review: Review,
    val authorNickname: String,
)

@Service
class AdminReviewService(
    private val reviewRepository: ReviewRepository,
    private val userRepository: UserRepository,
) {
    /**
     * 리뷰 목록. q 가 있으면 bookTitle/bookAuthor 부분일치 검색.
     * 각 행에 작성자 nickname 을 결합한다.
     */
    fun list(
        q: String?,
        pageable: Pageable,
    ): Page<ReviewRow> {
        val page =
            if (q.isNullOrBlank()) {
                reviewRepository.findAll(pageable)
            } else {
                reviewRepository.search(q.trim(), pageable)
            }
        val nicknames = nicknamesOf(page.content.mapNotNull { it.userId })
        return page.map { ReviewRow(it, nicknames[it.userId] ?: "-") }
    }

    /**
     * 상태 토글: ACTIVE ↔ DELETED.
     */
    fun toggleStatus(id: String): Review {
        val review = reviewRepository.findById(id).orElseThrow { NoSuchElementException("review not found: $id") }
        val next = if (review.status == Status.ACTIVE) Status.DELETED else Status.ACTIVE
        return reviewRepository.save(review.copy(status = next))
    }

    private fun nicknamesOf(userIds: List<String>): Map<String, String> =
        userRepository
            .findAllById(userIds.distinct())
            .associate { (it.id ?: "") to (it.nickname ?: "-") }
}
