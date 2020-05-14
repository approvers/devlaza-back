package com.approvers.devlazaApi.infra.entity

import com.approvers.devlazaApi.infra.table.ProjectMembersTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class ProjectMemberEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ProjectMemberEntity>(ProjectMembersTable)

    var project by ProjectEntity referencedOn ProjectMembersTable.project
    var user by UserEntity referencedOn ProjectMembersTable.user
}
