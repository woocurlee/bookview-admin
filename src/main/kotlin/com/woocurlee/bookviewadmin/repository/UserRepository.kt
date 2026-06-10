package com.woocurlee.bookviewadmin.repository

import com.woocurlee.bookviewadmin.domain.Status
import com.woocurlee.bookviewadmin.domain.User
import java.time.LocalDateTime
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

interface UserRepository : MongoRepository<User, String> {
    // nickname 또는 email 부분일치 (대소문자 무시)
    @Query(
        "{ '\$or': [ { 'nickname': { '\$regex': ?0, '\$options': 'i' } }, { 'email': { '\$regex': ?0, '\$options': 'i' } } ] }",
    )
    fun search(
        keyword: String,
        pageable: Pageable,
    ): Page<User>

    fun countByStatus(status: Status): Long

    fun countByCreatedAtGreaterThanEqual(from: LocalDateTime): Long

    // 대시보드: 최근 가입 유저
    fun findTop5ByOrderByCreatedAtDesc(): List<User>

    fun findAllByGoogleIdIn(googleIds: List<String>): List<User>
}
