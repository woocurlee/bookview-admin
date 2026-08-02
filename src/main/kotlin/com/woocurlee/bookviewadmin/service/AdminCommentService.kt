package com.woocurlee.bookviewadmin.service

import com.woocurlee.bookviewadmin.domain.BlockAction
import com.woocurlee.bookviewadmin.domain.BlockLog
import com.woocurlee.bookviewadmin.domain.Comment
import com.woocurlee.bookviewadmin.domain.Status
import com.woocurlee.bookviewadmin.domain.TargetType
import com.woocurlee.bookviewadmin.repository.BlockLogRepository
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
    private val blockLogRepository: BlockLogRepository,
) {
    /**
     * 댓글 목록. q 가 있으면 content 부분일치 검색.
     * status 가 있으면 해당 상태만 조회.
     * 각 행에 작성자 nickname 과 연결된 리뷰의 reviewNo 를 결합한다.
     */
    fun list(
        q: String?,
        status: Status?,
        pageable: Pageable,
    ): Page<CommentRow> {
        val keyword = q?.trim()?.takeIf { it.isNotBlank() }
        val page =
            when {
                status != null && keyword != null -> commentRepository.searchByStatus(keyword, status, pageable)
                status != null -> commentRepository.findByStatus(status, pageable)
                keyword != null -> commentRepository.search(keyword, pageable)
                else -> commentRepository.findAll(pageable)
            }

        val nicknames =
            userRepository
                .findAllByGoogleIdIn(page.content.mapNotNull { it.userId }.distinct())
                .associate { (it.googleId ?: "") to (it.nickname ?: "-") }
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
     * 상태 변경: ACTIVE → BLOCK / BLOCK → ACTIVE.
     * 차단 시 BlockLog 에 사유 기록.
     */
    fun toggleStatus(
        id: String,
        reason: String?,
    ): Comment {
        val comment = commentRepository.findById(id).orElseThrow { NoSuchElementException("comment not found: $id") }
        return if (comment.status == Status.ACTIVE) {
            require(!reason.isNullOrBlank()) { "차단 사유는 필수입니다." }
            blockLogRepository.save(
                BlockLog(
                    targetType = TargetType.COMMENT,
                    targetId = id,
                    action = BlockAction.BLOCK,
                    reason = reason.trim(),
                ),
            )
            commentRepository.save(comment.copy(status = Status.BLOCK))
        } else {
            blockLogRepository.save(
                BlockLog(targetType = TargetType.COMMENT, targetId = id, action = BlockAction.UNBLOCK),
            )
            commentRepository.save(comment.copy(status = Status.ACTIVE))
        }
    }
}
