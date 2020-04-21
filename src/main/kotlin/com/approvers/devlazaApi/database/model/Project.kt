package com.approvers.devlazaApi.database.model

import com.approvers.devlazaApi.database.table.ProjectMembersTable
import com.approvers.devlazaApi.database.table.ProjectsTable
import com.approvers.devlazaApi.database.table.SitesTable
import com.approvers.devlazaApi.database.table.TagsWithProjects
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class Project(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object: UUIDEntityClass<Project>(ProjectsTable)

    var name by ProjectsTable.name
    var introduction by ProjectsTable.introduction
    var createdAt by ProjectsTable.createdAt
    var owner by ProjectsTable.owner
    var recruitingState by ProjectsTable.recruitingState

    val sites by Site referrersOn  SitesTable.project
    val tags by Tag referrersOn TagsWithProjects.project
    val members by User referrersOn ProjectMembersTable.project
}