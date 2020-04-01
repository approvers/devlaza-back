package com.approvers.devlazaApi.controller

import com.approvers.devlazaApi.model.*
import com.approvers.devlazaApi.repository.ProjectsRepository
import com.approvers.devlazaApi.repository.SitesRepository
import com.approvers.devlazaApi.repository.TagsRepository
import com.approvers.devlazaApi.repository.TagsToProjectsBridgeRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.lang.IllegalArgumentException
import java.util.*
import javax.validation.Valid

@RestController
@RequestMapping("/projects")
class ProjectsController(
        private val projectsRepository: ProjectsRepository,
        private val sitesController: SitesController,
        private val tagsToProjectsBridgeRepository: TagsToProjectsBridgeRepository,
        private val tagsRepository: TagsRepository
){
    @GetMapping("/")
    fun getAllProjects(): List<Projects> = projectsRepository.findAll()

    @PostMapping("/")
    fun createNewProject(@Valid @RequestBody rawData: ProjectPoster): Projects{
        val projects = Projects(
                name = rawData.name,
                introduction = rawData.introduction,
                createdUserId = rawData.user_id
        )

        val dividedRawSites: List<String> = rawData.sites.split("+")
        val sites: MutableList<List<String>> = mutableListOf()

        for (rawSite in dividedRawSites){
            val colonIndex: Int = rawSite.indexOf("-:-")
            if (colonIndex == -1) continue
            val site: List<String> = rawSite.split("-:-")
            sites.add(site)
        }

        projectsRepository.save(projects)
        for (site in sites) {
            sitesController.createNewSite(
                    SitesPoster(
                            explanation = site[0],
                            url = site[1],
                            projectId = projects.id!!
                    )
            )
        }

        val tags: List<String> = divideTags(rawData.tags)

        for (tag in tags){
            if (tagsRepository.findByName(tag).isEmpty()){
                val newTag = Tags(name=tag)
                tagsRepository.save(newTag)
            }

            val tmp = TagsToProjectsBridge(
                    tagName=tag,
                    projectId=projects.id!!
            )
            println(projects.id)
            tagsToProjectsBridgeRepository.save(tmp)
        }

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
    ): ResponseEntity<List<Projects>>{
        val recruiting: Int = if(rawRecruiting != null && rawRecruiting.toIntOrNull() != null) {
            rawRecruiting.toInt()
        }else{
            1
        }
        val tags: List<String> = divideTags(rawTags)

        val searchProject = SearchProject(
                projectsRepository.findAll().toSet(),
                projectsRepository,
                tagsToProjectsBridgeRepository
        )
        searchProject.withKeyWord(keyword)
        searchProject.withUser(user)
        searchProject.withRecruiting(recruiting)
        searchProject.withTags(tags)
        searchProject.decideSort(sortOrder)

        val projectsList: List<Projects> = searchProject.getResult()
        if (projectsList.isEmpty()) return ResponseEntity.notFound().build()
        return ResponseEntity.ok(projectsList)
    }

    fun divideTags(rawTags: String?): List<String>{
        if (rawTags !is String) return listOf()

        val regex = Regex("\\+")
        val tags: MutableList<String>
        tags = if (regex.containsMatchIn(rawTags)){
            rawTags.split("+").toMutableList()
        }else{
            rawTags.split(" ").toMutableList()
        }

        while (tags.indexOf("") != -1){
            tags.removeAt(tags.indexOf(""))
        }
        return tags.toList()
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

class SearchProject(private var projects: Set<Projects>, private val projectsRepository: ProjectsRepository, private val tagsToProjectsBridgeRepository: TagsToProjectsBridgeRepository){
    fun withKeyWord(keyword: String?){
        if(keyword !is String) return
        val searchResults: Set<Projects> = projectsRepository.findByNameLike("%$keyword%").toSet()
        projects = projects.intersect(searchResults)
    }

    fun withUser(username: String?){
        if(username !is String) return
        val userResult: Set<Projects> = projectsRepository.findByCreatedUserId("$username").toSet()
        projects = projects.intersect(userResult)
    }

    fun withTags(tags: List<String>){
        if (tags.isEmpty()) return

        val result: MutableSet<Projects> = mutableSetOf()
        for (project in projects){
            val projectId: UUID = project.id!!
            var tagsCount: Int = tags.size
            println(tagsCount)

            val allTags: List<TagsToProjectsBridge> = tagsToProjectsBridgeRepository.findAll()

            val tagsList: MutableList<TagsToProjectsBridge> = mutableListOf()
            for (tag in allTags){
                if (tag.projectId == projectId) tagsList.add(tag)
            }
            println(tagsList)

            for (tagsToProjectBridge in tagsList){
                val tagName: String = tagsToProjectBridge.tagName
                for (tag in tags){
                    if (tag == tagName){
                        tagsCount--
                        break
                    }
                }
            }
            println(tagsCount)
            if (tagsCount == 0){
                result.add(project)
            }
        }
        projects = projects.intersect(result)
    }

    fun withRecruiting(recruiting: Int){
        if (recruiting == 1 || recruiting == 0){
            val recruitingResult: Set<Projects> = projectsRepository.findByRecruiting(recruiting).toSet()
            projects = projects.intersect(recruitingResult)
        }
    }

    fun decideSort(sortOrder: String?){
        val projectsList: MutableList<Projects> = projects.toMutableList()
        when(sortOrder){
            "asc" -> projectsList.sortBy{it.created_at}
            "desc" -> {
                projectsList.sortBy{it.created_at}
                projectsList.reverse()
            }
            else -> projectsList.sortBy{it.created_at}
        }
        projects = projectsList.toSet()
    }

    fun getResult(): List<Projects>{
        return projects.toMutableList()
    }
}
