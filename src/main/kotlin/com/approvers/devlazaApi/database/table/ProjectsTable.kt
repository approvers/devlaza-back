package com.approvers.devlazaApi.database.table

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.`java-time`.datetime

object ProjectsTable: UUIDTable() {
    val name = varchar("name", 20)
    val introduction = varchar("introduction", 100)
    val createdAt = datetime("created_at")
    val owner = reference("owner", UsersTable)
    val recruitingState = enumeration("recruiting_state", RecruitingState::class)
}

enum class RecruitingState {
    RECRUITING,
    CLOSED
}