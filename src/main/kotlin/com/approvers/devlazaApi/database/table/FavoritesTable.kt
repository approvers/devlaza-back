package com.approvers.devlazaApi.database.table

import org.jetbrains.exposed.dao.id.UUIDTable

object FavoritesTable : UUIDTable() {
    val user = reference("user", UsersTable).index()
    val project = reference("project", ProjectsTable).index()
}