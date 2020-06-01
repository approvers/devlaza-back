package dev.approvers.devlazaApi.infra.table

import org.jetbrains.exposed.dao.id.UUIDTable

object ProjectMembersTable : UUIDTable() {
    val project = reference("project", ProjectsTable).index()
    val user = reference("user", UsersTable).index()
}
