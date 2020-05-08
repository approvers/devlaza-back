package com.approvers.devlazaApi.domain.repository

import com.approvers.devlazaApi.domain.data.NonAuthorizedUser
import com.approvers.devlazaApi.domain.data.User
import java.util.UUID

interface NonAuthorizedUserRepository {
    fun get(id: UUID): NonAuthorizedUser?
    fun getWithMailAddress(mailAddress: String): NonAuthorizedUser?
    fun getWithToken(token: UUID): NonAuthorizedUser?
    fun delete(id: UUID)
    fun create(user: User): NonAuthorizedUser
    fun update(user: User): NonAuthorizedUser?

    fun existsByMailAddress(mailAddress: String): Boolean
}