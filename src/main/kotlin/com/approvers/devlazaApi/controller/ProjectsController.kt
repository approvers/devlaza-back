package com.approvers.devlazaApi.controller

import com.approvers.devlazaApi.controller.utils.*
import com.approvers.devlazaApi.errorResponses.errors.BadRequest
import com.approvers.devlazaApi.errorResponses.errors.NotFound
import com.approvers.devlazaApi.model.*
import com.approvers.devlazaApi.repository.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
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
    fun createNewProject(@Valid @RequestBody rawData: ProjectPoster): Projects{
        val tagsUtils = TagsUtils(tagsRepository, tagsToProjectsBridgeRepository)
        val tokenUtils = TokenUtils(tokenRepository, userRepository, projectsRepository)
        val sitesUtils = SitesUtils(sitesController)

        val token: Token = tokenUtils.tokenCheck(rawData.token) ?: return Projects(name="invalid token", introduction="")
        val projects = Projects(
                name = rawData.name,
                introduction = rawData.introduction,
                createdUserId = token.userId
        )

        projectsRepository.save(projects)

        sitesUtils.saveSites(rawData.sites, projects.id!!)

        tagsUtils.saveTags(rawData.tags, projects.id!!)

        return projects
    }

    @PatchMapping("/join/{id}")
    fun joinToProject(
            @RequestParam(name="token", defaultValue="") token: String,
            @PathVariable(value="id") rawId: String
    ): ResponseEntity<String>{
        val tokenUtils = TokenUtils(
                tokenRepository,
                userRepository,
                projectsRepository
        )
        val projectMemberUtils = ProjectMemberUtils(projectMemberRepository)
        val (userId: UUID, projectId: UUID) = tokenUtils.convertToTokenAndProjectIdMap(token, rawId)?: return ResponseEntity.badRequest().build()

        if (projectMemberUtils.checkProjectMemberExists(userId, projectId)) throw BadRequest("User has already joined the project.")

        val newMember = ProjectMember(
                userId=userId,
                projectId=projectId
        )
        projectMemberRepository.save(newMember)
        return ResponseEntity.ok("Joined")
    }

    @DeleteMapping("/leave/{id}")
    fun leaveFromProject(
            @RequestParam(name="token", defaultValue="") token: String,
            @PathVariable(value="id") rawId: String
    ): ResponseEntity<String>{
        val tokenUtils = TokenUtils(
                tokenRepository,
                userRepository,
                projectsRepository
        )
        val projectMemberUtils = ProjectMemberUtils(projectMemberRepository)
        val (userId: UUID, projectId: UUID) = tokenUtils.convertToTokenAndProjectIdMap(token, rawId)?: return ResponseEntity.badRequest().build()

        if (!projectMemberUtils.checkProjectMemberExists(userId, projectId)) throw NotFound("User doesn't join the project")

        val projectMember: ProjectMember = projectMemberUtils.getProjectMember(userId, projectId)[0]

        val projectCreatorId: UUID = projectsRepository.findById(projectId)[0].createdUserId!!

        if (projectCreatorId == userId) return ResponseEntity.badRequest().body("Project created user can't leave from project")

        projectMemberRepository.delete(projectMember)
        return ResponseEntity.ok("Deleted!")
    }

    // TODO: tag検索と時間での絞り込みの実装、Userテーブルとの連携
    @GetMapping("/condition")
    fun searchWithConditions(
            @RequestParam(name="keyword", defaultValue="#{null}") keyword: String?,
            @RequestParam(name="count", defaultValue="100") rawCount: Int?,
            @RequestParam(name="user", defaultValue="#{null}") user:String?,
            @RequestParam(name="tags", defaultValue="#{null}") rawTags: String?,
            @RequestParam(name="sort", defaultValue="asc") sortOrder: String,
            @RequestParam(name="recruiting", defaultValue="1") rawRecruiting: String?,
            @RequestParam(name="searchStartDate", defaultValue="#{null}") searchStart: String?,
            @RequestParam(name="searchEndDate", defaultValue="#{null}") searchEnd: String?
    ): ResponseEntity<List<Projects>>{
        val tagsUtils = TagsUtils(tagsRepository, tagsToProjectsBridgeRepository)

        val recruiting: Int = if(rawRecruiting != null && rawRecruiting.toIntOrNull() != null) {
            rawRecruiting.toInt()
        }else{
            1
        }
        val tags: List<String> = tagsUtils.divideTags(rawTags)

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

        if (projectsList.isEmpty()) return ResponseEntity.notFound().build()
        return ResponseEntity.ok(projectsList)
    }

    @GetMapping("/{id}")
    fun getProjectById(@PathVariable(value="id") rawId: String?): ResponseEntity<Projects>{
        val util = BaseUtils()
        val projectId: UUID = util.convertStringToUUID(rawId) ?: return ResponseEntity.badRequest().build()

        val project: Projects =  getProject(projectId)?: return ResponseEntity.badRequest().build()

        return ResponseEntity.ok(project)
    }

    @DeleteMapping("/{id}")
    fun deleteProject(@PathVariable(value="id") rawId: String?, @RequestParam(name="token", required=true) token: String): ResponseEntity<String>{
        val tokenUtils = TokenUtils(tokenRepository, userRepository, projectsRepository)
        val util = BaseUtils()

        val projectId: UUID = util.convertStringToUUID(rawId) ?: return ResponseEntity.badRequest().build()

        val project: Projects = getProject(projectId) ?: return ResponseEntity.badRequest().build()

        val userIdFromToken: UUID = tokenUtils.getUserIdFromToken(token) ?: return ResponseEntity.badRequest().build()

        if (project.createdUserId == userIdFromToken){
            projectsRepository.delete(project)
            return ResponseEntity.ok("Deleted")
        }
        return ResponseEntity.badRequest().build()
    }


    private fun getProject(projectId: UUID): Projects?{
        val projectsList: List<Projects> = projectsRepository.findById(projectId)
        if (projectsList.isEmpty()) return null
        return projectsList[0]
    }

}

