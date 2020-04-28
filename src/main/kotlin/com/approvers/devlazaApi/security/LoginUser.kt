package com.approvers.devlazaApi.security

import com.approvers.devlazaApi.error.BadRequest
import org.springframework.security.core.GrantedAuthority
import java.util.UUID
import com.approvers.devlazaApi.domain.data.User as DataUser
import org.springframework.security.core.userdetails.User as SpringUser

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