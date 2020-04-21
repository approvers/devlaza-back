package com.approvers.devlazaApi.database.table

import org.jetbrains.exposed.dao.id.UUIDTable

object FollowsTable : UUIDTable() {
    val user = reference("user", UsersTable).index()
    val followed = reference("followed", UsersTable).index()
}