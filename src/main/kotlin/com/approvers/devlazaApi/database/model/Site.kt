package com.approvers.devlazaApi.database.model

import com.approvers.devlazaApi.database.table.SitesTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class Site(id: EntityID<UUID>): UUIDEntity(id) {
    companion object: UUIDEntityClass<Site>(SitesTable)

    var url by SitesTable.url
    var description by SitesTable.description
    var project by SitesTable.project
}