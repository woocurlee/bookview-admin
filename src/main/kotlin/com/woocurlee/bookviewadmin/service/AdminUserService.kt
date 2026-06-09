package com.woocurlee.bookviewadmin.service

import com.woocurlee.bookviewadmin.domain.Status
import com.woocurlee.bookviewadmin.domain.User
import com.woocurlee.bookviewadmin.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class AdminUserService(
    private val userRepository: UserRepository,
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
     * 상태 토글: ACTIVE ↔ DELETED.
     */
    fun toggleStatus(id: String): User {
        val user = userRepository.findById(id).orElseThrow { NoSuchElementException("user not found: $id") }
        val next = if (user.status == Status.ACTIVE) Status.DELETED else Status.ACTIVE
        return userRepository.save(user.copy(status = next))
    }
}
