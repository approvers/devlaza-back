package com.approvers.devlazaApi.infra.repository.impl

import com.approvers.devlazaApi.domain.data.User
import com.approvers.devlazaApi.domain.repository.NonAuthorizedUserRepository
import com.approvers.devlazaApi.error.BadRequest
import com.approvers.devlazaApi.error.NotFound
import com.approvers.devlazaApi.infra.entity.NonAuthorizedUserEntity
import com.approvers.devlazaApi.infra.entity.apply
import com.approvers.devlazaApi.infra.entity.toData
import com.approvers.devlazaApi.infra.table.NonAuthorizedUsersTable
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class NonAuthorizedUserRepositoryImpl : NonAuthorizedUserRepository {
    override fun get(id: UUID): User? {
        return NonAuthorizedUserEntity.findById(id)
            ?.toData()
    }

    override fun getWithMailAddress(mailAddress: String): User? {
        return NonAuthorizedUserEntity.find { NonAuthorizedUsersTable.mailAddress eq mailAddress }
            .singleOrNull()
            ?.toData()
    }

    override fun delete(id: UUID) {
        NonAuthorizedUserEntity.findById(id)?.delete()
    }

    override fun create(user: User): User {
        return NonAuthorizedUserEntity.new {
            apply(user)
        }.toData()
    }

    override fun update(user: User): User {
        user.id ?: throw BadRequest("User id must not be null.")
        return NonAuthorizedUserEntity.findById(user.id)
            ?.apply(user)
            ?.toData()
            ?: throw NotFound("User not found!")
    }
}
