package com.approvers.devlazaApi.database.repository.impl

import com.approvers.devlazaApi.data.User
import com.approvers.devlazaApi.data.toData
import com.approvers.devlazaApi.database.entity.UserEntity
import com.approvers.devlazaApi.database.repository.UserRepository
import com.approvers.devlazaApi.database.table.UsersTable
import com.approvers.devlazaApi.errors.BadRequest
import com.approvers.devlazaApi.errors.NotFound
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