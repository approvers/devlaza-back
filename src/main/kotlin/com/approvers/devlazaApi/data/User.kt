package com.approvers.devlazaApi.data

import com.approvers.devlazaApi.database.entity.UserEntity
import com.approvers.devlazaApi.database.table.AuthorizationState
import com.approvers.devlazaApi.database.table.UserRole
import java.time.LocalDate
import java.util.*

data class User(
        val id: UUID,
        var name: String,
        var birthday: LocalDate?,
        var bio: String?,
        var favoriteLang: String?,
        var password: String,
        var displayId: String,
        var mailAuthorizeState: AuthorizationState,
        var mailAddress: String,
        var role: UserRole
)

fun UserEntity.toData(): User {
    return User(
            id = this.id.value,
            name = this.name,
            birthday = this.birthday,
            bio = this.bio,
            favoriteLang = this.favoriteLang,
            password = this.password,
            displayId = this.displayId,
            mailAuthorizeState = this.mailAuthorizeState,
            mailAddress = this.mailAddress,
            role = this.role
    )
}