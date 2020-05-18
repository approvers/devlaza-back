package dev.approvers.devlazaApi.infra.entity

import dev.approvers.devlazaApi.domain.data.NonAuthorizedUser
import dev.approvers.devlazaApi.infra.table.NonAuthorizedUsersTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class NonAuthorizedUserEntity(id: EntityID<UUID>) : UUIDEntity(id), UserLikeEntity {
    companion object : UUIDEntityClass<NonAuthorizedUserEntity>(NonAuthorizedUsersTable)

    override val userId: UUID
        get() = id.value

    override var name by NonAuthorizedUsersTable.name
    override var birthday by NonAuthorizedUsersTable.birthday
    override var bio by NonAuthorizedUsersTable.bio
    override var favoriteLang by NonAuthorizedUsersTable.favoriteLang
    override var password by NonAuthorizedUsersTable.password
    override var displayId by NonAuthorizedUsersTable.displayId
    override var mailAddress by NonAuthorizedUsersTable.mailAddress
    override var role by NonAuthorizedUsersTable.role

    var token by NonAuthorizedUsersTable.token
}

fun NonAuthorizedUserEntity.toData() = NonAuthorizedUser(
    user = (this as UserLikeEntity).toData(),
    token = this.token
)
