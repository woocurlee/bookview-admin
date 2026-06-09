package com.woocurlee.bookviewadmin.controller

import com.woocurlee.bookviewadmin.domain.Status
import com.woocurlee.bookviewadmin.service.AdminCommentService
import com.woocurlee.bookviewadmin.service.AdminReviewService
import com.woocurlee.bookviewadmin.service.AdminUserService
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 어드민 상태 변경 REST API.
 * 토글 결과로 바뀐 status 를 돌려준다.
 */
data class StatusResponse(
    val id: String?,
    val status: Status,
)

@RestController
@RequestMapping("/api")
class AdminApiController(
    private val userService: AdminUserService,
    private val reviewService: AdminReviewService,
    private val commentService: AdminCommentService,
) {
    @PatchMapping("/users/{id}/status")
    fun toggleUserStatus(
        @PathVariable id: String,
    ): StatusResponse {
        val user = userService.toggleStatus(id)
        return StatusResponse(user.id, user.status)
    }

    @PatchMapping("/reviews/{id}/status")
    fun toggleReviewStatus(
        @PathVariable id: String,
    ): StatusResponse {
        val review = reviewService.toggleStatus(id)
        return StatusResponse(review.id, review.status)
    }

    @PatchMapping("/comments/{id}/status")
    fun toggleCommentStatus(
        @PathVariable id: String,
    ): StatusResponse {
        val comment = commentService.toggleStatus(id)
        return StatusResponse(comment.id, comment.status)
    }
}
