package dev.approvers.devlazaApi.domain.service

import dev.approvers.devlazaApi.domain.data.Project
import dev.approvers.devlazaApi.domain.data.ProjectSearchOption
import dev.approvers.devlazaApi.domain.repository.ProjectRepository
import dev.approvers.devlazaApi.domain.repository.UserRepository
import dev.approvers.devlazaApi.error.BadRequest
import dev.approvers.devlazaApi.error.Forbidden
import dev.approvers.devlazaApi.error.NotFound
import dev.approvers.devlazaApi.util.toUuid
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class ProjectService(
    private val projectRepository: ProjectRepository,
    private val userRepository: UserRepository
) {
    fun get(id: String): Project {
        val projectId = id.toUuid()
        return projectRepository.get(projectId) ?: throw NotFound("Project not found.")
    }

    fun getAll(limit: Int): List<Project> = projectRepository.getAll(limit)

    fun create(project: Project): Project = projectRepository.create(project)

    fun delete(id: String, userId: UUID, isAdmin: Boolean) {
        val uuid = id.toUuid()
        val project = projectRepository.get(uuid) ?: throw NotFound("Project not found.")

        if (!isAdmin && project.owner.id != userId) throw Forbidden("Permission denied.")

        projectRepository.delete(uuid)
    }

    fun search(block: ProjectSearchOption.() -> Unit): List<Project> {
        val option = ProjectSearchOption().apply(block)

        return projectRepository.search(
            sort = option.sort,
            user = option.user,
            tags = option.tags,
            recruitingState = option.recruiting,
            name = option.name,
            createdBefore = option.createdBefore,
            createdAfter = option.createdAfter,
            start = option.start,
            end = option.end
        )
    }

    fun addMember(projectId: String, userId: String, isAdmin: Boolean): Project {
        val projectUuid = projectId.toUuid()
        val userUuid = userId.toUuid()

        val project = projectRepository.get(projectUuid) ?: throw NotFound("Project not found.")
        val user = userRepository.get(userUuid) ?: throw NotFound("User not found.")

        if (!isAdmin && project.owner.id != userUuid) throw Forbidden("Permission denied.")

        return projectRepository.addMember(project, user)
    }

    fun deleteMember(projectId: String, userId: String, isAdmin: Boolean): Project {
        val projectUuid = projectId.toUuid()
        val userUuid = userId.toUuid()

        val project = projectRepository.get(projectUuid) ?: throw NotFound("Project not found.")
        val user = userRepository.get(userUuid) ?: throw BadRequest("User not found.")

        if (!isAdmin && project.owner.id != userUuid) throw Forbidden("Permission denied.")

        return projectRepository.deleteMember(project, user)
    }

    fun addFavorite(projectId: String, userId: UUID): Project {
        val projectUuid = projectId.toUuid()

        val project = projectRepository.get(projectUuid) ?: throw NotFound("Project not found.")
        val user = userRepository.get(userId) ?: throw NotFound("User not found.")

        return projectRepository.addFavorite(project, user)
    }

    fun removeFavorite(projectId: String, userId: UUID): Project {
        val projectUuid = projectId.toUuid()

        val project = projectRepository.get(projectUuid) ?: throw NotFound("Project not found.")
        val user = userRepository.get(userId) ?: throw NotFound("User not found.")

        return projectRepository.deleteFavorite(project, user)
    }
}
