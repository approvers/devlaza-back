package dev.approvers.devlazaApi.infra.entity

import dev.approvers.devlazaApi.infra.table.UsersTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class UserEntity(id: EntityID<UUID>) : UUIDEntity(id), UserLikeEntity {
    companion object : UUIDEntityClass<UserEntity>(UsersTable)

    override val userId: UUID
        get() = id.value

    override var name by UsersTable.name
    override var birthday by UsersTable.birthday
    override var bio by UsersTable.bio
    override var favoriteLang by UsersTable.favoriteLang
    override var password by UsersTable.password
    override var displayId by UsersTable.displayId
    override var mailAddress by UsersTable.mailAddress
    override var role by UsersTable.role
}
