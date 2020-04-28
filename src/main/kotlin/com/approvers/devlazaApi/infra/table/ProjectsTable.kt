package com.approvers.devlazaApi.infra.table

import com.fasterxml.jackson.annotation.JsonFormat
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.`java-time`.datetime

object ProjectsTable: UUIDTable() {
    val name = varchar("name", 20)
    val introduction = varchar("introduction", 100)
    @JsonFormat(pattern = DATETIME_PATTERN)
    val createdAt = datetime("created_at")
    val owner = reference("owner", UsersTable)
    val recruitingState = enumeration("recruiting_state", RecruitingState::class)

    const val DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss"
}

enum class RecruitingState {
    RECRUITING,
    CLOSED;
}