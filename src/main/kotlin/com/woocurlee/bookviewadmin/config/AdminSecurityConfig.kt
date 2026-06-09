package com.woocurlee.bookviewadmin.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.authorization.AuthorizationManager
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.intercept.RequestAuthorizationContext
import org.springframework.security.web.util.matcher.IpAddressMatcher

/**
 * 어드민 접근 제어 설정.
 *
 * 어드민은 외부에 노출하지 않고 Tailscale VPN 으로만 접속한다.
 * 따라서 앱 레벨 인증(로그인) 없이, 허용된 IP 대역에서 온 요청만 통과시키고
 * 그 외 모든 요청은 403 으로 차단한다.
 */
@Configuration
@EnableWebSecurity
class AdminSecurityConfig(
    @Value("\${admin.allowed-cidr}")
    private val allowedCidr: String,
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // 내부망 전용이므로 CSRF 비활성화
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth.anyRequest().access(ipAllowListManager())
            }
        // 세션리스 설정은 두지 않는다 — 어드민 편의를 위해 기본 세션 사용
        return http.build()
    }

    /**
     * 허용 CIDR(Tailscale 대역) + localhost(IPv4/IPv6) 에서 온 요청만 인가한다.
     */
    private fun ipAllowListManager(): AuthorizationManager<RequestAuthorizationContext> {
        val matchers =
            listOf(
                allowedCidr, // Tailscale CGNAT 대역 (예: 100.64.0.0/10)
                "127.0.0.1/32", // localhost IPv4
                "::1/128", // localhost IPv6
            ).map { IpAddressMatcher(it) }

        return AuthorizationManager { _, context ->
            val remoteAddr = context.request.remoteAddr
            val granted = matchers.any { it.matches(remoteAddr) }
            AuthorizationDecision(granted)
        }
    }
}
