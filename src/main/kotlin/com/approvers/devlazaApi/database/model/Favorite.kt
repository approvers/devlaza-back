package com.approvers.devlazaApi.database.model

import com.approvers.devlazaApi.database.table.FavoritesTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class Favorite(id: EntityID<UUID>): UUIDEntity(id) {
    companion object: UUIDEntityClass<Favorite>(FavoritesTable)

    var user by FavoritesTable.user
    var project by FavoritesTable.project
}