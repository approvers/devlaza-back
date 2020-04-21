package com.approvers.devlazaApi.database.table

import org.jetbrains.exposed.sql.Table

object ProjectMembersTable : Table() {
    val project = reference("project", ProjectsTable).index()
    val user = reference("user", UsersTable).index()
}