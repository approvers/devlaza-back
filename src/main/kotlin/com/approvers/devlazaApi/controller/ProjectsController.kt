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

    // TODO: tag検索と時間での絞り込みの実装、countによるコンテンツ数の制限
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
        var projectsList: MutableList<Projects> = projectsRepository.findAll()
        if (keyword is String){
            projectsList = searchWithKeyWord(projectsList, keyword)
        }
        if (user is String){
            projectsList = searchWithUser(projectsList, user)
        }
        if (rawTags is String) {
            rawTags.split("+")
        }
        println(projectsList)
        projectsList = filterWithRecruiting(projectsList, recruiting)
        println(projectsList)
        when(sortOrder){
            "asc" -> projectsList.sortBy{it.created_at}
            "desc" -> {
                projectsList.sortBy{it.created_at}
                projectsList.reverse()
            }
        }
        println(projectsList)
        if (projectsList.size == 0) return ResponseEntity.notFound().build()
        return ResponseEntity.ok(projectsList)
    }

    fun searchWithUser(projectsList: MutableList<Projects>, userID: String): MutableList<Projects>{
        val results: MutableList<Projects> = mutableListOf()
        for (projects in projectsList){
            if (userID == projects.createdUserId) results.add(projects)
        }
        return results
    }

    fun searchWithKeyWord(projectsList: MutableList<Projects>, keyword: String): MutableList<Projects>{
        val results: MutableList<Projects> = mutableListOf()
        val regex = Regex(keyword)
        for (projects in projectsList){
            if (regex.containsMatchIn(projects.name)) results.add(projects)
        }
        return results
    }

    fun filterWithRecruiting(projectsList: MutableList<Projects>, recruiting: Int): MutableList<Projects>{
        val results: MutableList<Projects> = mutableListOf()
        for (projects in projectsList){
            print(projects.recruiting)
            println(recruiting)
            if (projects.recruiting == recruiting) results.add(projects)
        }
        return results
    }

    @GetMapping("/{id}")
    fun getProjectById(@PathVariable(value="id") rawId: String?): ResponseEntity<Projects>{
        val projectId: UUID
        try{
           projectId = UUID.fromString(rawId)
        }catch (e: IllegalArgumentException){
            return ResponseEntity.badRequest().build()
        }
        val projects: Projects? =  findByID(projectId)
        if (projects is Projects) return ResponseEntity.ok(projects)

        return ResponseEntity.notFound().build()
    }

    fun findByID(projectId: UUID): Projects?{
        val projectsList: List<Projects> = projectsRepository.findAll()
        for(project in projectsList){
            if (project.id == projectId) return project
        }
        return null
    }

    @DeleteMapping("/{id}")
    fun deleteProject(@PathVariable(value="id") rawId: String?, @RequestParam(name="user", required=true) user: String): ResponseEntity<String>{
        val projectId: UUID
        try{
            projectId = UUID.fromString(rawId)
        }catch (e: IllegalArgumentException){
            return ResponseEntity.badRequest().build()
        }
        val project: Projects = findByID(projectId) ?: return ResponseEntity.badRequest().build()
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
        val sitesList = findByID(projectId)
        if (sitesList.size != 0) return ResponseEntity.ok(sitesList.toList())
        return ResponseEntity.notFound().build()
    }

    fun findByID(projectId: UUID): MutableList<Sites>{
        val sitesList: MutableList<Sites> = mutableListOf()
        val getSites: List<Sites> = sitesRepository.findAll()
        for (sites in getSites){
            if (sites.projectId == projectId){
                print(sites.id)
                sitesList.add(sites)
            }
        }
        return sitesList
    }
}
