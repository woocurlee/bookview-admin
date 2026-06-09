package com.woocurlee.bookviewadmin.controller

import com.woocurlee.bookviewadmin.service.AdminCommentService
import com.woocurlee.bookviewadmin.service.AdminReviewService
import com.woocurlee.bookviewadmin.service.AdminUserService
import com.woocurlee.bookviewadmin.service.DashboardService
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * 어드민 화면 렌더링 컨트롤러 (Thymeleaf).
 */
@Controller
class ViewController(
    private val dashboardService: DashboardService,
    private val userService: AdminUserService,
    private val reviewService: AdminReviewService,
    private val commentService: AdminCommentService,
    @Value("\${bookview.base-url}")
    private val bookviewBaseUrl: String,
) {
    @GetMapping("/")
    fun dashboard(model: Model): String {
        model.addAttribute("active", "dashboard")
        model.addAttribute("stats", dashboardService.stats())
        model.addAttribute("recentUsers", dashboardService.recentUsers())
        model.addAttribute("recentReviews", dashboardService.recentReviews())
        return "dashboard"
    }

    @GetMapping("/users")
    fun users(
        @RequestParam(required = false) q: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        model: Model,
    ): String {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        model.addAttribute("active", "users")
        model.addAttribute("q", q ?: "")
        model.addAttribute("page", userService.list(q, pageable))
        return "users"
    }

    @GetMapping("/reviews")
    fun reviews(
        @RequestParam(required = false) q: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        model: Model,
    ): String {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        model.addAttribute("active", "reviews")
        model.addAttribute("q", q ?: "")
        model.addAttribute("page", reviewService.list(q, pageable))
        model.addAttribute("bookviewBaseUrl", bookviewBaseUrl)
        return "reviews"
    }

    @GetMapping("/comments")
    fun comments(
        @RequestParam(required = false) q: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        model: Model,
    ): String {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        model.addAttribute("active", "comments")
        model.addAttribute("q", q ?: "")
        model.addAttribute("page", commentService.list(q, pageable))
        return "comments"
    }
}
