package com.woocurlee.bookviewadmin.config

import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper
import org.springframework.data.mongodb.core.convert.MappingMongoConverter

/**
 * MongoDB 매핑 설정.
 * bookview 본 서비스와 동일한 컬렉션을 공유하므로, 문서에 `_class` 필드를 쓰지 않도록 제거한다.
 */
@Configuration
class MongoConfig(
    private val mappingMongoConverter: MappingMongoConverter,
) {
    @PostConstruct
    fun removeClassField() {
        // `_class` 타입 힌트 필드 제거
        mappingMongoConverter.setTypeMapper(DefaultMongoTypeMapper(null))
    }
}
