package com.approvers.devlazaApi.database.repository

import com.approvers.devlazaApi.data.User
import com.approvers.devlazaApi.database.entity.UserEntity
import com.approvers.devlazaApi.database.table.AuthorizationState
import com.approvers.devlazaApi.database.table.UserRole
import java.time.LocalDate
import java.util.*

interface UserRepository {
    fun get(id: UUID): UserEntity?
    fun getWithMailAddress(mailAddress: String): UserEntity?
    fun getAll(limit: Int): List<UserEntity>
    fun delete(id: UUID)
    fun create(
            name: String,
            birthday: LocalDate?,
            bio: String?,
            favoriteLang: String,
            password: String,
            displayId: String,
            mailAuthorizedState: AuthorizationState,
            mailAddress: String,
            role: UserRole
    ): User
    fun update(user: User): User
}