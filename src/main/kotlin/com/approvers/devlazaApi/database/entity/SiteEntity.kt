package com.approvers.devlazaApi.database.entity

import com.approvers.devlazaApi.database.table.SitesTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class SiteEntity(id: EntityID<UUID>): UUIDEntity(id) {
    companion object: UUIDEntityClass<SiteEntity>(SitesTable)

    var url by SitesTable.url
    var description by SitesTable.description
    var project by SitesTable.project
}