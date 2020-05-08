package com.approvers.devlazaApi.domain.repository

import com.approvers.devlazaApi.domain.data.User
import java.util.UUID

interface UserRepository {
    fun get(id: UUID): User?
    fun getWithMailAddress(mailAddress: String): User?
    fun getAll(limit: Int): List<User>
    fun delete(id: UUID)
    fun create(user: User): User
    fun update(user: User): User

    fun existsByMailAddress(mailAddress: String): Boolean
}
