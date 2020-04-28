package com.approvers.devlazaApi.infra.repository.impl

import com.approvers.devlazaApi.domain.data.User
import com.approvers.devlazaApi.domain.data.toData
import com.approvers.devlazaApi.domain.repository.UserRepository
import com.approvers.devlazaApi.error.BadRequest
import com.approvers.devlazaApi.error.NotFound
import com.approvers.devlazaApi.infra.entity.UserEntity
import com.approvers.devlazaApi.infra.table.UsersTable
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Repository
class UserRepositoryImpl : UserRepository {
    @Transactional
    override fun get(id: UUID): User {
        return UserEntity.findById(id)?.toData() ?: throw NotFound("User not found.")
    }

    @Transactional
    override fun getAll(limit: Int): List<User> {
        return UserEntity.all()
            .limit(limit)
            .sortedBy { it.name }
            .map(UserEntity::toData)
    }

    @Transactional
    override fun getWithMailAddress(mailAddress: String): User? {
        return UserEntity.find { UsersTable.mailAddress eq mailAddress }
            .singleOrNull()
            ?.toData()
    }

    @Transactional
    override fun create(user: User): User {
        return UserEntity.new {
            apply(user)
        }.toData()
    }

    @Transactional
    override fun delete(id: UUID) {
        UserEntity.findById(id)?.delete()
    }

    @Transactional
    override fun update(user: User): User {
        user.id ?: throw BadRequest("User id must not be null.")
        return UserEntity.findById(user.id)
            ?.apply(user)
            ?.toData()
            ?: throw NotFound("User not found")
    }

    private fun UserEntity.apply(user: User): UserEntity = apply {
        name = user.name
        birthday = user.birthday
        bio = user.bio
        favoriteLang = user.favoriteLang
        password = user.password
        displayId = user.displayId
        mailAuthorizeState = user.mailAuthorizeState
        mailAddress = user.mailAddress
        role = user.role
    }
}
