package dev.approvers.devlazaApi.infra.table

import org.jetbrains.exposed.dao.id.UUIDTable

object DeveloperExperienceTable : UUIDTable() {
    val user = reference("user", UsersTable).index()
    val description = varchar("description", 200)
}
