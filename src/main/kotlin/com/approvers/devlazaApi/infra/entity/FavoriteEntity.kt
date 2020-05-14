package com.approvers.devlazaApi.infra.entity

import com.approvers.devlazaApi.infra.table.FavoritesTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class FavoriteEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<FavoriteEntity>(FavoritesTable)

    var user by UserEntity referencedOn FavoritesTable.user
    var project by ProjectEntity referencedOn FavoritesTable.project
}
