package com.woocurlee.bookviewadmin.service

import com.woocurlee.bookviewadmin.domain.Comment
import com.woocurlee.bookviewadmin.domain.Status
import com.woocurlee.bookviewadmin.repository.CommentRepository
import com.woocurlee.bookviewadmin.repository.ReviewRepository
import com.woocurlee.bookviewadmin.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

/**
 * 댓글 목록 한 행 (작성자 nickname, 연결 reviewNo 결합).
 */
data class CommentRow(
    val comment: Comment,
    val authorNickname: String,
    val reviewNo: Long?,
) {
    /** content 앞 80자 미리보기 */
    val preview: String
        get() {
            val text = comment.content ?: ""
            return if (text.length > 80) text.take(80) + "…" else text
        }
}

@Service
class AdminCommentService(
    private val commentRepository: CommentRepository,
    private val userRepository: UserRepository,
    private val reviewRepository: ReviewRepository,
) {
    /**
     * 댓글 목록. q 가 있으면 content 부분일치 검색.
     * 각 행에 작성자 nickname 과 연결된 리뷰의 reviewNo 를 결합한다.
     */
    fun list(
        q: String?,
        pageable: Pageable,
    ): Page<CommentRow> {
        val page =
            if (q.isNullOrBlank()) {
                commentRepository.findAll(pageable)
            } else {
                commentRepository.search(q.trim(), pageable)
            }

        val nicknames =
            userRepository
                .findAllById(page.content.mapNotNull { it.userId }.distinct())
                .associate { (it.id ?: "") to (it.nickname ?: "-") }
        val reviewNos =
            reviewRepository
                .findAllById(page.content.mapNotNull { it.reviewId }.distinct())
                .associate { (it.id ?: "") to it.reviewNo }

        return page.map {
            CommentRow(
                comment = it,
                authorNickname = nicknames[it.userId] ?: "-",
                reviewNo = reviewNos[it.reviewId],
            )
        }
    }

    /**
     * 상태 토글: ACTIVE ↔ DELETED.
     */
    fun toggleStatus(id: String): Comment {
        val comment = commentRepository.findById(id).orElseThrow { NoSuchElementException("comment not found: $id") }
        val next = if (comment.status == Status.ACTIVE) Status.DELETED else Status.ACTIVE
        return commentRepository.save(comment.copy(status = next))
    }
}
