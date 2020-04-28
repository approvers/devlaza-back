package com.approvers.devlazaApi.infra.entity

import com.approvers.devlazaApi.infra.table.TagsTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class TagEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<TagEntity>(TagsTable)

    var name by TagsTable.name
}