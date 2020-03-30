package com.approvers.devlazaApi.controller

import com.approvers.devlazaApi.repository.ProjectsRepository
import com.approvers.devlazaApi.repository.SitesRepository
import com.approvers.devlazaApi.model.ProjectPoster
import com.approvers.devlazaApi.model.Projects
import com.approvers.devlazaApi.model.Sites
import com.approvers.devlazaApi.model.SitesPoster
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
