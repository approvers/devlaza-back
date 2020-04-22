package com.approvers.devlazaApi.database.entity

import com.approvers.devlazaApi.database.table.FavoritesTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class FavoriteEntity(id: EntityID<UUID>): UUIDEntity(id) {
    companion object: UUIDEntityClass<FavoriteEntity>(FavoritesTable)

    var user by FavoritesTable.user
    var project by FavoritesTable.project
}