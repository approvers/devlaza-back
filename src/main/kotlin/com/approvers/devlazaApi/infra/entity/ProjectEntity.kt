package com.approvers.devlazaApi.infra.entity

import com.approvers.devlazaApi.domain.data.Project
import com.approvers.devlazaApi.infra.table.ProjectMembersTable
import com.approvers.devlazaApi.infra.table.ProjectsTable
import com.approvers.devlazaApi.infra.table.SitesTable
import com.approvers.devlazaApi.infra.table.TagsWithProjects
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class ProjectEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ProjectEntity>(ProjectsTable)

    var name by ProjectsTable.name
    var introduction by ProjectsTable.introduction
    var createdAt by ProjectsTable.createdAt
    var owner by UserEntity referencedOn ProjectsTable.owner
    var recruitingState by ProjectsTable.recruitingState

    val sites by SiteEntity referrersOn SitesTable.project
    val tags by TagEntity referrersOn TagsWithProjects.project
    val members by UserEntity referrersOn ProjectMembersTable.project
}

fun ProjectEntity.toData(): Project = Project(
    id = id.value,
    name = name,
    introduction = introduction,
    createdAt = createdAt,
    owner = owner.toData(),
    recruitingState = recruitingState
)
