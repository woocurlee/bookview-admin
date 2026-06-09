package com.woocurlee.bookviewadmin.service

import com.woocurlee.bookviewadmin.domain.Status
import com.woocurlee.bookviewadmin.repository.CommentRepository
import com.woocurlee.bookviewadmin.repository.ReviewRepository
import com.woocurlee.bookviewadmin.repository.UserRepository
import java.time.LocalDate
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class DashboardServiceTest {
    private val userRepository: UserRepository = mock()
    private val reviewRepository: ReviewRepository = mock()
    private val commentRepository: CommentRepository = mock()

    private val service =
        DashboardService(userRepository, reviewRepository, commentRepository)

    @Test
    fun `stats 는 각 컬렉션의 ACTIVE 개수와 오늘 가입 수를 집계한다`() {
        // given
        whenever(userRepository.countByStatus(Status.ACTIVE)).thenReturn(42L)
        whenever(reviewRepository.countByStatus(Status.ACTIVE)).thenReturn(17L)
        whenever(commentRepository.countByStatus(Status.ACTIVE)).thenReturn(8L)
        whenever(userRepository.countByCreatedAtGreaterThanEqual(any())).thenReturn(3L)

        // when
        val stats = service.stats()

        // then
        assertEquals(42L, stats.activeUserCount)
        assertEquals(17L, stats.activeReviewCount)
        assertEquals(8L, stats.activeCommentCount)
        assertEquals(3L, stats.todaySignupCount)
    }

    @Test
    fun `countTodaySignups 는 오늘 00시 기준으로 카운트를 위임한다`() {
        // given
        val todayStart = LocalDate.now().atStartOfDay()
        whenever(userRepository.countByCreatedAtGreaterThanEqual(todayStart)).thenReturn(5L)

        // when
        val count = service.countTodaySignups(todayStart)

        // then
        assertEquals(5L, count)
    }
}
