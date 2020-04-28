package com.approvers.devlazaApi.request.project

import com.approvers.devlazaApi.database.table.ProjectsTable
import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime
import java.util.*

data class NewProjectRequest(
        val name: String,
        val introduction: String,
        @JsonFormat(pattern = ProjectsTable.DATETIME_PATTERN)
        val createdAt: LocalDateTime,
        val owner: UUID,
        val recruitingState: String
)