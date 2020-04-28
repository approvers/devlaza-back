package com.approvers.devlazaApi.infra.table

import org.jetbrains.exposed.sql.Table

object ProjectMembersTable : Table() {
    val project = reference("project", ProjectsTable).index()
    val user = reference("user", UsersTable).index()
}
