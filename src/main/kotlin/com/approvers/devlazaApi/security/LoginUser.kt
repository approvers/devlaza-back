package com.approvers.devlazaApi.security

import com.approvers.devlazaApi.errors.BadRequest
import org.springframework.security.core.GrantedAuthority
import java.util.UUID
import org.springframework.security.core.userdetails.User as SpringUser
import com.approvers.devlazaApi.domain.data.User as DataUser

class LoginUser(
        username: String,
        password: String,
        val id: UUID,
        val mailAddress: String,
        roles: Collection<GrantedAuthority>
) : SpringUser(username, password, roles)

fun DataUser.toLoginUser(): LoginUser {
    return LoginUser(
            username = displayId,
            password = password,
            id = id ?: throw BadRequest("User id must not be null"),
            mailAddress = mailAddress,
            roles = role.toAuthorities()
    )
}