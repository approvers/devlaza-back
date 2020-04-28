package com.approvers.devlazaApi.data

import com.approvers.devlazaApi.database.entity.ProjectEntity
import com.approvers.devlazaApi.database.table.RecruitingState
import java.time.LocalDateTime
import java.util.UUID

data class Project(
        val id: UUID? = null,
        var name: String,
        var introduction: String,
        val createdAt: LocalDateTime,
        var owner: User,
        var recruitingState: RecruitingState
)

fun ProjectEntity.toData(): Project {
    return Project(
            id = id.value,
            name = name,
            introduction = introduction,
            createdAt = createdAt,
            owner = owner.toData(),
            recruitingState = recruitingState
    )
}