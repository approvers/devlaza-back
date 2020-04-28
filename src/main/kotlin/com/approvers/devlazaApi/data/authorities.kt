package com.approvers.devlazaApi.data

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority

enum class AuthorizationState {
    NO_AUTHORIZED,
    VERIFIED,
}

enum class UserRole(
        private val level: Int,
        private val authority : GrantedAuthority
) {
    USER(1, SimpleGrantedAuthority("ROLE_USER")),
    ADMIN(10, SimpleGrantedAuthority("ROLE_ADMIN")),;

    fun toAuthorities(): List<GrantedAuthority> {
        return values().filter { it.level <= level }
                .map(UserRole::authority)
    }
}