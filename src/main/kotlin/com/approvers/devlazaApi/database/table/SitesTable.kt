package com.approvers.devlazaApi.database.table

import org.jetbrains.exposed.dao.id.UUIDTable

object SitesTable : UUIDTable() {
    val url = varchar("url", 200).nullable()
    val description = varchar("description", 400)
    val project = reference("project", ProjectsTable)
}