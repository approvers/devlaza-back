package com.approvers.devlazaApi.infra.repository.impl

import com.approvers.devlazaApi.domain.data.Project
import com.approvers.devlazaApi.domain.data.ProjectSearchOption
import com.approvers.devlazaApi.domain.data.User
import com.approvers.devlazaApi.domain.repository.ProjectRepository
import com.approvers.devlazaApi.error.Conflict
import com.approvers.devlazaApi.error.NotFound
import com.approvers.devlazaApi.infra.entity.FavoriteEntity
import com.approvers.devlazaApi.infra.entity.ProjectEntity
import com.approvers.devlazaApi.infra.entity.ProjectMemberEntity
import com.approvers.devlazaApi.infra.entity.UserEntity
import com.approvers.devlazaApi.infra.entity.toData
import com.approvers.devlazaApi.infra.table.FavoritesTable
import com.approvers.devlazaApi.infra.table.ProjectMembersTable
import com.approvers.devlazaApi.infra.table.ProjectsTable
import com.approvers.devlazaApi.infra.table.RecruitingState
import com.approvers.devlazaApi.infra.table.UsersTable
import com.approvers.devlazaApi.util.checkNotNull
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.`java-time`.date
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.wrapAsExpression
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.UUID

@Repository
class ProjectRepositoryImpl : ProjectRepository {
    override fun get(id: UUID): Project? {
        return ProjectEntity.findById(id)
            ?.toData()
    }

    override fun getAll(limit: Int): List<Project> {
        return ProjectEntity.all()
            .limit(limit)
            .sortedBy { it.createdAt }
            .map(ProjectEntity::toData)
    }

    override fun delete(id: UUID) {
        ProjectEntity.findById(id)?.delete()
    }

    override fun create(project: Project): Project {
        return ProjectEntity.new {
            apply(project)
        }.toData()
    }

    override fun update(project: Project): Project {
        project.id.checkNotNull("project")
        return ProjectEntity.findById(project.id)
            ?.apply(project)
            ?.toData()
            ?: throw NotFound("Project not found.")
    }

    private fun User.toEntity(): UserEntity? {
        id.checkNotNull("user")
        return UserEntity.findById(id)
    }

    private fun ProjectEntity.apply(project: Project): ProjectEntity = apply {
        name = project.name
        introduction = project.introduction
        createdAt = project.createdAt
        owner = project.owner.toEntity() ?: throw NotFound("Project owner not found.")
    }

    override fun search(
        sort: ProjectSearchOption.Sort,
        user: String?,
        tags: List<String>,
        recruitingState: RecruitingState,
        name: String?,
        createdBefore: LocalDate?,
        createdAfter: LocalDate?,
        start: Long,
        end: Long
    ): List<Project> {
        val query = ProjectsTable.innerJoin(UsersTable)
            .selectAll()
            .apply {
                if (user != null) andWhere { UsersTable.name eq user }
                andWhere { ProjectsTable.recruitingState eq recruitingState }
                if (name != null) andWhere { ProjectsTable.name like "%$name%" }
                if (createdBefore != null) andWhere { ProjectsTable.createdAt.date() lessEq createdBefore }
                if (createdAfter != null) andWhere { ProjectsTable.createdAt.date() greaterEq createdAfter }

                when (sort) {
                    ProjectSearchOption.Sort.ASC -> orderBy(ProjectsTable.createdAt to SortOrder.ASC)
                    ProjectSearchOption.Sort.DESC -> orderBy(ProjectsTable.createdAt to SortOrder.DESC)
                    ProjectSearchOption.Sort.POPULAR -> {
                        val order = wrapAsExpression<UUID>(
                            FavoritesTable.slice(FavoritesTable.project.count())
                                .select {
                                    ProjectsTable.id eq FavoritesTable.id
                                }
                        )
                        orderBy(order, SortOrder.ASC)
                    }
                }
            }
            .limit((end - start).toInt(), start)
            .withDistinct()

        return ProjectEntity.wrapRows(query)
            .toList()
            .filter {
                it.tags.map { tag -> tag.name }
                    .containsAll(tags)
            }
            .map(ProjectEntity::toData)
    }

    override fun addMember(project: Project, user: User): Project {
        project.id.checkNotNull("project")
        val projectEntity = ProjectEntity.findById(project.id) ?: throw NotFound("Project not found.")
        user.id.checkNotNull("User id is must not be null.")
        val userEntity = UserEntity.findById(user.id) ?: throw NotFound("User not found.")

        ProjectMemberEntity.find {
            ProjectMembersTable.project eq projectEntity.id and (ProjectMembersTable.user eq userEntity.id)
        }.let {
            if (!it.empty()) throw Conflict("User already added to this project.")
        }

        ProjectMemberEntity.new {
            this.project = projectEntity
            this.user = userEntity
        }

        projectEntity.refresh()
        return projectEntity.toData()
    }

    override fun deleteMember(project: Project, user: User): Project {
        project.id.checkNotNull("project")

        ProjectMemberEntity.find {
            ProjectMembersTable.project eq project.id and (ProjectMembersTable.user eq user.id)
        }.singleOrNull()
            ?.delete()

        return ProjectEntity.findById(project.id)
            ?.toData()
            ?: throw NotFound("Project not found.")
    }

    override fun addFavorite(project: Project, user: User): Project {
        project.id.checkNotNull("project")
        val projectEntity = ProjectEntity.findById(project.id) ?: throw NotFound("Project not found.")
        user.id.checkNotNull("User id is must not be null.")
        val userEntity = UserEntity.findById(user.id) ?: throw NotFound("User not found.")

        FavoriteEntity.find {
            FavoritesTable.project eq projectEntity.id and (FavoritesTable.user eq userEntity.id)
        }.let {
            if (!it.empty()) throw Conflict("User already favorite this project.")
        }

        FavoriteEntity.new {
            this.project = projectEntity
            this.user = userEntity
        }

        return ProjectEntity.findById(project.id)
            ?.toData()
            ?: throw NotFound("Project not found.")
    }

    override fun deleteFavorite(project: Project, user: User): Project {
        project.id.checkNotNull("project")
        val projectEntity = ProjectEntity.findById(project.id) ?: throw NotFound("Project not found.")
        user.id.checkNotNull("user")
        val userEntity = UserEntity.findById(user.id) ?: throw NotFound("User not found.")

        FavoriteEntity.find {
            FavoritesTable.project eq projectEntity.id and (FavoritesTable.user eq userEntity.id)
        }.singleOrNull()
            ?.delete()

        return ProjectEntity.findById(project.id)
            ?.toData()
            ?: throw NotFound("Project not found.")
    }
}
