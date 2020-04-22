package com.approvers.devlazaApi.database.entity

import com.approvers.devlazaApi.database.table.UsersTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class UserEntity(id: EntityID<UUID>): UUIDEntity(id) {
    companion object: UUIDEntityClass<UserEntity>(UsersTable)

    var name by UsersTable.name
    var birthday by UsersTable.birthday
    var bio by UsersTable.bio
    var favoriteLang by UsersTable.favoriteLang
    var password by UsersTable.password
    var displayId by UsersTable.displayId
    var mailAuthorizeState by UsersTable.mailAuthorizeState
    var mailAddress by UsersTable.mailAddress
    var role by UsersTable.role
}