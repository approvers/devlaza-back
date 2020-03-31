package com.approvers.devlazaApi.controller

import com.approvers.devlazaApi.model.*
import com.approvers.devlazaApi.repository.ProjectsRepository
import com.approvers.devlazaApi.repository.SitesRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.lang.IllegalArgumentException
import java.util.*
import javax.validation.Valid

@RestController
@RequestMapping("/projects")
class ProjectsController(private val projectsRepository: ProjectsRepository, private val sitesController: SitesController){
    @GetMapping("/")
    fun getAllProjects(): List<Projects> = projectsRepository.findAll()

    @PostMapping("/")
    fun createNewProject(@Valid @RequestBody rawData: ProjectPoster): Projects{
        val projects = Projects(
                name = rawData.name,
                introduction = rawData.introduction,
                createdUserId = rawData.user_id
        )
        val created: List<String> = rawData.sites.split(":")
        projectsRepository.save(projects)
        sitesController.createNewSite(
            SitesPoster(
                explanation = created[0],
                url = created[1],
                projectId = projects.id!!
            )
        )
        return projects
    }

    // TODO: tag検索と時間での絞り込みの実装、Userテーブルとの連携
    @GetMapping("/condition")
    fun searchWithConditions(
            @RequestParam(name="keyword", defaultValue="#{null}") keyword: String?,
            @RequestParam(name="count", defaultValue="100") rawCount: Int?,
            @RequestParam(name="user", defaultValue="#{null}") user:String?,
            @RequestParam(name="tags", defaultValue="#{null}") rawTags: String?,
            @RequestParam(name="sort", defaultValue="asc") sortOrder: String,
            @RequestParam(name="recruiting", defaultValue="1") rawRecruiting: String?

    ): ResponseEntity<MutableList<Projects>>{
        val recruiting: Int = if(rawRecruiting is String && rawRecruiting.toIntOrNull() != null) {
            rawRecruiting.toInt()
        }else{
            1
        }
        var projectsSet: Set<Projects> = projectsRepository.findAll().toSet()

        if (keyword is String){
            val nameResult:Set<Projects> = projectsRepository.findByNameLike("%$keyword%").toSet()
            projectsSet = projectsSet.intersect(nameResult)
        }
        if (user is String){
            val userResult: Set<Projects> = projectsRepository.findByCreatedUserId("$user").toSet()
            projectsSet = projectsSet.intersect(userResult)
        }
        if (rawTags is String) {
            rawTags.split("+")
        }
        if (recruiting == 1 || recruiting == 0){
            val recruitingResult: Set<Projects> = projectsRepository.findByRecruiting(recruiting).toSet()
            projectsSet = projectsSet.intersect(recruitingResult)
        }
        val projectsList: MutableList<Projects> = projectsSet.toMutableList()
        when(sortOrder){
            "asc" -> projectsList.sortBy{it.created_at}
            "desc" -> {
                projectsList.sortBy{it.created_at}
                projectsList.reverse()
            }
        }
        if (projectsList.size == 0) return ResponseEntity.notFound().build()
        return ResponseEntity.ok(projectsList)
    }

    @GetMapping("/{id}")
    fun getProjectById(@PathVariable(value="id") rawId: String?): ResponseEntity<Projects>{
        val projectId: UUID
        try{
           projectId = UUID.fromString(rawId)
        }catch (e: IllegalArgumentException){
            return ResponseEntity.badRequest().build()
        }

        val projects: Projects? =  projectsRepository.findById(projectId)[0]
        if (projects is Projects) return ResponseEntity.ok(projects)

        return ResponseEntity.notFound().build()
    }

    @DeleteMapping("/{id}")
    fun deleteProject(@PathVariable(value="id") rawId: String?, @RequestParam(name="user", required=true) user: String): ResponseEntity<String>{
        val projectId: UUID
        try{
            projectId = UUID.fromString(rawId)
        }catch (e: IllegalArgumentException){
            return ResponseEntity.badRequest().build()
        }
        val projectsList: List<Projects> = projectsRepository.findById(projectId)
        if (projectsList.isEmpty()) return ResponseEntity.badRequest().build()

        val project: Projects = projectsList[0]
        if (project.createdUserId == user){
            projectsRepository.delete(project)
            return ResponseEntity.ok("Deleted")
        }
        return ResponseEntity.badRequest().build()
    }
}

@RestController
@RequestMapping("/sites")
class SitesController(private val sitesRepository: SitesRepository){
    @GetMapping("/")
    fun getAllSites(): List<Sites> = sitesRepository.findAll()

    @PostMapping("/add")
    fun createNewSite(@Valid @RequestBody rawData: SitesPoster): Sites{
        val site = Sites(
                explanation = rawData.explanation,
                url = rawData.url,
                projectId = rawData.projectId
        )
        return sitesRepository.save(site)
    }

    @GetMapping("/{project_id}")
    fun searchFromProjectId(@PathVariable(value="project_id") rawId: String?): ResponseEntity<List<Sites>>{
        val projectId: UUID
        try {
            projectId = UUID.fromString(rawId)
        }catch (e: IllegalArgumentException){
            return ResponseEntity.badRequest().build()
        }
        val sitesList = sitesRepository.findByProjectId(projectId)
        if (sitesList.isNotEmpty()) return ResponseEntity.ok(sitesList.toList())
        return ResponseEntity.notFound().build()
    }
}
