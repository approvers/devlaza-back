package com.approvers.devlazaApi.security

import com.approvers.devlazaApi.domain.data.UserRole
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken

val UsernamePasswordAuthenticationToken.isAdmin: Boolean
    get() = this.authorities.containsAll(UserRole.ADMIN.toAuthorities())

val UsernamePasswordAuthenticationToken.user: LoginUser
    get() = this.principal as LoginUser
