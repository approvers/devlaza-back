package com.approvers.devlazaApi.database.entity

import com.approvers.devlazaApi.database.table.TagsTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class TagEntity (id: EntityID<UUID>): UUIDEntity(id) {
    companion object: UUIDEntityClass<TagEntity>(TagsTable)

    var name by TagsTable.name
}