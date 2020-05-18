package dev.approvers.devlazaApi.infra.table

import org.jetbrains.exposed.dao.id.UUIDTable

object FavoritesTable : UUIDTable() {
    val user = reference("user", UsersTable).index()
    val project = reference("project", ProjectsTable).index()
}
