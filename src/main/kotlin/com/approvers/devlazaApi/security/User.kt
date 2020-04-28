package com.approvers.devlazaApi.security

import com.approvers.devlazaApi.database.entity.UserEntity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.UUID
import org.springframework.security.core.userdetails.User as SpringUser

class User(
        username: String,
        password: String,
        val id: UUID,
        val mailAddress: String,
        roles: Collection<GrantedAuthority>
) : SpringUser(username, password, roles)

fun UserEntity.toUser() : User = User(
        username = this.displayId,
        password = this.password,
        id = this.id.value,
        mailAddress = this.mailAddress,
        // TODO: roleを管理する列の追加
        roles = listOf(SimpleGrantedAuthority("USER"))
)