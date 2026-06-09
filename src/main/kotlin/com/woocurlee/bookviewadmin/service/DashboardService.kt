package com.woocurlee.bookviewadmin.service

import com.woocurlee.bookviewadmin.domain.Review
import com.woocurlee.bookviewadmin.domain.Status
import com.woocurlee.bookviewadmin.domain.User
import com.woocurlee.bookviewadmin.repository.CommentRepository
import com.woocurlee.bookviewadmin.repository.ReviewRepository
import com.woocurlee.bookviewadmin.repository.UserRepository
import java.time.LocalDate
import java.time.LocalDateTime
import org.springframework.stereotype.Service

/**
 * 대시보드 통계 카드 값.
 */
data class DashboardStats(
    val activeUserCount: Long,
    val activeReviewCount: Long,
    val activeCommentCount: Long,
    val todaySignupCount: Long,
)

@Service
class DashboardService(
    private val userRepository: UserRepository,
    private val reviewRepository: ReviewRepository,
    private val commentRepository: CommentRepository,
) {
    /**
     * 통계 카드 4개 값을 계산한다.
     * 오늘 가입 유저 수는 createdAt 이 오늘 00:00 이후인 유저 수.
     */
    fun stats(): DashboardStats {
        val todayStart = LocalDate.now().atStartOfDay()
        return DashboardStats(
            activeUserCount = userRepository.countByStatus(Status.ACTIVE),
            activeReviewCount = reviewRepository.countByStatus(Status.ACTIVE),
            activeCommentCount = commentRepository.countByStatus(Status.ACTIVE),
            todaySignupCount = userRepository.countByCreatedAtGreaterThanEqual(todayStart),
        )
    }

    /** 오늘 가입 유저 수 (테스트용으로 기준 시각 주입 가능). */
    fun countTodaySignups(todayStart: LocalDateTime = LocalDate.now().atStartOfDay()): Long =
        userRepository.countByCreatedAtGreaterThanEqual(todayStart)

    fun recentUsers(): List<User> = userRepository.findTop5ByOrderByCreatedAtDesc()

    fun recentReviews(): List<Review> = reviewRepository.findTop5ByOrderByCreatedAtDesc()
}
