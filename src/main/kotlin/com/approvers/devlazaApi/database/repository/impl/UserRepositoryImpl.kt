package com.approvers.devlazaApi.database.repository.impl

import com.approvers.devlazaApi.data.User
import com.approvers.devlazaApi.data.toData
import com.approvers.devlazaApi.database.entity.UserEntity
import com.approvers.devlazaApi.database.repository.UserRepository
import com.approvers.devlazaApi.database.table.AuthorizationState
import com.approvers.devlazaApi.database.table.UserRole
import com.approvers.devlazaApi.database.table.UsersTable
import com.approvers.devlazaApi.errors.NotFound
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.*

@Service
class UserRepositoryImpl : UserRepository {
    @Transactional
    override fun get(id: UUID): UserEntity {
        return UserEntity.findById(id) ?: throw NotFound("User not found.")
    }

    @Transactional
    override fun getAll(limit: Int): List<UserEntity> {
        return UserEntity.all()
                .limit(limit)
                .sortedBy { it.name }
    }

    override fun getWithMailAddress(mailAddress: String): UserEntity? {
        return UserEntity.find { UsersTable.mailAddress eq mailAddress }
                .singleOrNull()
    }

    @Transactional
    override fun create(
            name: String,
            birthday: LocalDate?,
            bio: String?,
            favoriteLang: String,
            password: String,
            displayId: String,
            mailAuthorizedState: AuthorizationState,
            mailAddress: String,
            role: UserRole
    ): User {
        return UserEntity.new {
            this.name = name
            this.birthday = birthday
            this.bio = bio
            this.favoriteLang = favoriteLang
            this.password = password
            this.displayId = displayId
            this.mailAuthorizeState = mailAuthorizeState
            this.mailAddress = mailAddress
            this.role = role
        }.toData()
    }

    @Transactional
    override fun delete(id: UUID) {
        UserEntity.findById(id)?.delete()
    }

    @Transactional
    override fun update(user: User): User {
        return UserEntity.findById(user.id)?.apply {
            name = user.name
            birthday = user.birthday
            bio = user.bio
            favoriteLang = user.favoriteLang
            password = user.password
            displayId = user.displayId
            mailAuthorizeState = user.mailAuthorizeState
            mailAddress = user.mailAddress
            role = user.role
        }?.toData() ?: throw NotFound("User not found")
    }
}