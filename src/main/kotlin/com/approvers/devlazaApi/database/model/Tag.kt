package com.approvers.devlazaApi.database.model

import com.approvers.devlazaApi.database.table.TagsTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class Tag (id: EntityID<UUID>): UUIDEntity(id) {
    companion object: UUIDEntityClass<Tag>(TagsTable)

    var name by TagsTable.name
}