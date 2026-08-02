package com.woocurlee.bookviewadmin.repository

import com.woocurlee.bookviewadmin.domain.Review
import com.woocurlee.bookviewadmin.domain.Status
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

interface ReviewRepository : MongoRepository<Review, String> {
    // bookTitle 또는 bookAuthor 부분일치 (대소문자 무시)
    @Query(
        "{ '\$or': [ { 'bookTitle': { '\$regex': ?0, '\$options': 'i' } }, { 'bookAuthor': { '\$regex': ?0, '\$options': 'i' } } ] }",
    )
    fun search(
        keyword: String,
        pageable: Pageable,
    ): Page<Review>

    // status 필터 + 키워드 검색
    @Query(
        "{ 'status': ?1, '\$or': [ { 'bookTitle': { '\$regex': ?0, '\$options': 'i' } }, { 'bookAuthor': { '\$regex': ?0, '\$options': 'i' } } ] }",
    )
    fun searchByStatus(
        keyword: String,
        status: Status,
        pageable: Pageable,
    ): Page<Review>

    fun findByStatus(
        status: Status,
        pageable: Pageable,
    ): Page<Review>

    fun countByStatus(status: Status): Long

    // 대시보드: 최근 작성 리뷰
    fun findTop5ByOrderByCreatedAtDesc(): List<Review>
}
