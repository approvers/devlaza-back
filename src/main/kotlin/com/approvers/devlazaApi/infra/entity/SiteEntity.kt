package com.approvers.devlazaApi.infra.entity

import com.approvers.devlazaApi.infra.table.SitesTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class SiteEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<SiteEntity>(SitesTable)

    var url by SitesTable.url
    var description by SitesTable.description
    var project by SitesTable.project
}