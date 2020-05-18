package dev.approvers.devlazaApi.domain.repository

import dev.approvers.devlazaApi.domain.data.Project
import dev.approvers.devlazaApi.domain.data.ProjectSearchOption
import dev.approvers.devlazaApi.domain.data.User
import dev.approvers.devlazaApi.infra.table.RecruitingState
import java.time.LocalDate
import java.util.UUID

interface ProjectRepository {
    fun get(id: UUID): Project?
    fun getAll(limit: Int): List<Project>
    fun delete(id: UUID)
    fun create(project: Project): Project
    fun update(project: Project): Project

    fun search(
        sort: ProjectSearchOption.Sort,
        user: String?,
        tags: List<String>,
        recruitingState: RecruitingState,
        name: String?,
        createdBefore: LocalDate?,
        createdAfter: LocalDate?,
        start: Long,
        end: Long
    ): List<Project>

    fun addMember(project: Project, user: User): Project
    fun deleteMember(project: Project, user: User): Project

    fun addFavorite(project: Project, user: User): Project
    fun deleteFavorite(project: Project, user: User): Project
}
