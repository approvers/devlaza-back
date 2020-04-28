package com.approvers.devlazaApi.infra.repository.impl

import com.approvers.devlazaApi.domain.data.Project
import com.approvers.devlazaApi.domain.data.User
import com.approvers.devlazaApi.domain.data.toData
import com.approvers.devlazaApi.domain.repository.ProjectRepository
import com.approvers.devlazaApi.error.BadRequest
import com.approvers.devlazaApi.error.NotFound
import com.approvers.devlazaApi.infra.entity.ProjectEntity
import com.approvers.devlazaApi.infra.entity.UserEntity
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Repository
class ProjectRepositoryImpl : ProjectRepository {
    @Transactional
    override fun get(id: UUID): Project? {
        return ProjectEntity.findById(id)
                ?.toData()
    }

    @Transactional
    override fun getAll(limit: Int): List<Project> {
        return ProjectEntity.all()
                .limit(limit)
                .sortedBy { it.createdAt }
                .map(ProjectEntity::toData)
    }

    @Transactional
    override fun delete(id: UUID) {
        ProjectEntity.findById(id)?.delete()
    }

    @Transactional
    override fun create(project: Project): Project {
        return ProjectEntity.new {
            apply(project)
        }.toData()
    }

    @Transactional
    override fun update(project: Project): Project {
        project.id ?: throw BadRequest("Project id must not be null")
        return ProjectEntity.findById(project.id)
                ?.apply(project)
                ?.toData()
                ?: throw NotFound("Project not found.")
    }

    private fun User.toEntity(): UserEntity? {
        id ?: throw BadRequest("User id must not be null.")
        return UserEntity.findById(id)
    }

    private fun ProjectEntity.apply(project: Project): ProjectEntity = apply {
        name = project.name
        introduction = project.introduction
        createdAt = project.createdAt
        owner = project.owner.toEntity() ?: throw NotFound("Project owner not found.")
    }
}