package com.approvers.devlazaApi.controller

import com.approvers.devlazaApi.controller.search.project.SearchWithCreatedDate
import com.approvers.devlazaApi.controller.search.project.SearchWithKeyword
import com.approvers.devlazaApi.controller.search.project.SearchWithRecruiting
import com.approvers.devlazaApi.controller.search.project.SearchWithTags
import com.approvers.devlazaApi.controller.search.project.SearchWithUser
import com.approvers.devlazaApi.errors.BadRequest
import com.approvers.devlazaApi.errors.NotFound
import com.approvers.devlazaApi.model.ProjectMember
import com.approvers.devlazaApi.model.ProjectPoster
import com.approvers.devlazaApi.model.Projects
import com.approvers.devlazaApi.model.Tags
import com.approvers.devlazaApi.model.TagsToProjectsBridge
import com.approvers.devlazaApi.repository.ProjectMemberRepository
import com.approvers.devlazaApi.repository.ProjectsRepository
import com.approvers.devlazaApi.repository.TagsRepository
import com.approvers.devlazaApi.repository.TagsToProjectsBridgeRepository
import com.approvers.devlazaApi.repository.UserRepository
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.UnsupportedEncodingException
import java.util.UUID
import javax.validation.Valid

@RestController
@RequestMapping("/projects")
class ProjectsController(
    private val projectsRepository: ProjectsRepository,
    private val sitesController: SitesController,
    private val tagsToProjectsBridgeRepository: TagsToProjectsBridgeRepository,
    private val tagsRepository: TagsRepository,
    private val projectMemberRepository: ProjectMemberRepository,
    private val userRepository: UserRepository
) {
    private val secret: String = System.getenv("secret") ?: "secret"

    // JWT
    private val algorithm: Algorithm = Algorithm.HMAC256(secret)
    private val verifier: JWTVerifier = JWT.require(algorithm).build()

    // searcher
    private val searchWithKeyword = SearchWithKeyword()
    private val searchWithUser = SearchWithUser()
    private val searchWithTags = SearchWithTags()
    private val searchWithCreatedDate = SearchWithCreatedDate()
    private val searchWithRecruiting = SearchWithRecruiting()

    @GetMapping("/")
    fun getAllProjects(
        @RequestParam(name = "get_begin", defaultValue = "0") raw_get_begin: String,
        @RequestParam(name = "get_end", defaultValue = "20") raw_get_end: String
    ): List<Projects> {
        val getBegin: Int = raw_get_begin.toIntOrNull() ?: throw BadRequest("get_begin must be integer")
        var getEnd: Int = raw_get_end.toIntOrNull() ?: throw BadRequest("get_end must be integer")

        if (getBegin >= getEnd) BadRequest("get_begin must be smaller than get_end")

        val allProjects: MutableList<Projects> = projectsRepository.findAll().toMutableList()
        allProjects.sortBy { it.created_at }
        allProjects.reverse()
        if (allProjects.size < getBegin) NotFound("get_begin is larger than number of projects")

        if (allProjects.size < getEnd) getEnd = allProjects.size

        return allProjects.slice(getBegin..getEnd)
    }

    @PostMapping("/")
    fun createNewProject(@Valid @RequestBody rawData: ProjectPoster): ResponseEntity<Projects> {
        val userId: UUID = decode(rawData.token)
        val projects = Projects(
            name = rawData.name,
            introduction = rawData.introduction,
            createdUserId = userId
        )

        projectsRepository.save(projects)

        sitesController.saveSites(rawData.sites, projects.id!!)

        tagsToProjectsBridgeRepository.addTagToProject(rawData.tags, projects.id!!, tagsRepository)

        return ResponseEntity.ok(projects)
    }

    @PostMapping("/{id}/join")
    fun joinToProject(
        @Valid @RequestBody tokenPoster: TokenPoster,
        @PathVariable(value = "id") rawId: String
    ): ResponseEntity<Unit> {
        val token: String = tokenPoster.token
        val userId: UUID = decode(token)

        val projectId: UUID = rawId.toUUID()

        if (userRepository.findById(userId).isEmpty()) throw NotFound("User with given token does not exist")
        if (projectsRepository.findById(projectId).isEmpty()) throw NotFound("Project with given token does not exists")

        if (projectMemberRepository.checkProjectMemberExist(userId, projectId)) throw BadRequest("User already join to this project")

        val newMember = ProjectMember(
            userId = userId,
            projectId = projectId
        )
        projectMemberRepository.save(newMember)
        return ResponseEntity.ok().build()
    }

    data class TokenPoster(val token: String)

    @PostMapping("/{id}/leave")
    fun leaveFromProject(
        @Valid @RequestBody tokenPoster: TokenPoster,
        @PathVariable(value = "id") rawId: String
    ): ResponseEntity<Unit> {
        val userId: UUID = decode(tokenPoster.token)

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
        @RequestParam(name = "keyword", defaultValue = "#{null}") keyword: String?,
        @RequestParam(name = "count", defaultValue = "100") rawCount: Int?,
        @RequestParam(name = "user", defaultValue = "#{null}") user: String?,
        @RequestParam(name = "tags", defaultValue = "#{null}") rawTags: String?,
        @RequestParam(name = "sort", defaultValue = "asc") sortOrder: String,
        @RequestParam(name = "recruiting", defaultValue = "1") rawRecruiting: String,
        @RequestParam(name = "searchStartDate", defaultValue = "#{null}") searchStart: String?,
        @RequestParam(name = "searchEndDate", defaultValue = "#{null}") searchEnd: String?
    ): ResponseEntity<List<Projects>> {
        val recruiting: Int = rawRecruiting.toIntOrNull() ?: 1

        val tags: List<String> = rawTags.divideToTags()

        val projectsList: List<Projects> = search(
            keyword,
            user,
            recruiting,
            tags,
            searchStart,
            searchEnd,
            sortOrder
        )

        if (projectsList.isEmpty()) throw NotFound("No projects match your search criteria")
        return ResponseEntity.ok(projectsList)
    }

    @GetMapping("/{id}")
    fun getProjectById(@PathVariable(value = "id", required = true) rawId: String): ResponseEntity<Projects> {
        val projectId: UUID = rawId.toUUID()

        val project: Projects = getProject(projectId) ?: throw BadRequest("")

        return ResponseEntity.ok(project)
    }

    @DeleteMapping("/{id}")
    fun deleteProject(@PathVariable(value = "id", required = true) rawId: String, @RequestParam(name = "token", required = true) token: String): ResponseEntity<String> {
        val projectId: UUID = rawId.toUUID()

        val project: Projects = getProject(projectId) ?: throw BadRequest("Project not found")

        val userIdFromToken: UUID = decode(token)

        if (project.createdUserId == userIdFromToken) {
            projectsRepository.delete(project)
            return ResponseEntity.noContent().build()
        }
        throw BadRequest("The user does not created the project")
    }

    private fun decode(token: String): UUID {
        val userId: UUID
        try {
            val decodedJWT: DecodedJWT = verifier.verify(token)

            userId = UUID.fromString(
                decodedJWT.getClaim("USER_ID").asString()
            )
        } catch (e: Exception) {
            when (e) {
                is UnsupportedEncodingException, is JWTVerificationException
                -> throw BadRequest("token is invalid")
                else -> throw e
            }
        }

        return userId
    }

    private fun getProject(projectId: UUID): Projects? {
        val projectsList: List<Projects> = projectsRepository.findById(projectId)
        return projectsList.singleOrNull()
    }

    private fun String.toUUID(): UUID {
        val id: UUID
        try {
            id = UUID.fromString(this)
        } catch (e: IllegalArgumentException) {
            throw BadRequest("The format of id is invalid")
        }
        return id
    }

    private fun TagsToProjectsBridgeRepository.addTagToProject(rawTags: String, projectId: UUID, tagsRepository: TagsRepository) {
        val tags: List<String> = rawTags.divideToTags().distinct()

        for (tag in tags) {
            tagsRepository.createNewTag(tag)
            val tmp = TagsToProjectsBridge(tagName = tag, projectId = projectId)
            this.save(tmp)
        }
    }

    private fun String?.divideToTags(): List<String> {
        val tags: MutableList<String> = this.getTags() ?: return listOf()

        return tags.filterNot { it.isEmpty() }
    }

    private fun String?.getTags(): MutableList<String>? {
        if (this !is String) return null

        val regex = Regex("\\+")
        val tags: MutableList<String>
        tags = if (regex.containsMatchIn(this)) {
            this.split("+").toMutableList()
        } else {
            this.split(" ").toMutableList()
        }
        return tags
    }

    private fun TagsRepository.createNewTag(tag: String) {
        if (this.findByName(tag).isNotEmpty()) return

        val newTag = Tags(name = tag)
        this.save(newTag)
    }

    private fun ProjectMemberRepository.getProjectMember(userId: UUID, projectId: UUID): ProjectMember {
        val projectMemberSetWithUserId: Set<ProjectMember> = this.findByUserId(userId).toSet()
        val projectMemberSetWithProjectId: Set<ProjectMember> = this.findByProjectId(projectId).toSet()

        val getResultList: List<ProjectMember> = projectMemberSetWithProjectId.intersect(projectMemberSetWithUserId).toList()

        if (getResultList.isEmpty()) throw NotFound("No project members were found for the given information")

        return getResultList[0]
    }

    private fun ProjectMemberRepository.checkProjectMemberExist(userId: UUID, projectId: UUID): Boolean {
        return try {
            this.getProjectMember(userId, projectId)
            true
        } catch (e: NotFound) {
            false
        }
    }

    private fun search(
        keyword: String?,
        userName: String?,
        recruiting: Int,
        tags: List<String>,
        searchStart: String?,
        searchEnd: String?,
        sortOrder: String
    ): List<Projects> {
        var projects: Set<Projects> = projectsRepository.findAll().toSet()
        projects = searchWithKeyword.search(projects, keyword, projectsRepository)
        projects = searchWithUser.search(projects, userName, projectsRepository, userRepository)
        projects = searchWithTags.search(projects, tags, projectsRepository, tagsToProjectsBridgeRepository)
        projects = searchWithRecruiting.search(projects, recruiting, projectsRepository)

        projects = searchWithCreatedDate.search(projects, listOf(searchStart, searchEnd), projectsRepository)

        val projectsList: MutableList<Projects> = projects.toMutableList()
        when (sortOrder) {
            "asc" -> projectsList.sortBy { it.created_at }
            "desc" -> {
                projectsList.sortBy { it.created_at }
                projectsList.reverse()
            }
            else -> projectsList.sortBy { it.created_at }
        }
        return projectsList.toList()
    }
}

