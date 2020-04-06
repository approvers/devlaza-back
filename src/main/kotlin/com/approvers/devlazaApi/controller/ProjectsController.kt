package com.approvers.devlazaApi.controller

// 大量の拡張関数があるので*でimport
import com.approvers.devlazaApi.controller.utils.*
//大量に使っているので*import
import org.springframework.web.bind.annotation.*
//大量に使っているので*import
import com.approvers.devlazaApi.model.*
//大量に使っているので*import
import com.approvers.devlazaApi.repository.*

import com.approvers.devlazaApi.errors.BadRequest
import com.approvers.devlazaApi.errors.NotFound
import org.springframework.http.ResponseEntity
import java.util.UUID
import javax.validation.Valid

@RestController
@RequestMapping("/projects")
class ProjectsController(
        private val projectsRepository: ProjectsRepository,
        private val sitesController: SitesController,
        private val tagsToProjectsBridgeRepository: TagsToProjectsBridgeRepository,
        private val tagsRepository: TagsRepository,
        private val tokenRepository: TokenRepository,
        private val projectMemberRepository: ProjectMemberRepository,
        private val userRepository: UserRepository
){
    @GetMapping("/")
    fun getAllProjects(): List<Projects> = projectsRepository.findAll()

    @PostMapping("/")
    fun createNewProject(@Valid @RequestBody rawData: ProjectPoster): ResponseEntity<Projects>{
        val token: Token = tokenRepository.checkToken(rawData.token)
        val projects = Projects(
                name = rawData.name,
                introduction = rawData.introduction,
                createdUserId = token.userId
        )

        projectsRepository.save(projects)

        sitesController.saveSites(rawData.sites, projects.id!!)

        tagsToProjectsBridgeRepository.addTagToProject(rawData.tags, projects.id!!, tagsRepository)

        return ResponseEntity.ok(projects)
    }

    @PostMapping("/{id}/join")
    fun joinToProject(
            @Valid @RequestBody tokenPoster: TokenPoster,
            @PathVariable(value="id") rawId: String
    ): ResponseEntity<Unit>{
        val token: String = tokenPoster.token
        val userId: UUID = tokenRepository.getUserIdFromToken(token)

        val projectId: UUID = rawId.toUUID()

        if (userRepository.findById(userId).isEmpty()) throw NotFound("User with given token does not exist")
        if (projectsRepository.findById(projectId).isEmpty()) throw NotFound("Project with given token does not exists")

        if (projectMemberRepository.checkProjectMemberExist(userId, projectId)) throw BadRequest("User already join to this project")

        val newMember = ProjectMember(
                userId=userId,
                projectId=projectId
        )
        projectMemberRepository.save(newMember)
        return ResponseEntity.ok().build()
    }

    data class TokenPoster(val token: String)

    @DeleteMapping("/{id}/leave")
    fun leaveFromProject(
            @RequestParam(name="token", defaultValue="") token: String,
            @PathVariable(value="id") rawId: String
    ): ResponseEntity<Unit> {
        val userId: UUID = tokenRepository.getUserIdFromToken(token)

        val projectId: UUID = rawId.toUUID()

        if (userRepository.findById(userId).isEmpty()) throw NotFound("User with given token does not exist")
        if (projectsRepository.findById(projectId).isEmpty()) throw NotFound("Project with given token does not exists")

        val projectMember: ProjectMember = projectMemberRepository.getProjectMember(userId, projectId)

        val projectCreatorId: UUID = projectsRepository.findById(projectId)[0].createdUserId!!

        if (projectCreatorId == userId) throw BadRequest("Project created user can't leave from project")

        projectMemberRepository.delete(projectMember)
        return ResponseEntity.noContent().build()
    }

    // TODO: tag検索と時間での絞り込みの実装、Userテーブルとの連携
    @GetMapping("/condition")
    fun searchWithConditions(
            @RequestParam(name="keyword", defaultValue="#{null}") keyword: String?,
            @RequestParam(name="count", defaultValue="100") rawCount: Int?,
            @RequestParam(name="user", defaultValue="#{null}") user:String?,
            @RequestParam(name="tags", defaultValue="#{null}") rawTags: String?,
            @RequestParam(name="sort", defaultValue="asc") sortOrder: String,
            @RequestParam(name="recruiting", defaultValue="1") rawRecruiting: String,
            @RequestParam(name="searchStartDate", defaultValue="#{null}") searchStart: String?,
            @RequestParam(name="searchEndDate", defaultValue="#{null}") searchEnd: String?
    ): ResponseEntity<List<Projects>>{
        val recruiting: Int = rawRecruiting.toIntOrNull() ?: 1

        val tags: List<String> = rawTags.divideToTags()

        val searchProject = ProjectSearcher(
                projectsRepository.findAll().toSet(),
                projectsRepository,
                tagsToProjectsBridgeRepository,
                userRepository
        )
        searchProject.withKeyWord(keyword)
        searchProject.withUser(user)
        searchProject.withRecruiting(recruiting)
        searchProject.withTags(tags)
        searchProject.filterWithCreatedDay(searchStart, searchEnd)
        searchProject.decideSort(sortOrder)

        val projectsList: List<Projects> = searchProject.getResult()

        if (projectsList.isEmpty()) throw NotFound("No projects match your search criteria")
        return ResponseEntity.ok(projectsList)
    }

    @GetMapping("/{id}")
    fun getProjectById(@PathVariable(value="id", required=true) rawId: String): ResponseEntity<Projects>{
        val projectId: UUID = rawId.toUUID()

        val project: Projects =  getProject(projectId)?: throw BadRequest("")

        return ResponseEntity.ok(project)
    }

    @DeleteMapping("/{id}")
    fun deleteProject(@PathVariable(value="id", required=true) rawId: String, @RequestParam(name="token", required=true) token: String): ResponseEntity<String>{
        val projectId: UUID = rawId.toUUID()

        val project: Projects = getProject(projectId) ?: throw BadRequest("Project not found")

        val userIdFromToken: UUID = tokenRepository.getUserIdFromToken(token)

        if (project.createdUserId == userIdFromToken){
            projectsRepository.delete(project)
            return ResponseEntity.noContent().build()
        }
        throw BadRequest("The user does not created the project")
    }

    private fun getProject(projectId: UUID): Projects?{
        val projectsList: List<Projects> = projectsRepository.findById(projectId)
        return projectsList.singleOrNull()
    }
}

