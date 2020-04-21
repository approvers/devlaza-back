package com.approvers.devlazaApi.database.table

import org.jetbrains.exposed.dao.id.UUIDTable

object DeveloperExperience : UUIDTable() {
    val user = reference("user", UsersTable).index()
    val description = varchar("description", 200)
}