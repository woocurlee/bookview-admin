package com.woocurlee.bookviewadmin.controller

import com.woocurlee.bookviewadmin.domain.Status
import com.woocurlee.bookviewadmin.service.AdminCommentService
import com.woocurlee.bookviewadmin.service.AdminReviewService
import com.woocurlee.bookviewadmin.service.AdminUserService
import com.woocurlee.bookviewadmin.service.ReviewRow
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 어드민 상태 변경 REST API.
 * ACTIVE → BLOCK: reason 필수. BLOCK → ACTIVE: body 불필요.
 */
data class BlockRequest(
    val reason: String? = null,
)

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
        @RequestBody(required = false) body: BlockRequest?,
    ): StatusResponse {
        val user = userService.toggleStatus(id, body?.reason)
        return StatusResponse(user.id, user.status)
    }

    @GetMapping("/reviews/{id}")
    fun getReview(
        @PathVariable id: String,
    ): ReviewRow = reviewService.findById(id)

    @PatchMapping("/reviews/{id}/status")
    fun toggleReviewStatus(
        @PathVariable id: String,
        @RequestBody(required = false) body: BlockRequest?,
    ): StatusResponse {
        val review = reviewService.toggleStatus(id, body?.reason)
        return StatusResponse(review.id, review.status)
    }

    @PatchMapping("/comments/{id}/status")
    fun toggleCommentStatus(
        @PathVariable id: String,
        @RequestBody(required = false) body: BlockRequest?,
    ): StatusResponse {
        val comment = commentService.toggleStatus(id, body?.reason)
        return StatusResponse(comment.id, comment.status)
    }
}
